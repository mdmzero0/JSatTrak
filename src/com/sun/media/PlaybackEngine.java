// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PlaybackEngine.java

package com.sun.media;

import com.sun.media.controls.BitRateAdapter;
import com.sun.media.controls.FramePositioningAdapter;
import com.sun.media.controls.FrameRateAdapter;
import com.sun.media.controls.ProgressControl;
import com.sun.media.controls.ProgressControlAdapter;
import com.sun.media.controls.StringControl;
import com.sun.media.controls.StringControlAdapter;
import com.sun.media.protocol.Streamable;
import com.sun.media.renderer.audio.AudioRenderer;
import com.sun.media.util.JMFI18N;
import com.sun.media.util.RTPInfo;
import com.sun.media.util.Resource;
import java.awt.*;
import java.io.IOException;
import java.util.Vector;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.renderer.VideoRenderer;
import javax.media.renderer.VisualContainer;

// Referenced classes of package com.sun.media:
//            BasicController, BasicTrackControl, BasicJMD, BasicSinkModule, 
//            StateTransistor, BasicModule, BasicFilterModule, BasicSourceModule, 
//            BasicRendererModule, Module, ModuleListener, Log, 
//            JMD, InputConnector, Connector, GraphNode, 
//            OutputConnector, SimpleGraphBuilder, BasicPlayer, BasicClock

