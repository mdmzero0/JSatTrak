// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Handler.java

package com.sun.media.content.rtp;

import com.sun.media.BasicController;
import com.sun.media.BasicPlayer;
import com.sun.media.JMFSecurity;
import com.sun.media.Log;
import com.sun.media.protocol.BufferListener;
import com.sun.media.rtp.RTPMediaLocator;
import com.sun.media.rtp.RTPSessionMgr;
import java.awt.Component;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Vector;
import javax.media.Clock;
import javax.media.ClockStartedError;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NotRealizedError;
import javax.media.Owned;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.SystemTimeBase;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import javax.media.format.FormatChangeEvent;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.RTPPushDataSource;
import javax.media.rtp.RTPSocket;
import javax.media.rtp.RTPStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.RTPEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;

public class Handler extends BasicPlayer
    implements ReceiveStreamListener, BufferListener
{
    class BC
        implements BufferControl, Owned
    {

        public long getBufferLength()
        {
            if(len < 0L)
                return prebuffer ? 750L : 125L;
            else
                return len;
        }

        public long setBufferLength(long time)
        {
            len = time;
            Log.comment("RTP Handler buffer length set: " + len);
            return len;
        }

        public long getMinimumThreshold()
        {
            if(min < 0L)
                return prebuffer ? 125L : 0L;
            else
                return min;
        }

        public long setMinimumThreshold(long time)
        {
            min = time;
            Log.comment("RTP Handler buffer minimum threshold: " + min);
            return min;
        }

        public void setEnabledThreshold(boolean flag)
        {
        }

        public boolean getEnabledThreshold()
        {
            return getMinimumThreshold() > 0L;
        }

        public Component getControlComponent()
        {
            return null;
        }

        public Object getOwner()
        {
            return Handler.this;
        }

        long len;
        long min;

        BC()
        {
            len = -1L;
            min = -1L;
        }
    }

    class PlayerListener
        implements ControllerListener
    {

        public synchronized void controllerUpdate(ControllerEvent ce)
        {
            Player p = (Player)ce.getSourceController();
            if(p == null)
                return;
            int idx;
            for(idx = 0; idx < players.length; idx++)
                if(players[idx] == p)
                    break;

            if(idx >= players.length)
            {
                System.err.println("Unknown player: " + p);
                return;
            }
            if(ce instanceof RealizeCompleteEvent)
            {
                if(formatChanged[idx] != null)
                    try
                    {
                        invalidateComp();
                        FormatChangeEvent f = new FormatChangeEvent(handler, formats[idx], formatChanged[idx]);
                        handler.sendMyEvent(f);
                        formats[idx] = formatChanged[idx];
                        formatChanged[idx] = null;
                    }
                    catch(Exception e)
                    {
                        e.getMessage();
                    }
                realized[idx] = true;
                for(int i = 0; i < realized.length; i++)
                    if(!realized[i])
                        return;

                synchronized(realizedSync)
                {
                    playersRealized = true;
                    realizedSync.notifyAll();
                }
            }
            if(ce instanceof ControllerErrorEvent)
            {
                players[idx].removeControllerListener(this);
                Log.error("RTP Handler internal error: " + ce);
                players[idx] = null;
            }
        }

        Handler handler;

        public PlayerListener(Handler handler)
        {
            this.handler = handler;
        }
    }

    class StateWaiter
        implements ControllerListener
    {

        public void waitForStart(Player p, boolean startOn)
        {
            p.addControllerListener(this);
            if(startOn)
                p.start();
            else
                p.stop();
            synchronized(stateLock)
            {
                while((startOn && p.getState() != 600 || !startOn && p.getState() == 600) && !closeDown) 
                {
                    try
                    {
                        stateLock.wait(1000L);
                        continue;
                    }
                    catch(InterruptedException ie) { }
                    break;
                }
            }
            p.removeControllerListener(this);
        }

        public void waitForClose(Player p)
        {
            p.addControllerListener(this);
            p.close();
            synchronized(stateLock)
            {
                while(!closeDown) 
                {
                    try
                    {
                        stateLock.wait(1000L);
                        continue;
                    }
                    catch(InterruptedException ie) { }
                    break;
                }
            }
            p.removeControllerListener(this);
        }

        public void controllerUpdate(ControllerEvent ce)
        {
            if((ce instanceof ControllerClosedEvent) || (ce instanceof ControllerErrorEvent))
                closeDown = true;
            synchronized(stateLock)
            {
                stateLock.notify();
            }
        }

        boolean closeDown;
        Object stateLock;

        StateWaiter()
        {
            closeDown = false;
            stateLock = new Object();
        }
    }


    public Handler()
    {
        mgrs = null;
        sources = null;
        players = null;
        formats = null;
        formatChanged = null;
        realized = null;
        dataReady = null;
        locators = null;
        listener = new PlayerListener(this);
        playersRealized = false;
        realizedSync = new Object();
        closeSync = new Object();
        dataSync = new Object();
        stateLock = new Object();
        closed = false;
        audioEnabled = false;
        videoEnabled = false;
        prebuffer = false;
        dataAllReady = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        sessionError = "cannot create and initialize the RTP session.";
        super.framePositioning = false;
        super.bufferControl = new BC();
        super.stopThreadEnabled = true;
    }

    protected boolean doRealize()
    {
        super.doRealize();
        try
        {
            if(super.source instanceof RTPSocket)
            {
                mgrs = new RTPSessionMgr[1];
                mgrs[1] = new RTPSessionMgr((RTPSocket)super.source);
                mgrs[1].addReceiveStreamListener(this);
                sources = new DataSource[1];
                players = new Player[1];
                formats = new Format[1];
                realized = new boolean[1];
                dataReady = new boolean[1];
                formatChanged = new Format[1];
                sources[0] = super.source;
                dataReady[0] = false;
            } else
            {
                SessionAddress localAddr = new SessionAddress();
                mgrs = new RTPSessionMgr[locators.size()];
                sources = new DataSource[locators.size()];
                players = new Player[locators.size()];
                formats = new Format[locators.size()];
                realized = new boolean[locators.size()];
                dataReady = new boolean[locators.size()];
                formatChanged = new Format[locators.size()];
                for(int i = 0; i < locators.size(); i++)
                {
                    RTPMediaLocator rml = (RTPMediaLocator)locators.elementAt(i);
                    realized[i] = false;
                    mgrs[i] = (RTPSessionMgr)RTPManager.newInstance();
                    mgrs[i].addReceiveStreamListener(this);
                    InetAddress ipAddr = InetAddress.getByName(rml.getSessionAddress());
                    SessionAddress destAddr;
                    if(ipAddr.isMulticastAddress())
                    {
                        localAddr = new SessionAddress(ipAddr, rml.getSessionPort(), rml.getTTL());
                        destAddr = new SessionAddress(ipAddr, rml.getSessionPort(), rml.getTTL());
                    } else
                    {
                        localAddr = new SessionAddress(InetAddress.getLocalHost(), rml.getSessionPort());
                        destAddr = new SessionAddress(ipAddr, rml.getSessionPort());
                    }
                    mgrs[i].initialize(localAddr);
                    if(prebuffer)
                    {
                        BufferControl bc = (BufferControl)mgrs[i].getControl("javax.media.control.BufferControl");
                        bc.setBufferLength(super.bufferControl.getBufferLength());
                        bc.setMinimumThreshold(super.bufferControl.getMinimumThreshold());
                    }
                    mgrs[i].addTarget(destAddr);
                }

            }
        }
        catch(Exception e)
        {
            Log.error("Cannot create the RTP Session: " + e.getMessage());
            super.processError = sessionError;
            return false;
        }
        try
        {
            synchronized(realizedSync)
            {
                for(; !playersRealized && !isInterrupted() && !closed; realizedSync.wait());
            }
        }
        catch(Exception e) { }
        if(closed || isInterrupted())
        {
            resetInterrupt();
            super.processError = "no RTP data was received.";
            return false;
        } else
        {
            return true;
        }
    }

    protected void completeRealize()
    {
        super.state = 300;
        super.completeRealize();
    }

    protected void doStart()
    {
        super.doStart();
        synchronized(dataSync)
        {
            if(prebuffer)
            {
                dataAllReady = false;
                for(int i = 0; i < dataReady.length; i++)
                {
                    dataReady[i] = false;
                    ((com.sun.media.protocol.rtp.DataSource)sources[i]).flush();
                    ((com.sun.media.protocol.rtp.DataSource)sources[i]).prebuffer();
                }

                if(!dataAllReady && !closed)
                    try
                    {
                        dataSync.wait(3000L);
                    }
                    catch(Exception e) { }
            }
        }
        for(int i = 0; i < players.length; i++)
            try
            {
                if(players[i] != null)
                    waitForStart(players[i]);
            }
            catch(Exception e) { }

    }

    protected void doStop()
    {
        super.doStop();
        synchronized(dataSync)
        {
            if(prebuffer)
                dataSync.notify();
        }
        for(int i = 0; i < players.length; i++)
            try
            {
                if(players[i] != null)
                    waitForStop(players[i]);
            }
            catch(Exception e) { }

    }

    protected void doDeallocate()
    {
        for(int i = 0; i < players.length; i++)
            try
            {
                if(players[i] != null)
                    players[i].deallocate();
            }
            catch(Exception e) { }

        synchronized(realizedSync)
        {
            realizedSync.notify();
        }
    }

    protected void doFailedRealize()
    {
        synchronized(closeSync)
        {
            for(int i = 0; i < mgrs.length; i++)
                if(mgrs[i] != null)
                {
                    mgrs[i].removeTargets("Closing session from the RTP Handler");
                    mgrs[i].dispose();
                    mgrs[i] = null;
                }

        }
        super.doFailedRealize();
    }

    protected void doClose()
    {
        closed = true;
        synchronized(realizedSync)
        {
            realizedSync.notify();
        }
        synchronized(dataSync)
        {
            dataSync.notifyAll();
        }
        stop();
        for(int i = 0; i < players.length; i++)
            try
            {
                if(players[i] != null)
                    players[i].close();
            }
            catch(Exception e) { }

        synchronized(closeSync)
        {
            for(int i = 0; i < mgrs.length; i++)
                if(mgrs[i] != null)
                {
                    mgrs[i].removeTargets("Closing session from the RTP Handler");
                    mgrs[i].dispose();
                    mgrs[i] = null;
                }

        }
        super.doClose();
    }

    public void setTimeBase(TimeBase timebase)
        throws IncompatibleTimeBaseException
    {
    }

    protected TimeBase getMasterTimeBase()
    {
        return new SystemTimeBase();
    }

    public float setRate(float rate)
    {
        if(getState() < 300)
            throwError(new NotRealizedError("Cannot set rate on an unrealized Player."));
        return 1.0F;
    }

    public void setStopTime(Time t)
    {
        controllerSetStopTime(t);
    }

    protected void stopAtTime()
    {
        controllerStopAtTime();
    }

    public synchronized void addController(Controller newController)
        throws IncompatibleTimeBaseException
    {
        int playerState = getState();
        if(playerState == 600)
            throwError(new ClockStartedError("Cannot add controller to a started player"));
        if(playerState == 100 || playerState == 200)
            throwError(new NotRealizedError("A Controller cannot be added to an Unrealized Player"));
        throw new IncompatibleTimeBaseException();
    }

    protected boolean audioEnabled()
    {
        return audioEnabled;
    }

    protected boolean videoEnabled()
    {
        return videoEnabled;
    }

    private void sendMyEvent(ControllerEvent e)
    {
        super.sendEvent(e);
    }

    public void update(ReceiveStreamEvent event)
    {
        RTPSessionMgr mgr = (RTPSessionMgr)event.getSource();
        int idx;
        for(idx = 0; idx < mgrs.length; idx++)
            if(mgrs[idx] == mgr)
                break;

        if(idx >= mgrs.length)
        {
            System.err.println("Unknown manager: " + mgr);
            return;
        }
        if(event instanceof RemotePayloadChangeEvent)
        {
            Log.comment("Received an RTP PayloadChangeEvent");
            RTPControl ctl = (RTPControl)sources[idx].getControl("javax.media.rtp.RTPControl");
            if(ctl != null)
                formatChanged[idx] = ctl.getFormat();
            if(players[idx] != null)
            {
                stop();
                waitForClose(players[idx]);
            }
            try
            {
                sources[idx].connect();
                players[idx] = Manager.createPlayer(sources[idx]);
                if(players[idx] == null)
                {
                    Log.error("Could not create player for the new RTP payload.");
                    return;
                }
                players[idx].addControllerListener(listener);
                players[idx].realize();
            }
            catch(Exception e)
            {
                Log.error("Could not create player for the new payload.");
            }
        }
        if(event instanceof NewReceiveStreamEvent)
        {
            if(players[idx] != null)
                return;
            javax.media.rtp.ReceiveStream stream = null;
            try
            {
                stream = ((NewReceiveStreamEvent)event).getReceiveStream();
                sources[idx] = stream.getDataSource();
                RTPControl ctl = (RTPControl)sources[idx].getControl("javax.media.rtp.RTPControl");
                if(ctl != null)
                {
                    formats[idx] = ctl.getFormat();
                    if(formats[idx] instanceof AudioFormat)
                        audioEnabled = true;
                    else
                    if(formats[idx] instanceof VideoFormat)
                        videoEnabled = true;
                }
                if(super.source instanceof RTPSocket)
                    ((RTPSocket)super.source).setChild(sources[idx]);
                else
                    ((com.sun.media.protocol.rtp.DataSource)super.source).setChild((com.sun.media.protocol.rtp.DataSource)sources[idx]);
                players[idx] = Manager.createPlayer(sources[idx]);
                if(players[idx] == null)
                    return;
                players[idx].addControllerListener(listener);
                players[idx].realize();
                if(prebuffer)
                    ((com.sun.media.protocol.rtp.DataSource)sources[idx]).setBufferListener(this);
            }
            catch(Exception e)
            {
                Log.error("NewReceiveStreamEvent exception " + e.getMessage());
                return;
            }
        }
    }

    private void waitForStart(Player p)
    {
        (new StateWaiter()).waitForStart(p, true);
    }

    private void waitForStop(Player p)
    {
        (new StateWaiter()).waitForStart(p, false);
    }

    private void waitForClose(Player p)
    {
        (new StateWaiter()).waitForClose(p);
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        super.setSource(source);
        if(source instanceof com.sun.media.protocol.rtp.DataSource)
        {
            MediaLocator ml = source.getLocator();
            String mlStr = ml.getRemainder();
            int start;
            for(start = 0; mlStr.charAt(start) == '/'; start++);
            locators = new Vector();
            try
            {
                String str;
                int i;
                RTPMediaLocator rml;
                for(; start < mlStr.length() && (i = mlStr.indexOf("&", start)) != -1; start = i + 1)
                {
                    str = mlStr.substring(start, i);
                    rml = new RTPMediaLocator("rtp://" + str);
                    locators.addElement(rml);
                }

                if(start != 0)
                    str = mlStr.substring(start);
                else
                    str = mlStr;
                rml = new RTPMediaLocator("rtp://" + str);
                locators.addElement(rml);
            }
            catch(Exception e)
            {
                throw new IncompatibleSourceException();
            }
            if(locators.size() > 1)
                prebuffer = true;
        } else
        if(!(source instanceof RTPSocket))
            throw new IncompatibleSourceException();
        RTPControl ctl = (RTPControl)source.getControl("javax.media.rtp.RTPControl");
        if(ctl != null)
            ctl.addFormat(new AudioFormat("dvi/rtp", 44100D, 4, 1), 18);
    }

    private void invalidateComp()
    {
        super.controlComp = null;
        super.controls = null;
    }

    public Component getVisualComponent()
    {
        super.getVisualComponent();
        for(int i = 0; i < players.length; i++)
            if(players[i] != null && players[i].getVisualComponent() != null)
                return players[i].getVisualComponent();

        return null;
    }

    public Control[] getControls()
    {
        if(super.controls != null)
            return super.controls;
        Vector cv = new Vector();
        if(super.cachingControl != null)
            cv.addElement(super.cachingControl);
        if(super.bufferControl != null)
            cv.addElement(super.bufferControl);
        int size = players.length;
        for(int i = 0; i < size; i++)
        {
            Controller ctrller = players[i];
            Object cs[] = ctrller.getControls();
            if(cs != null)
            {
                for(int j = 0; j < cs.length; j++)
                    cv.addElement(cs[j]);

            }
        }

        size = cv.size();
        Control ctrls[] = new Control[size];
        for(int i = 0; i < size; i++)
            ctrls[i] = (Control)cv.elementAt(i);

        if(getState() >= 300)
            super.controls = ctrls;
        return ctrls;
    }

    public void updateStats()
    {
        for(int i = 0; i < players.length; i++)
            if(players[i] != null)
                ((BasicPlayer)players[i]).updateStats();

    }

    public void minThresholdReached(DataSource ds)
    {
        boolean ready = true;
        synchronized(dataSync)
        {
            for(int i = 0; i < sources.length; i++)
                if(sources[i] == ds)
                    dataReady[i] = true;
                else
                if(!dataReady[i])
                    ready = false;

            if(!ready)
                return;
            dataAllReady = true;
            dataSync.notify();
        }
    }

    RTPSessionMgr mgrs[];
    DataSource sources[];
    Player players[];
    Format formats[];
    Format formatChanged[];
    boolean realized[];
    boolean dataReady[];
    Vector locators;
    ControllerListener listener;
    boolean playersRealized;
    Object realizedSync;
    Object closeSync;
    Object dataSync;
    Object stateLock;
    private boolean closed;
    private boolean audioEnabled;
    private boolean videoEnabled;
    private boolean prebuffer;
    private boolean dataAllReady;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    String sessionError;




}