public class PlaybackEngine extends BasicController
    implements ModuleListener
{
    class LightPanel extends Container
        implements VisualContainer
    {

        public LightPanel(Vector visuals)
        {
        }
    }

    class HeavyPanel extends Panel
        implements VisualContainer
    {

        public HeavyPanel(Vector visuals)
        {
        }
    }

    class PlayerGraphBuilder extends SimpleGraphBuilder
    {

        protected GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node)
        {
            return engine.buildTrackFromGraph((PlayerTControl)tc, node);
        }

        protected PlaybackEngine engine;

        PlayerGraphBuilder(PlaybackEngine engine)
        {
            this.engine = engine;
        }
    }

    class PlayerTControl extends BasicTrackControl
        implements Owned
    {

        public Object getOwner()
        {
            return player;
        }

        public boolean buildTrack(int trackID, int numTracks)
        {
            if(gb == null)
                gb = new PlayerGraphBuilder(super.engine);
            else
                gb.reset();
            boolean rtn = gb.buildGraph(this);
            gb = null;
            return rtn;
        }

        public boolean isTimeBase()
        {
            for(int j = 0; j < super.modules.size(); j++)
                if(super.modules.elementAt(j) == masterSink)
                    return true;

            return false;
        }

        protected ProgressControl progressControl()
        {
            return PlaybackEngine.this.progressControl;
        }

        protected FrameRateControl frameRateControl()
        {
            return PlaybackEngine.this.frameRateControl;
        }

        protected PlayerGraphBuilder gb;

        public PlayerTControl(PlaybackEngine engine, Track track, OutputConnector oc)
        {
            super(engine, track, oc);
        }
    }

    class SlaveClock
        implements Clock
    {

        public void setMaster(Clock master)
        {
            this.master = master;
            current = ((Clock) (master != null ? master : ((Clock) (backup))));
            if(master != null)
                try
                {
                    backup.setTimeBase(master.getTimeBase());
                }
                catch(IncompatibleTimeBaseException e) { }
        }

        public void setTimeBase(TimeBase tb)
            throws IncompatibleTimeBaseException
        {
            synchronized(backup)
            {
                backup.setTimeBase(tb);
            }
        }

        public void syncStart(Time tbt)
        {
            synchronized(backup)
            {
                if(backup.getState() != 1)
                    backup.syncStart(tbt);
            }
        }

        public void stop()
        {
            synchronized(backup)
            {
                backup.stop();
            }
        }

        public void setStopTime(Time t)
        {
            synchronized(backup)
            {
                backup.setStopTime(t);
            }
        }

        public Time getStopTime()
        {
            return backup.getStopTime();
        }

        public void setMediaTime(Time now)
        {
            synchronized(backup)
            {
                if(backup.getState() == 1)
                {
                    backup.stop();
                    backup.setMediaTime(now);
                    backup.syncStart(backup.getTimeBase().getTime());
                } else
                {
                    backup.setMediaTime(now);
                }
            }
        }

        public Time getMediaTime()
        {
            return current.getMediaTime();
        }

        public long getMediaNanoseconds()
        {
            return current.getMediaNanoseconds();
        }

        public Time getSyncTime()
        {
            return current.getSyncTime();
        }

        public TimeBase getTimeBase()
        {
            return current.getTimeBase();
        }

        public Time mapToTimeBase(Time t)
            throws ClockStoppedException
        {
            return current.mapToTimeBase(t);
        }

        public float setRate(float factor)
        {
            return backup.setRate(factor);
        }

        public float getRate()
        {
            return current.getRate();
        }

        protected void reset(boolean useMaster)
        {
            if(master != null && useMaster)
            {
                current = master;
            } else
            {
                if(master != null)
                    synchronized(backup)
                    {
                        boolean started = false;
                        if(backup.getState() == 1)
                        {
                            backup.stop();
                            started = true;
                        }
                        backup.setMediaTime(master.getMediaTime());
                        if(started)
                            backup.syncStart(backup.getTimeBase().getTime());
                    }
                current = backup;
            }
        }

        Clock master;
        Clock current;
        BasicClock backup;

        SlaveClock()
        {
            backup = new BasicClock();
            current = backup;
        }
    }

    class BitRateA extends BitRateAdapter
        implements Owned
    {

        public int setBitRate(int rate)
        {
            super.value = rate;
            return super.value;
        }

        public Component getControlComponent()
        {
            return null;
        }

        public Object getOwner()
        {
            return player;
        }

        public BitRateA(int initialBitRate, int minBitRate, int maxBitRate, boolean settable)
        {
            super(initialBitRate, minBitRate, maxBitRate, settable);
        }
    }


    public static void setMemoryTrace(boolean on)
    {
        TRACE_ON = on;
    }

    public PlaybackEngine(BasicPlayer p)
    {
        masterSink = null;
        internalErrorOccurred = false;
        prefetched = false;
        started = false;
        dataPathBlocked = false;
        useMoreRenderBuffer = false;
        deallocated = false;
        prefetchEnabled = true;
        timeBeforeAbortPrefetch = null;
        rate = 1.0F;
        framePositioningControl = null;
        latency = 0L;
        jmd = null;
        container = null;
        trackControls = new BasicTrackControl[0];
        configError = "Failed to configure: " + this;
        configIntError = "  The configure process is being interrupted.\n";
        configInt2Error = "interrupted while the Processor is being configured.";
        parseError = "failed to parse the input media.";
        realizeError = "Failed to realize: " + this;
        timeBaseError = "  Cannot manage the different time bases.\n";
        genericProcessorError = "cannot handle the customized options set on the Processor.\nCheck jmf.log for full details.";
        prefetchError = "Failed to prefetch: " + this;
        rtpInfo = null;
        testedRTP = false;
        prefetchLogged = false;
        markedDataStartTime = 0L;
        reportOnce = false;
        lastBitRate = 0L;
        lastStatsTime = 0L;
        long initTime = System.currentTimeMillis();
        player = p;
        createProgressControl();
        setClock(slaveClock = new SlaveClock());
        super.stopThreadEnabled = false;
        profile("instantiation", initTime);
    }

    protected boolean isConfigurable()
    {
        return true;
    }

    public void setSource(DataSource ds)
        throws IOException, IncompatibleSourceException
    {
        try
        {
            source = BasicSourceModule.createModule(ds);
        }
        catch(IOException ioe)
        {
            Log.warning("Input DataSource: " + ds);
            Log.warning("  Failed with IO exception: " + ioe.getMessage());
            throw ioe;
        }
        catch(IncompatibleSourceException ise)
        {
            Log.warning("Input DataSource: " + ds);
            Log.warning("  is not compatible with the MediaEngine.");
            Log.warning("  It's likely that the DataSource is required to extend PullDataSource;");
            Log.warning("  and that its source streams implement the Seekable interface ");
            Log.warning("  and with random access capability.");
            throw ise;
        }
        if(source == null)
            throw new IncompatibleSourceException();
        if(DEBUG && jmd == null)
        {
            String jmdTitle = "PlugIn Viewer";
            if(ds != null && ds.getLocator() != null)
            {
                jmdTitle = ds.getLocator().toString();
                String protocol = ds.getLocator().getProtocol();
                if(protocol != null)
                {
                    protocol = protocol.toLowerCase();
                    if(protocol.equals("file") || protocol.startsWith("http") || protocol.equals("ftp"))
                        useMoreRenderBuffer = true;
                }
            }
            jmd = new BasicJMD(jmdTitle);
        }
        if(DEBUG)
            source.setJMD(jmd);
        source.setController(this);
        dsource = ds;
        if((dsource instanceof Streamable) && !((Streamable)dsource).isPrefetchable())
        {
            prefetchEnabled = false;
            dataPathBlocked = true;
        }
        if(dsource instanceof CaptureDevice)
            prefetchEnabled = false;
    }

    protected boolean doConfigure()
    {
        if(!doConfigure1())
            return false;
        String names[] = source.getOutputConnectorNames();
        trackControls = new BasicTrackControl[tracks.length];
        for(int i = 0; i < tracks.length; i++)
            trackControls[i] = new PlayerTControl(this, tracks[i], source.getOutputConnector(names[i]));

        return doConfigure2();
    }

    protected boolean doConfigure1()
    {
        long parsingTime = System.currentTimeMillis();
        modules = new Vector();
        filters = new Vector();
        sinks = new Vector();
        waitPrefetched = new Vector();
        waitStopped = new Vector();
        waitEnded = new Vector();
        waitResetted = new Vector();
        source.setModuleListener(this);
        source.setController(this);
        modules.addElement(source);
        if(!source.doRealize())
        {
            Log.error(configError);
            if(source.errMsg != null)
                Log.error("  " + source.errMsg + "\n");
            player.processError = parseError;
            return false;
        }
        if(isInterrupted())
        {
            Log.error(configError);
            Log.error(configIntError);
            player.processError = configInt2Error;
            return false;
        }
        if((parser = source.getDemultiplexer()) == null)
        {
            Log.error(configError);
            Log.error("  Cannot obtain demultiplexer for the source.\n");
            player.processError = parseError;
            return false;
        }
        try
        {
            tracks = parser.getTracks();
        }
        catch(Exception e)
        {
            Log.error(configError);
            Log.error("  Cannot obtain tracks from the demultiplexer: " + e + "\n");
            player.processError = parseError;
            return false;
        }
        if(isInterrupted())
        {
            Log.error(configError);
            Log.error(configIntError);
            player.processError = configInt2Error;
            return false;
        } else
        {
            profile("parsing", parsingTime);
            return true;
        }
    }

    protected boolean doConfigure2()
    {
        if(parser.isPositionable() && parser.isRandomAccess())
        {
            Track master = FramePositioningAdapter.getMasterTrack(tracks);
            if(master != null)
                framePositioningControl = new FramePositioningAdapter(player, master);
        }
        return true;
    }

    protected synchronized boolean doRealize()
    {
        return doRealize1() && doRealize2();
    }

    protected boolean doRealize1()
    {
        Log.comment("Building flow graph for: " + dsource.getLocator() + "\n");
        realizeTime = System.currentTimeMillis();
        boolean atLeastOneTrack = false;
        int trackID = 0;
        int numTracks = getNumTracks();
        for(int i = 0; i < trackControls.length; i++)
            if(trackControls[i].isEnabled())
            {
                Log.setIndent(0);
                Log.comment("Building Track: " + i);
                if(trackControls[i].buildTrack(trackID, numTracks))
                {
                    atLeastOneTrack = true;
                    trackControls[i].setEnabled(true);
                } else
                {
                    if(trackControls[i].isCustomized())
                    {
                        Log.error(realizeError);
                        trackControls[i].prError();
                        player.processError = genericProcessorError;
                        return false;
                    }
                    trackControls[i].setEnabled(false);
                    Log.warning("Failed to handle track " + i);
                    trackControls[i].prError();
                }
                if(isInterrupted())
                {
                    Log.error(realizeError);
                    Log.error("  The graph building process is being interrupted.\n");
                    player.processError = "interrupted while the player is being constructed.";
                    return false;
                }
                trackID++;
                Log.write("\n");
            }

        if(!atLeastOneTrack)
        {
            Log.error(realizeError);
            player.processError = "input media not supported: " + getCodecList();
            return false;
        } else
        {
            return true;
        }
    }

    protected boolean doRealize2()
    {
        if(!manageTimeBases())
        {
            Log.error(realizeError);
            Log.error(timeBaseError);
            player.processError = timeBaseError;
            return false;
        }
        Log.comment("Here's the completed flow graph:");
        traceGraph(source);
        Log.write("\n");
        profile("graph building", realizeTime);
        realizeTime = System.currentTimeMillis();
        updateFormats();
        if(DEBUG)
            jmd.initGraph(source);
        profile("realize, post graph building", realizeTime);
        return true;
    }

    String getCodecList()
    {
        String list = "";
        for(int i = 0; i < trackControls.length; i++)
        {
            Format fmt = trackControls[i].getOriginalFormat();
            if(fmt != null && fmt.getEncoding() != null)
            {
                list = list + fmt.getEncoding();
                if(fmt instanceof VideoFormat)
                    list = list + " video";
                else
                if(fmt instanceof AudioFormat)
                    list = list + " audio";
                if(i + 1 < trackControls.length)
                    list = list + ", ";
            }
        }

        return list;
    }

    int getNumTracks()
    {
        int num = 0;
        for(int i = 0; i < trackControls.length; i++)
            if(trackControls[i].isEnabled())
                num++;

        return num;
    }

    boolean manageTimeBases()
    {
        masterSink = findMasterSink();
        return updateMasterTimeBase();
    }

    protected BasicSinkModule findMasterSink()
    {
        for(int i = 0; i < trackControls.length; i++)
            if(trackControls[i].isEnabled() && trackControls[i].rendererModule != null && trackControls[i].rendererModule.getClock() != null)
                return trackControls[i].rendererModule;

        return null;
    }

    boolean updateMasterTimeBase()
    {
        int size = sinks.size();
        if(masterSink != null)
            slaveClock.setMaster(masterSink.getClock());
        else
            slaveClock.setMaster(null);
        for(int i = 0; i < size; i++)
        {
            BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
            if(bsm != masterSink && !bsm.prefetchFailed())
                try
                {
                    bsm.setTimeBase(slaveClock.getTimeBase());
                }
                catch(IncompatibleTimeBaseException e)
                {
                    return false;
                }
        }

        return true;
    }

    protected synchronized void abortConfigure()
    {
        if(source != null)
            source.abortRealize();
    }

    protected synchronized void abortRealize()
    {
        int size = modules.size();
        for(int i = 0; i < size; i++)
        {
            StateTransistor m = (StateTransistor)modules.elementAt(i);
            m.abortRealize();
        }

    }

    protected synchronized void doFailedRealize()
    {
        int size = modules.size();
        for(int i = 0; i < size; i++)
        {
            StateTransistor m = (StateTransistor)modules.elementAt(i);
            m.doFailedRealize();
        }

        super.doFailedRealize();
    }

    protected synchronized boolean doPrefetch()
    {
        if(prefetched)
            return true;
        else
            return doPrefetch1() && doPrefetch2();
    }

    protected boolean doPrefetch1()
    {
        if(timeBeforeAbortPrefetch != null)
        {
            doSetMediaTime(timeBeforeAbortPrefetch);
            timeBeforeAbortPrefetch = null;
        }
        prefetchTime = System.currentTimeMillis();
        resetPrefetchedList();
        if(!source.doPrefetch())
        {
            Log.error(prefetchError);
            if(dsource != null)
                Log.error("  Cannot prefetch the source: " + dsource.getLocator() + "\n");
            return false;
        }
        boolean atLeastOneTrack = false;
        for(int i = 0; i < trackControls.length; i++)
        {
            boolean usedToFailed = trackControls[i].prefetchFailed;
            if(!usedToFailed || getState() <= 400)
                if(trackControls[i].prefetchTrack())
                {
                    atLeastOneTrack = true;
                    if(usedToFailed)
                    {
                        if(!manageTimeBases())
                        {
                            Log.error(prefetchError);
                            Log.error(timeBaseError);
                            return false;
                        }
                        doSetMediaTime(getMediaTime());
                    }
                } else
                {
                    trackControls[i].prError();
                    if(trackControls[i].isTimeBase() && !manageTimeBases())
                    {
                        Log.error(prefetchError);
                        Log.error(timeBaseError);
                        player.processError = timeBaseError;
                        return false;
                    }
                    if((trackControls[i].getFormat() instanceof AudioFormat) && trackControls[i].rendererFailed)
                        player.processError = "cannot open the audio device.";
                }
        }

        if(!atLeastOneTrack)
        {
            Log.error(prefetchError);
            return false;
        } else
        {
            player.processError = null;
            return true;
        }
    }

    protected boolean doPrefetch2()
    {
        if(prefetchEnabled)
            synchronized(waitPrefetched)
            {
                source.doStart();
                try
                {
                    if(!waitPrefetched.isEmpty())
                        waitPrefetched.wait(3000L);
                }
                catch(InterruptedException e) { }
            }
        else
            prefetched = true;
        deallocated = false;
        return true;
    }

    protected synchronized void abortPrefetch()
    {
        timeBeforeAbortPrefetch = getMediaTime();
        doReset();
        int size = modules.size();
        for(int i = 0; i < size; i++)
        {
            StateTransistor m = (StateTransistor)modules.elementAt(i);
            m.abortPrefetch();
        }

        deallocated = true;
    }

    protected synchronized void doFailedPrefetch()
    {
        int size = modules.size();
        for(int i = 0; i < size; i++)
        {
            StateTransistor m = (StateTransistor)modules.elementAt(i);
            m.doFailedPrefetch();
        }

        super.doFailedPrefetch();
    }

    protected synchronized void doStart()
    {
        if(started)
        {
            return;
        } else
        {
            doStart1();
            doStart2();
            return;
        }
    }

    protected void doStart1()
    {
        if((dsource instanceof CaptureDevice) || isRTP())
            reset();
        resetPrefetchedList();
        resetStoppedList();
        resetEndedList();
        for(int i = 0; i < trackControls.length; i++)
            if(trackControls[i].isEnabled())
                trackControls[i].startTrack();

    }

    protected void doStart2()
    {
        source.doStart();
        started = true;
        prefetched = true;
    }

    public synchronized void stop()
    {
        super.stop();
        sendEvent(new StopByRequestEvent(this, 600, 500, getTargetState(), getMediaTime()));
    }

    protected synchronized void localStop()
    {
        super.stop();
    }

    protected synchronized void doStop()
    {
        if(!started)
        {
            return;
        } else
        {
            doStop1();
            doStop2();
            return;
        }
    }

    protected void doStop1()
    {
        resetPrefetchedList();
        source.doStop();
        for(int i = 0; i < trackControls.length; i++)
            if(trackControls[i].isEnabled())
                trackControls[i].stopTrack();

    }

    protected void doStop2()
    {
        if(!prefetchEnabled)
            source.pause();
        started = false;
    }

    public void setStopTime(Time t)
    {
        if(getState() < 300)
            throwError(new NotRealizedError("Cannot set stop time on an unrealized controller."));
        if(getStopTime() != null && getStopTime().getNanoseconds() != t.getNanoseconds())
            sendEvent(new StopTimeChangeEvent(this, t));
        if(getState() == 600 && t != Clock.RESET && t.getNanoseconds() < getMediaNanoseconds())
        {
            localStop();
            setStopTime(Clock.RESET);
            sendEvent(new StopAtTimeEvent(this, getState(), 500, getTargetState(), getMediaTime()));
        } else
        {
            getClock().setStopTime(t);
            int size = sinks.size();
            for(int i = 0; i < size; i++)
            {
                BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
                bsm.setStopTime(t);
            }

        }
    }

    protected void doDeallocate()
    {
    }

    protected synchronized void doClose()
    {
        if(modules == null)
        {
            if(source != null)
                source.doClose();
            return;
        }
        if(getState() == 600)
            localStop();
        if(getState() == 500)
            doReset();
        int size = modules.size();
        for(int i = 0; i < size; i++)
        {
            StateTransistor m = (StateTransistor)modules.elementAt(i);
            m.doClose();
        }

        if(needSavingDB)
        {
            Resource.saveDB();
            needSavingDB = false;
        }
    }

    public boolean isRTP()
    {
        if(testedRTP)
        {
            return rtpInfo != null;
        } else
        {
            rtpInfo = (RTPInfo)dsource.getControl("com.sun.media.util.RTPInfo");
            testedRTP = true;
            return rtpInfo != null;
        }
    }

    public String getCNAME()
    {
        if(rtpInfo == null && (rtpInfo = (RTPInfo)dsource.getControl("com.sun.media.util.RTPInfo")) == null)
            return null;
        else
            return rtpInfo.getCNAME();
    }

    public synchronized void setMediaTime(Time when)
    {
        if(super.state < 300)
            throwError(new NotRealizedError("Cannot set media time on a unrealized controller"));
        if(when.getNanoseconds() == getMediaNanoseconds())
        {
            return;
        } else
        {
            reset();
            timeBeforeAbortPrefetch = null;
            doSetMediaTime(when);
            doPrefetch();
            sendEvent(new MediaTimeSetEvent(this, when));
            return;
        }
    }

    protected void doSetMediaTime(Time when)
    {
        slaveClock.setMediaTime(when);
        Time t;
        if((t = source.setPosition(when, 0)) == null)
            t = when;
        int size = sinks.size();
        for(int i = 0; i < size; i++)
        {
            BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
            bsm.doSetMediaTime(when);
            bsm.setPreroll(when.getNanoseconds(), t.getNanoseconds());
        }

    }

    public synchronized float doSetRate(float r)
    {
        if(r <= 0.0F)
            r = 1.0F;
        if(r == rate)
            return r;
        if(masterSink == null)
            r = getClock().setRate(r);
        else
            r = masterSink.doSetRate(r);
        int size = modules.size();
        for(int i = 0; i < size; i++)
        {
            BasicModule m = (BasicModule)modules.elementAt(i);
            if(m != masterSink)
                m.doSetRate(r);
        }

        rate = r;
        return r;
    }

    protected synchronized void reset()
    {
        if(started || !prefetched || dataPathBlocked)
        {
            return;
        } else
        {
            doReset();
            return;
        }
    }

    protected synchronized void doReset()
    {
        synchronized(waitResetted)
        {
            resetResettedList();
            int size = modules.size();
            for(int i = size - 1; i >= 0; i--)
            {
                BasicModule m = (BasicModule)modules.elementAt(i);
                if(!m.prefetchFailed())
                    m.reset();
            }

            size = sinks.size();
            for(int i = 0; i < size; i++)
            {
                BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
                if(!bsm.prefetchFailed())
                    bsm.triggerReset();
            }

            if(!waitResetted.isEmpty())
                try
                {
                    waitResetted.wait(3000L);
                }
                catch(Exception e) { }
            size = sinks.size();
            for(int i = 0; i < size; i++)
            {
                BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
                if(!bsm.prefetchFailed())
                    bsm.doneReset();
            }

        }
        prefetched = false;
    }

    private void resetPrefetchedList()
    {
        synchronized(waitPrefetched)
        {
            waitPrefetched.removeAllElements();
            int size = sinks.size();
            for(int i = 0; i < size; i++)
            {
                BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
                if(!bsm.prefetchFailed())
                    waitPrefetched.addElement(bsm);
            }

            waitPrefetched.notifyAll();
        }
    }

    private void resetStoppedList()
    {
        synchronized(waitStopped)
        {
            waitStopped.removeAllElements();
            int size = sinks.size();
            for(int i = 0; i < size; i++)
            {
                BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
                if(!bsm.prefetchFailed())
                    waitStopped.addElement(bsm);
            }

            waitStopped.notifyAll();
        }
    }

    private void resetEndedList()
    {
        synchronized(waitEnded)
        {
            waitEnded.removeAllElements();
            int size = sinks.size();
            for(int i = 0; i < size; i++)
            {
                BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
                if(!bsm.prefetchFailed())
                    waitEnded.addElement(bsm);
            }

            waitEnded.notifyAll();
        }
    }

    private void resetResettedList()
    {
        synchronized(waitResetted)
        {
            waitResetted.removeAllElements();
            int size = sinks.size();
            for(int i = 0; i < size; i++)
            {
                BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
                if(!bsm.prefetchFailed())
                    waitResetted.addElement(bsm);
            }

            waitResetted.notifyAll();
        }
    }

    public void bufferPrefetched(Module src)
    {
        if(!prefetchEnabled)
            return;
        if(src instanceof BasicSinkModule)
            synchronized(waitPrefetched)
            {
                if(waitPrefetched.contains(src))
                    waitPrefetched.removeElement(src);
                if(waitPrefetched.isEmpty())
                {
                    waitPrefetched.notifyAll();
                    if(!prefetchLogged)
                    {
                        profile("prefetch", prefetchTime);
                        prefetchLogged = true;
                    }
                    if(getState() != 600 && getTargetState() != 600)
                        source.pause();
                    prefetched = true;
                }
            }
    }

    public void stopAtTime(Module src)
    {
        if(src instanceof BasicSinkModule)
            synchronized(waitStopped)
            {
                if(waitStopped.contains(src))
                    waitStopped.removeElement(src);
                if(waitStopped.isEmpty() || waitEnded.size() == 1 && waitEnded.contains(src))
                {
                    started = false;
                    stopControllerOnly();
                    setStopTime(Clock.RESET);
                    sendEvent(new StopAtTimeEvent(this, 600, 500, getTargetState(), getMediaTime()));
                    slaveClock.reset(USE_MASTER);
                } else
                if(src == masterSink)
                    slaveClock.reset(USE_BACKUP);
            }
    }

    public void mediaEnded(Module src)
    {
        if(src instanceof BasicSinkModule)
            synchronized(waitEnded)
            {
                if(waitEnded.contains(src))
                    waitEnded.removeElement(src);
                if(waitEnded.isEmpty())
                {
                    started = false;
                    stopControllerOnly();
                    sendEvent(new EndOfMediaEvent(this, 600, 500, getTargetState(), getMediaTime()));
                    slaveClock.reset(USE_MASTER);
                } else
                if(src == masterSink)
                    slaveClock.reset(USE_BACKUP);
            }
    }

    public void resetted(Module src)
    {
        synchronized(waitResetted)
        {
            if(waitResetted.contains(src))
                waitResetted.removeElement(src);
            if(waitResetted.isEmpty())
                waitResetted.notifyAll();
        }
    }

    public void dataBlocked(Module src, boolean blocked)
    {
        dataPathBlocked = blocked;
        if(blocked)
        {
            resetPrefetchedList();
            resetResettedList();
        }
        if(getTargetState() != 600)
            return;
        if(blocked)
        {
            localStop();
            setTargetState(600);
            sendEvent(new RestartingEvent(this, 600, 400, 600, getMediaTime()));
        } else
        {
            sendEvent(new StartEvent(this, 500, 600, 600, getMediaTime(), getTimeBase().getTime()));
        }
    }

    public void framesBehind(Module src, float frames, InputConnector ic)
    {
        for(; ic != null; ic = src.getInputConnector(null))
        {
            OutputConnector oc;
            if((oc = ic.getOutputConnector()) == null || (src = oc.getModule()) == null || !(src instanceof BasicFilterModule))
                break;
            BasicFilterModule bfm = (BasicFilterModule)src;
            bfm.setFramesBehind(frames);
        }

    }

    public void markedDataArrived(Module src, Buffer buffer)
    {
        if(src instanceof BasicSourceModule)
        {
            markedDataStartTime = getMediaNanoseconds();
        } else
        {
            long t = getMediaNanoseconds() - markedDataStartTime;
            if(t > 0L && t < 0x3b9aca00L)
            {
                if(!reportOnce)
                {
                    Log.comment("Computed latency for video: " + t / 0xf4240L + " ms\n");
                    reportOnce = true;
                }
                latency = (t + latency) / 2L;
            }
        }
    }

    public void formatChanged(Module src, Format oldFormat, Format newFormat)
    {
        Log.comment(src + ": input format changed: " + newFormat);
        if((src instanceof BasicRendererModule) && (oldFormat instanceof VideoFormat) && (newFormat instanceof VideoFormat))
        {
            Dimension s1 = ((VideoFormat)oldFormat).getSize();
            Dimension s2 = ((VideoFormat)newFormat).getSize();
            if(s2 != null && (s1 == null || !s1.equals(s2)))
                sendEvent(new SizeChangeEvent(this, s2.width, s2.height, 1.0F));
        }
    }

    public void formatChangedFailure(Module src, Format oldFormat, Format newFormat)
    {
        if(!internalErrorOccurred)
        {
            sendEvent(new InternalErrorEvent(this, "Internal module " + src + ": failed to handle a data format change!"));
            internalErrorOccurred = true;
            close();
        }
    }

    public void pluginTerminated(Module src)
    {
        if(!internalErrorOccurred)
        {
            sendEvent(new ControllerClosedEvent(this));
            internalErrorOccurred = true;
            close();
        }
    }

    public void internalErrorOccurred(Module src)
    {
        if(!internalErrorOccurred)
        {
            sendEvent(new InternalErrorEvent(this, "Internal module " + src + " failed!"));
            internalErrorOccurred = true;
            close();
        }
    }

    public boolean audioEnabled()
    {
        for(int i = 0; i < trackControls.length; i++)
            if(trackControls[i].isEnabled() && (trackControls[i].getOriginalFormat() instanceof AudioFormat))
                return true;

        return false;
    }

    public boolean videoEnabled()
    {
        for(int i = 0; i < trackControls.length; i++)
            if(trackControls[i].isEnabled() && (trackControls[i].getOriginalFormat() instanceof VideoFormat))
                return true;

        return false;
    }

    public Control[] getControls()
    {
        Vector cv = new Vector();
        int size = modules != null ? modules.size() : 0;
        int otherSize = 0;
        for(int i = 0; i < size; i++)
        {
            Module m = (Module)modules.elementAt(i);
            Object cs[] = m.getControls();
            if(cs != null)
            {
                for(int j = 0; j < cs.length; j++)
                    cv.addElement(cs[j]);

            }
        }

        size = cv.size();
        if(videoEnabled() && frameRateControl == null)
            frameRateControl = new FrameRateAdapter(player, 0.0F, 0.0F, 30F, false) {

                public float setFrameRate(float rate)
                {
                    super.value = rate;
                    return -1F;
                }

                public Component getControlComponent()
                {
                    return null;
                }

                public Object getOwner()
                {
                    return player;
                }

            }
;
        if(bitRateControl == null)
            bitRateControl = new BitRateA(0, -1, -1, false);
        if(frameRateControl != null)
            otherSize++;
        if(bitRateControl != null)
            otherSize++;
        if(framePositioningControl != null)
            otherSize++;
        if(DEBUG)
            otherSize++;
        Control controls[] = new Control[size + otherSize + trackControls.length];
        for(int i = 0; i < size; i++)
            controls[i] = (Control)cv.elementAt(i);

        if(bitRateControl != null)
            controls[size++] = bitRateControl;
        if(frameRateControl != null)
            controls[size++] = frameRateControl;
        if(framePositioningControl != null)
            controls[size++] = framePositioningControl;
        if(DEBUG)
            controls[size++] = jmd;
        for(int i = 0; i < trackControls.length; i++)
            controls[size + i] = trackControls[i];

        return controls;
    }

    public GainControl getGainControl()
    {
        return (GainControl)getControl("javax.media.GainControl");
    }

    public Component getVisualComponent()
    {
        Vector visuals = new Vector(1);
        if(modules == null)
            return null;
        for(int i = 0; i < modules.size(); i++)
        {
            BasicModule bm = (BasicModule)modules.elementAt(i);
            PlugIn pi = getPlugIn(bm);
            if(pi instanceof VideoRenderer)
            {
                Component comp = ((VideoRenderer)pi).getComponent();
                if(comp != null)
                    visuals.addElement(comp);
            }
        }

        if(visuals.size() == 0)
            return null;
        if(visuals.size() == 1)
            return (Component)visuals.elementAt(0);
        else
            return createVisualContainer(visuals);
    }

    protected Component createVisualContainer(Vector visuals)
    {
        Boolean hint = (Boolean)Manager.getHint(3);
        if(container == null)
        {
            if(hint == null || !hint.booleanValue())
                container = new HeavyPanel(visuals);
            else
                container = new LightPanel(visuals);
            container.setLayout(new FlowLayout());
            container.setBackground(Color.black);
            for(int i = 0; i < visuals.size(); i++)
            {
                Component c = (Component)visuals.elementAt(i);
                container.add(c);
                c.setSize(c.getPreferredSize());
            }

        }
        return container;
    }

    public Time getStartLatency()
    {
        if(super.state == 100 || super.state == 200)
            throwError(new NotRealizedError("Cannot get start latency from an unrealized controller"));
        return Controller.LATENCY_UNKNOWN;
    }

    public long getLatency()
    {
        return latency;
    }

    public Time getDuration()
    {
        return source.getDuration();
    }

    public void setProgressControl(ProgressControl p)
    {
        progressControl = p;
    }

    public void createProgressControl()
    {
        StringControl frameRate = new StringControlAdapter();
        frameRate.setValue(JMFI18N.getResource("mediaplayer.N/A"));
        StringControl bitRate = new StringControlAdapter();
        bitRate.setValue(JMFI18N.getResource("mediaplayer.N/A"));
        StringControl videoProps = new StringControlAdapter();
        videoProps.setValue(JMFI18N.getResource("mediaplayer.N/A"));
        StringControl audioProps = new StringControlAdapter();
        audioProps.setValue(JMFI18N.getResource("mediaplayer.N/A"));
        StringControl audioCodec = new StringControlAdapter();
        audioCodec.setValue(JMFI18N.getResource("mediaplayer.N/A"));
        StringControl videoCodec = new StringControlAdapter();
        videoCodec.setValue(JMFI18N.getResource("mediaplayer.N/A"));
        progressControl = new ProgressControlAdapter(frameRate, bitRate, videoProps, audioProps, videoCodec, audioCodec);
    }

    public void updateFormats()
    {
        for(int i = 0; i < trackControls.length; i++)
            trackControls[i].updateFormat();

    }

    public void updateRates()
    {
        if(getState() < 300)
            return;
        long now = System.currentTimeMillis();
        long rate;
        if(now == lastStatsTime)
            rate = lastBitRate;
        else
            rate = (long)((((double)getBitRate() * 8D) / (double)(now - lastStatsTime)) * 1000D);
        long avg = (lastBitRate + rate) / 2L;
        if(bitRateControl != null)
            bitRateControl.setBitRate((int)avg);
        lastBitRate = rate;
        lastStatsTime = now;
        resetBitRate();
        for(int i = 0; i < trackControls.length; i++)
            trackControls[i].updateRates(now);

        source.checkLatency();
    }

    protected long getBitRate()
    {
        return source.getBitsRead();
    }

    protected void resetBitRate()
    {
        source.resetBitsRead();
    }

    public void setTimeBase(TimeBase tb)
        throws IncompatibleTimeBaseException
    {
        getClock().setTimeBase(tb);
        if(sinks == null)
            return;
        int size = sinks.size();
        for(int i = 0; i < size; i++)
        {
            BasicSinkModule bsm = (BasicSinkModule)sinks.elementAt(i);
            bsm.setTimeBase(tb);
        }

    }

    public TimeBase getTimeBase()
    {
        return getClock().getTimeBase();
    }

    protected GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node)
    {
        BasicModule src = null;
        BasicModule dst = null;
        InputConnector ic = null;
        OutputConnector oc = null;
        boolean lastNode = true;
        Vector used = new Vector(5);
        int indent = 0;
        if(node.plugin == null)
            return null;
        Log.setIndent(indent++);
        for(; node != null && node.plugin != null; node = node.prev)
        {
            if((src = createModule(node, used)) == null)
            {
                Log.error("Internal error: buildTrackFromGraph");
                node.failed = true;
                return node;
            }
            if(lastNode)
            {
                if(src instanceof BasicRendererModule)
                {
                    tc.rendererModule = (BasicRendererModule)src;
                    if(useMoreRenderBuffer && (tc.rendererModule.getRenderer() instanceof AudioRenderer))
                        setRenderBufferSize(tc.rendererModule.getRenderer());
                } else
                if(src instanceof BasicFilterModule)
                {
                    tc.lastOC = src.getOutputConnector(null);
                    tc.lastOC.setFormat(node.output);
                }
                lastNode = false;
            }
            ic = src.getInputConnector(null);
            ic.setFormat(node.input);
            if(dst != null)
            {
                oc = src.getOutputConnector(null);
                ic = dst.getInputConnector(null);
                oc.setFormat(ic.getFormat());
            }
            src.setController(this);
            if(!src.doRealize())
            {
                Log.setIndent(indent--);
                node.failed = true;
                return node;
            }
            if(oc != null && ic != null)
                connectModules(oc, ic, dst);
            dst = src;
        }

        dst = src;
        do
        {
            dst.setModuleListener(this);
            modules.addElement(dst);
            tc.modules.addElement(dst);
            if(dst instanceof BasicFilterModule)
                filters.addElement(dst);
            else
            if(dst instanceof BasicSinkModule)
                sinks.addElement(dst);
            oc = dst.getOutputConnector(null);
        } while(oc != null && (ic = oc.getInputConnector()) != null && (dst = (BasicModule)ic.getModule()) != null);
        tc.firstOC.setFormat(tc.getOriginalFormat());
        ic = src.getInputConnector(null);
        Format fmt = ic.getFormat();
        if(fmt == null || !fmt.equals(tc.getOriginalFormat()))
            ic.setFormat(tc.getOriginalFormat());
        connectModules(tc.firstOC, ic, src);
        Log.setIndent(indent--);
        return null;
    }

    protected void setRenderBufferSize(Renderer r)
    {
        BufferControl bc = (BufferControl)r.getControl("javax.media.control.BufferControl");
        if(bc != null)
            bc.setBufferLength(2000L);
    }

    protected BasicModule lastModule(BasicModule bm)
    {
        InputConnector inputconnector;
        for(OutputConnector oc = bm.getOutputConnector(null); oc != null && (inputconnector = oc.getInputConnector()) != null; oc = bm.getOutputConnector(null))
            bm = (BasicModule)inputconnector.getModule();

        return bm;
    }

    protected BasicModule createModule(GraphNode n, Vector used)
    {
        BasicModule m = null;
        if(n.plugin == null)
            return null;
        PlugIn p;
        if(used.contains(n.plugin))
        {
            if(n.cname == null || (p = SimpleGraphBuilder.createPlugIn(n.cname, -1)) == null)
            {
                Log.write("Failed to instantiate " + n.cname);
                return null;
            }
        } else
        {
            p = n.plugin;
            used.addElement(p);
        }
        if((n.type == -1 || n.type == 4) && (p instanceof Renderer))
            m = new BasicRendererModule((Renderer)p);
        else
        if((n.type == -1 || n.type == 2) && (p instanceof Codec))
            m = new BasicFilterModule((Codec)p);
        if(DEBUG && m != null)
            m.setJMD(jmd);
        return m;
    }

    protected void connectModules(OutputConnector oc, InputConnector ic, BasicModule dst)
    {
        if(dst instanceof BasicRendererModule)
            oc.setProtocol(ic.getProtocol());
        else
            ic.setProtocol(oc.getProtocol());
        oc.connectTo(ic, ic.getFormat());
    }

    static boolean isRawVideo(Format fmt)
    {
        return (fmt instanceof RGBFormat) || (fmt instanceof YUVFormat);
    }

    void traceGraph(BasicModule source)
    {
        String names[] = source.getOutputConnectorNames();
        for(int i = 0; i < names.length; i++)
        {
            OutputConnector oc = source.getOutputConnector(names[i]);
            Module m;
            InputConnector ic;
            if((ic = oc.getInputConnector()) != null && (m = ic.getModule()) != null)
            {
                Log.write("  " + getPlugIn(source));
                Log.write("     connects to: " + getPlugIn((BasicModule)m));
                Log.write("     format: " + oc.getFormat());
                traceGraph((BasicModule)m);
            }
        }

    }

    protected PlugIn getPlugIn(BasicModule m)
    {
        if(m instanceof BasicSourceModule)
            return ((BasicSourceModule)m).getDemultiplexer();
        if(m instanceof BasicFilterModule)
            return ((BasicFilterModule)m).getCodec();
        if(m instanceof BasicRendererModule)
            return ((BasicRendererModule)m).getRenderer();
        else
            return null;
    }

    static void profile(String msg, long time)
    {
        Log.profile("Profile: " + msg + ": " + (System.currentTimeMillis() - time) + " ms\n");
    }

    protected BasicPlayer player;
    protected DataSource dsource;
    protected Vector modules;
    protected Vector filters;
    protected Vector sinks;
    protected Vector waitPrefetched;
    protected Vector waitStopped;
    protected Vector waitEnded;
    protected Vector waitResetted;
    protected Track tracks[];
    protected Demultiplexer parser;
    protected BasicSinkModule masterSink;
    protected BasicSourceModule source;
    protected SlaveClock slaveClock;
    private boolean internalErrorOccurred;
    protected boolean prefetched;
    protected boolean started;
    private boolean dataPathBlocked;
    private boolean useMoreRenderBuffer;
    private boolean deallocated;
    public boolean prefetchEnabled;
    protected static boolean needSavingDB = false;
    private Time timeBeforeAbortPrefetch;
    private float rate;
    protected BitRateControl bitRateControl;
    protected FrameRateControl frameRateControl;
    protected FramePositioningControl framePositioningControl;
    private long latency;
    static boolean DEBUG = true;
    protected JMD jmd;
    protected Container container;
    public static boolean TRACE_ON = false;
    protected BasicTrackControl trackControls[];
    protected ProgressControl progressControl;
    private long realizeTime;
    private long prefetchTime;
    static String NOT_CONFIGURED_ERROR = "cannot be called before configured";
    static String NOT_REALIZED_ERROR = "cannot be called before realized";
    static String STARTED_ERROR = "cannot be called after started";
    String configError;
    String configIntError;
    String configInt2Error;
    String parseError;
    protected String realizeError;
    protected String timeBaseError;
    protected String genericProcessorError;
    String prefetchError;
    RTPInfo rtpInfo;
    boolean testedRTP;
    boolean prefetchLogged;
    long markedDataStartTime;
    boolean reportOnce;
    long lastBitRate;
    long lastStatsTime;
    static boolean USE_MASTER = true;
    static boolean USE_BACKUP = false;

    static 
    {
        try
        {
            Toolkit.getDefaultToolkit();
        }
        catch(Throwable t)
        {
            DEBUG = false;
        }
    }
}
