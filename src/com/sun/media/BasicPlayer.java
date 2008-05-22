// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicPlayer.java

package com.sun.media;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.controls.SliderRegionControl;
import com.sun.media.controls.SliderRegionControlAdapter;
import com.sun.media.ui.DefaultControlPanel;
import com.sun.media.util.JMFI18N;
import com.sun.media.util.LoopThread;
import com.sun.media.util.jdk12;
import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;
import javax.media.*;
import javax.media.control.BufferControl;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media:
//            BasicController, PlayThread, StatsThread, SeekFailedEvent, 
//            Log, JMFSecurity, CreateWorkThreadAction, JMFSecurityManager

public abstract class BasicPlayer extends BasicController
    implements Player, ControllerListener, DownloadProgressListener
{

    public BasicPlayer()
    {
        source = null;
        controllerList = new Vector();
        optionalControllerList = new Vector();
        removedControllerList = new Vector();
        currentControllerList = new Vector();
        potentialEventsList = null;
        receivedEventList = new Vector();
        receivedAllEvents = false;
        configureEventList = new Vector();
        realizeEventList = new Vector();
        prefetchEventList = new Vector();
        stopEventList = new Vector();
        CachingControlEvent = null;
        restartFrom = null;
        eomEventsReceivedFrom = new Vector();
        stopAtTimeReceivedFrom = new Vector();
        playThread = null;
        statsThread = null;
        duration = Duration.DURATION_UNKNOWN;
        aboutToRestart = false;
        closing = false;
        prefetchFailed = false;
        framePositioning = true;
        controls = null;
        controlComp = null;
        regionControl = null;
        cachingControl = null;
        extendedCachingControl = null;
        bufferControl = null;
        startSync = new Object();
        mediaTimeSync = new Object();
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        lastTime = 0L;
        configureEventList.addElement("javax.media.ConfigureCompleteEvent");
        configureEventList.addElement("javax.media.ResourceUnavailableEvent");
        realizeEventList.addElement("javax.media.RealizeCompleteEvent");
        realizeEventList.addElement("javax.media.ResourceUnavailableEvent");
        prefetchEventList.addElement("javax.media.PrefetchCompleteEvent");
        prefetchEventList.addElement("javax.media.ResourceUnavailableEvent");
        stopEventList.addElement("javax.media.StopEvent");
        stopEventList.addElement("javax.media.StopByRequestEvent");
        stopEventList.addElement("javax.media.StopAtTimeEvent");
        super.stopThreadEnabled = false;
    }

    public boolean isFramePositionable()
    {
        return framePositioning;
    }

    protected boolean isConfigurable()
    {
        return false;
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        this.source = source;
        try
        {
            cachingControl = (CachingControl)source.getControl("javax.media.CachingControl");
            if(cachingControl != null && (cachingControl instanceof ExtendedCachingControl))
            {
                extendedCachingControl = (ExtendedCachingControl)cachingControl;
                if(extendedCachingControl != null)
                {
                    regionControl = new SliderRegionControlAdapter();
                    extendedCachingControl.addDownloadProgressListener(this, 100);
                }
            }
        }
        catch(ClassCastException e) { }
    }

    public void downloadUpdate()
    {
        if(extendedCachingControl == null)
            return;
        sendEvent(new CachingControlEvent(this, cachingControl, cachingControl.getContentProgress()));
        if(regionControl == null)
            return;
        long contentLength = cachingControl.getContentLength();
        int maxValuePercent;
        if(contentLength == -1L || contentLength <= 0L)
        {
            maxValuePercent = 0;
        } else
        {
            long endOffset = extendedCachingControl.getEndOffset();
            maxValuePercent = (int)((100D * (double)endOffset) / (double)contentLength);
            if(maxValuePercent < 0)
                maxValuePercent = 0;
            else
            if(maxValuePercent > 100)
                maxValuePercent = 100;
        }
        regionControl.setMinValue(0L);
        regionControl.setMaxValue(maxValuePercent);
    }

    public MediaLocator getMediaLocator()
    {
        if(source != null)
            return source.getLocator();
        else
            return null;
    }

    public String getContentType()
    {
        if(source != null)
            return source.getContentType();
        else
            return null;
    }

    protected DataSource getSource()
    {
        return source;
    }

    protected void doClose()
    {
        synchronized(this)
        {
            closing = true;
            notifyAll();
        }
        if(getState() == 600)
            stop(0);
        if(controllerList != null)
        {
            Controller c;
            for(; !controllerList.isEmpty(); controllerList.removeElement(c))
            {
                c = (Controller)controllerList.firstElement();
                c.close();
            }

        }
        if(controlComp != null)
            ((DefaultControlPanel)controlComp).dispose();
        controlComp = null;
        if(statsThread != null)
            statsThread.kill();
        sendEvent(new ControllerClosedEvent(this));
    }

    public void setTimeBase(TimeBase tb)
        throws IncompatibleTimeBaseException
    {
        TimeBase oldTimeBase = getMasterTimeBase();
        if(tb == null)
            tb = oldTimeBase;
        Controller c = null;
        if(controllerList != null)
            try
            {
                for(int i = controllerList.size(); --i >= 0;)
                {
                    c = (Controller)controllerList.elementAt(i);
                    c.setTimeBase(tb);
                }

            }
            catch(IncompatibleTimeBaseException e)
            {
                for(int i = controllerList.size(); --i >= 0;)
                {
                    Controller cx = (Controller)controllerList.elementAt(i);
                    if(cx == c)
                        break;
                    cx.setTimeBase(oldTimeBase);
                }

                Log.dumpStack(e);
                throw e;
            }
        super.setTimeBase(tb);
    }

    protected void setMediaLength(long t)
    {
        duration = new Time(t);
        super.setMediaLength(t);
    }

    public Time getDuration()
    {
        long t;
        if((t = getMediaNanoseconds()) > lastTime)
        {
            lastTime = t;
            updateDuration();
        }
        return duration;
    }

    protected synchronized void updateDuration()
    {
        Time oldDuration = duration;
        duration = Duration.DURATION_UNKNOWN;
        for(int i = 0; i < controllerList.size(); i++)
        {
            Controller c = (Controller)controllerList.elementAt(i);
            Time dur = c.getDuration();
            if(dur.equals(Duration.DURATION_UNKNOWN))
            {
                if(c instanceof BasicController)
                    continue;
                duration = Duration.DURATION_UNKNOWN;
                break;
            }
            if(dur.equals(Duration.DURATION_UNBOUNDED))
            {
                duration = Duration.DURATION_UNBOUNDED;
                break;
            }
            if(duration.equals(Duration.DURATION_UNKNOWN))
                duration = dur;
            else
            if(duration.getNanoseconds() < dur.getNanoseconds())
                duration = dur;
        }

        if(duration.getNanoseconds() != oldDuration.getNanoseconds())
        {
            setMediaLength(duration.getNanoseconds());
            sendEvent(new DurationUpdateEvent(this, duration));
        }
    }

    public Time getStartLatency()
    {
        super.getStartLatency();
        long t = 0L;
        for(int i = 0; i < controllerList.size(); i++)
        {
            Controller c = (Controller)controllerList.elementAt(i);
            Time latency = c.getStartLatency();
            if(latency != Controller.LATENCY_UNKNOWN && latency.getNanoseconds() > t)
                t = latency.getNanoseconds();
        }

        if(t == 0L)
            return Controller.LATENCY_UNKNOWN;
        else
            return new Time(t);
    }

    protected void stopAtTime()
    {
    }

    protected void controllerStopAtTime()
    {
        super.stopAtTime();
    }

    public void setStopTime(Time t)
    {
        if(super.state < 300)
            throwError(new NotRealizedError("Cannot set stop time on an unrealized controller."));
        if(getClock().getStopTime() == null || getClock().getStopTime().getNanoseconds() != t.getNanoseconds())
            sendEvent(new StopTimeChangeEvent(this, t));
        doSetStopTime(t);
    }

    private void doSetStopTime(Time t)
    {
        getClock().setStopTime(t);
        Vector list = controllerList;
        for(int i = list.size(); --i >= 0;)
        {
            Controller c = (Controller)controllerList.elementAt(i);
            c.setStopTime(t);
        }

    }

    protected void controllerSetStopTime(Time t)
    {
        super.setStopTime(t);
    }

    public final void setMediaTime(Time now)
    {
        if(super.state < 300)
            throwError(new NotRealizedError(BasicController.MediaTimeError));
        synchronized(mediaTimeSync)
        {
            if(syncStartInProgress())
                return;
            if(getState() == 600)
            {
                aboutToRestart = true;
                stop(2);
            }
            if(source instanceof Positionable)
                now = ((Positionable)source).setPosition(now, 2);
            super.setMediaTime(now);
            for(int i = controllerList.size(); --i >= 0;)
                ((Controller)controllerList.elementAt(i)).setMediaTime(now);

            doSetMediaTime(now);
            if(aboutToRestart)
            {
                syncStart(getTimeBase().getTime());
                aboutToRestart = false;
            }
        }
    }

    public boolean isAboutToRestart()
    {
        return aboutToRestart;
    }

    protected void doSetMediaTime(Time time)
    {
    }

    public Component getVisualComponent()
    {
        int state = getState();
        if(state < 300)
            throwError(new NotRealizedError("Cannot get visual component on an unrealized player"));
        return null;
    }

    public Component getControlPanelComponent()
    {
        int state = getState();
        if(state < 300)
            throwError(new NotRealizedError("Cannot get control panel component on an unrealized player"));
        if(controlComp == null)
            controlComp = new DefaultControlPanel(this);
        return controlComp;
    }

    public GainControl getGainControl()
    {
        int state = getState();
        if(state < 300)
            throwError(new NotRealizedError("Cannot get gain control on an unrealized player"));
        else
            return (GainControl)getControl("javax.media.GainControl");
        return null;
    }

    public Control[] getControls()
    {
        if(controls != null)
            return controls;
        Vector cv = new Vector();
        if(cachingControl != null)
            cv.addElement(cachingControl);
        if(bufferControl != null)
            cv.addElement(bufferControl);
        int size = controllerList.size();
        for(int i = 0; i < size; i++)
        {
            Controller ctrller = (Controller)controllerList.elementAt(i);
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
            controls = ctrls;
        return ctrls;
    }

    public final void controllerUpdate(ControllerEvent evt)
    {
        processEvent(evt);
    }

    public final Vector getControllerList()
    {
        return controllerList;
    }

    private Vector getPotentialEventsList()
    {
        return potentialEventsList;
    }

    private void resetReceivedEventList()
    {
        if(receivedEventList != null)
            receivedEventList.removeAllElements();
    }

    private Vector getReceivedEventsList()
    {
        return receivedEventList;
    }

    private void updateReceivedEventsList(ControllerEvent event)
    {
        if(receivedEventList != null)
        {
            Controller source = event.getSourceController();
            if(receivedEventList.contains(source))
                return;
            receivedEventList.addElement(source);
        }
    }

    public final void start()
    {
        synchronized(startSync)
        {
            if(restartFrom != null)
                return;
            if(getState() == 600)
            {
                sendEvent(new StartEvent(this, 600, 600, 600, mediaTimeAtStart, startTime));
                return;
            }
            if(playThread == null || !playThread.isAlive())
            {
                setTargetState(600);
                if(jmfSecurity != null)
                {
                    String permission = null;
                    try
                    {
                        if(jmfSecurity.getName().startsWith("jmf-security"))
                        {
                            permission = "thread";
                            jmfSecurity.requestPermission(m, cl, args, 16);
                            m[0].invoke(cl[0], args[0]);
                            permission = "thread group";
                            jmfSecurity.requestPermission(m, cl, args, 32);
                            m[0].invoke(cl[0], args[0]);
                        } else
                        if(jmfSecurity.getName().startsWith("internet"))
                        {
                            PolicyEngine.checkPermission(PermissionID.THREAD);
                            PolicyEngine.assertPermission(PermissionID.THREAD);
                        }
                    }
                    catch(Throwable e)
                    {
                        securityPrivelege = false;
                    }
                }
                if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
                {
                    try
                    {
                        Constructor cons = CreateWorkThreadAction.cons;
                        playThread = (PlayThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                            cons.newInstance(new Object[] {
                                com.sun.media.PlayThread.class, com.sun.media.BasicPlayer.class, this
                            })
                        });
                        playThread.start();
                    }
                    catch(Exception e) { }
                } else
                {
                    playThread = new PlayThread(this);
                    playThread.start();
                }
            }
        }
    }

    public final void syncStart(Time tbt)
    {
        synchronized(mediaTimeSync)
        {
            if(syncStartInProgress())
                return;
            int state = getState();
            if(state == 600)
                throwError(new ClockStartedError("syncStart() cannot be used on an already started player"));
            if(state != 500)
                throwError(new NotPrefetchedError("Cannot start player before it has been prefetched"));
            eomEventsReceivedFrom.removeAllElements();
            stopAtTimeReceivedFrom.removeAllElements();
            setTargetState(600);
            for(int i = controllerList.size(); --i >= 0;)
                if(getTargetState() == 600)
                    ((Controller)controllerList.elementAt(i)).syncStart(tbt);

            if(getTargetState() == 600)
            {
                startTime = tbt;
                mediaTimeAtStart = getMediaTime();
                super.syncStart(tbt);
            }
        }
    }

    protected void doStart()
    {
    }

    final synchronized void play()
    {
        if(getTargetState() != 600)
            return;
        prefetchFailed = false;
        int state = getState();
        if(state == 100 || state == 180 || state == 300)
            prefetch();
        while(!closing && !prefetchFailed && (getState() == 140 || getState() == 200 || getState() == 300 || getState() == 400)) 
            try
            {
                wait();
            }
            catch(InterruptedException e) { }
        if(getState() != 600 && getTargetState() == 600 && getState() == 500)
            syncStart(getTimeBase().getTime());
    }

    protected void doStop()
    {
    }

    public final void stop()
    {
        stop(1);
    }

    private void stop(int stopType)
    {
        int state;
        switch(state = getState())
        {
        case 100: // 'd'
        case 300: 
        case 500: 
            setTargetState(state);
            break;

        case 200: 
            setTargetState(300);
            break;

        case 400: 
        case 600: 
            setTargetState(500);
            break;
        }
        if(getState() != 600)
            switch(stopType)
            {
            case 1: // '\001'
                sendEvent(new StopByRequestEvent(this, getState(), getState(), getTargetState(), getMediaTime()));
                break;

            case 2: // '\002'
                sendEvent(new RestartingEvent(this, getState(), getState(), 600, getMediaTime()));
                break;

            default:
                sendEvent(new StopEvent(this, getState(), getState(), getTargetState(), getMediaTime()));
                break;
            }
        else
        if(getState() == 600)
            synchronized(this)
            {
                potentialEventsList = stopEventList;
                resetReceivedEventList();
                receivedAllEvents = false;
                currentControllerList.removeAllElements();
                for(int i = controllerList.size(); --i >= 0;)
                {
                    Controller c = (Controller)controllerList.elementAt(i);
                    currentControllerList.addElement(c);
                    c.stop();
                }

                if(currentControllerList == null)
                    return;
                if(!currentControllerList.isEmpty())
                {
                    try
                    {
                        while(!closing && !receivedAllEvents) 
                            wait();
                    }
                    catch(InterruptedException e) { }
                    currentControllerList.removeAllElements();
                }
                super.stop();
                switch(stopType)
                {
                case 1: // '\001'
                    sendEvent(new StopByRequestEvent(this, 600, getState(), getTargetState(), getMediaTime()));
                    break;

                case 2: // '\002'
                    sendEvent(new RestartingEvent(this, 600, getState(), 600, getMediaTime()));
                    break;

                default:
                    sendEvent(new StopEvent(this, 600, getState(), getTargetState(), getMediaTime()));
                    break;
                }
            }
    }

    protected final void processEndOfMedia()
    {
        super.stop();
        sendEvent(new EndOfMediaEvent(this, 600, 500, getTargetState(), getMediaTime()));
    }

    protected final void manageController(Controller controller)
    {
        manageController(controller, false);
    }

    protected final void manageController(Controller controller, boolean optional)
    {
        if(controller != null && !controllerList.contains(controller))
        {
            controllerList.addElement(controller);
            if(optional)
                optionalControllerList.addElement(controller);
            controller.addControllerListener(this);
        }
        updateDuration();
    }

    public final void unmanageController(Controller controller)
    {
        if(controller != null && controllerList.contains(controller))
        {
            controllerList.removeElement(controller);
            controller.removeControllerListener(this);
        }
    }

    public synchronized void addController(Controller newController)
        throws IncompatibleTimeBaseException
    {
        int playerState = getState();
        if(playerState == 600)
            throwError(new ClockStartedError("Cannot add controller to a started player"));
        if(playerState == 100 || playerState == 200)
            throwError(new NotRealizedError("A Controller cannot be added to an Unrealized Player"));
        if(newController == null || newController == this)
            return;
        int controllerState = newController.getState();
        if(controllerState == 100 || controllerState == 200)
            throwError(new NotRealizedError("An Unrealized Controller cannot be added to a Player"));
        if(controllerList.contains(newController))
            return;
        if(playerState == 500 && (controllerState == 300 || controllerState == 400))
            deallocate();
        manageController(newController);
        newController.setTimeBase(getTimeBase());
        newController.setMediaTime(getMediaTime());
        newController.setStopTime(getStopTime());
        if(newController.setRate(getRate()) != getRate())
            setRate(1.0F);
    }

    public final synchronized void removeController(Controller oldController)
    {
        int state = getState();
        if(state < 300)
            throwError(new NotRealizedError("Cannot remove controller from a unrealized player"));
        if(state == 600)
            throwError(new ClockStartedError("Cannot remove controller from a started player"));
        if(oldController == null)
            return;
        if(controllerList.contains(oldController))
        {
            controllerList.removeElement(oldController);
            oldController.removeControllerListener(this);
            updateDuration();
            try
            {
                oldController.setTimeBase(null);
            }
            catch(IncompatibleTimeBaseException e) { }
        }
    }

    protected abstract boolean audioEnabled();

    protected abstract boolean videoEnabled();

    protected abstract TimeBase getMasterTimeBase();

    protected synchronized boolean doConfigure()
    {
        potentialEventsList = configureEventList;
        resetReceivedEventList();
        receivedAllEvents = false;
        currentControllerList.removeAllElements();
        for(int i = controllerList.size(); --i >= 0;)
        {
            Controller c = (Controller)controllerList.elementAt(i);
            if(c.getState() == 100 && ((c instanceof Processor) || (c instanceof BasicController)))
                currentControllerList.addElement(c);
        }

        for(int i = currentControllerList.size(); --i >= 0;)
        {
            Controller c = (Controller)currentControllerList.elementAt(i);
            if(c instanceof Processor)
                ((Processor)c).configure();
            else
            if(c instanceof BasicController)
                ((BasicController)c).configure();
        }

        if(!currentControllerList.isEmpty())
        {
            try
            {
                while(!closing && !receivedAllEvents) 
                    wait();
            }
            catch(InterruptedException e) { }
            currentControllerList.removeAllElements();
        }
        for(int i = controllerList.size(); --i >= 0;)
        {
            Controller c = (Controller)controllerList.elementAt(i);
            if(((c instanceof Processor) || (c instanceof BasicController)) && c.getState() < 180)
            {
                Log.error("Error: Unable to configure " + c);
                source.disconnect();
                return false;
            }
        }

        return true;
    }

    protected void completeConfigure()
    {
        super.completeConfigure();
        synchronized(this)
        {
            notify();
        }
    }

    protected void doFailedConfigure()
    {
        super.doFailedConfigure();
        synchronized(this)
        {
            notify();
        }
        close();
    }

    protected synchronized boolean doRealize()
    {
        potentialEventsList = realizeEventList;
        resetReceivedEventList();
        receivedAllEvents = false;
        currentControllerList.removeAllElements();
        for(int i = controllerList.size(); --i >= 0;)
        {
            Controller c = (Controller)controllerList.elementAt(i);
            if(c.getState() == 100 || c.getState() == 180)
                currentControllerList.addElement(c);
        }

        for(int i = currentControllerList.size(); --i >= 0;)
        {
            Controller c = (Controller)currentControllerList.elementAt(i);
            c.realize();
        }

        if(!currentControllerList.isEmpty())
        {
            try
            {
                while(!closing && !receivedAllEvents) 
                    wait();
            }
            catch(InterruptedException e) { }
            currentControllerList.removeAllElements();
        }
        for(int i = controllerList.size(); --i >= 0;)
        {
            Controller c = (Controller)controllerList.elementAt(i);
            if(c.getState() < 300)
            {
                Log.error("Error: Unable to realize " + c);
                source.disconnect();
                return false;
            }
        }

        updateDuration();
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "thread";
                    jmfSecurity.requestPermission(m, cl, args, 16);
                    m[0].invoke(cl[0], args[0]);
                    permission = "thread group";
                    jmfSecurity.requestPermission(m, cl, args, 32);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.THREAD);
                    PolicyEngine.assertPermission(PermissionID.THREAD);
                }
            }
            catch(Exception e)
            {
                securityPrivelege = false;
            }
        }
        if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
        {
            try
            {
                Constructor cons = CreateWorkThreadAction.cons;
                statsThread = (StatsThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.StatsThread.class, com.sun.media.BasicPlayer.class, this
                    })
                });
                statsThread.start();
            }
            catch(Exception e) { }
        } else
        {
            statsThread = new StatsThread(this);
            statsThread.start();
        }
        return true;
    }

    protected void completeRealize()
    {
        super.state = 300;
        try
        {
            slaveToMasterTimeBase(getMasterTimeBase());
        }
        catch(IncompatibleTimeBaseException e)
        {
            Log.error(e);
        }
        super.completeRealize();
        synchronized(this)
        {
            notify();
        }
    }

    protected void doFailedRealize()
    {
        super.doFailedRealize();
        synchronized(this)
        {
            notify();
        }
        close();
    }

    protected void completePrefetch()
    {
        super.completePrefetch();
        synchronized(this)
        {
            notify();
        }
    }

    protected void doFailedPrefetch()
    {
        super.doFailedPrefetch();
        synchronized(this)
        {
            notify();
        }
    }

    protected final void abortRealize()
    {
        if(controllerList != null)
        {
            for(int i = controllerList.size(); --i >= 0;)
            {
                Controller c = (Controller)controllerList.elementAt(i);
                c.deallocate();
            }

        }
        synchronized(this)
        {
            notify();
        }
    }

    protected boolean doPrefetch()
    {
        potentialEventsList = prefetchEventList;
        resetReceivedEventList();
        receivedAllEvents = false;
        currentControllerList.removeAllElements();
        Vector list = controllerList;
        if(list == null)
            return false;
        for(int i = list.size(); --i >= 0;)
        {
            Controller c = (Controller)list.elementAt(i);
            if(c.getState() == 300)
            {
                currentControllerList.addElement(c);
                c.prefetch();
            }
        }

        if(!currentControllerList.isEmpty())
            synchronized(this)
            {
                try
                {
                    while(!closing && !receivedAllEvents) 
                        wait();
                }
                catch(InterruptedException e) { }
                currentControllerList.removeAllElements();
            }
        for(int i = list.size(); --i >= 0;)
        {
            Controller c = (Controller)list.elementAt(i);
            if(c.getState() < 500)
            {
                Log.error("Error: Unable to prefetch " + c + "\n");
                if(optionalControllerList.contains(c))
                {
                    removedControllerList.addElement(c);
                } else
                {
                    synchronized(this)
                    {
                        prefetchFailed = true;
                        notifyAll();
                    }
                    return false;
                }
            }
        }

        if(removedControllerList != null)
        {
            for(int i = removedControllerList.size(); --i >= 0;)
            {
                Object o = removedControllerList.elementAt(i);
                controllerList.removeElement(o);
                ((BasicController)o).close();
                if(!deviceBusy((BasicController)o))
                {
                    synchronized(this)
                    {
                        prefetchFailed = true;
                        notifyAll();
                    }
                    return false;
                }
            }

            removedControllerList.removeAllElements();
        }
        return true;
    }

    protected final void abortPrefetch()
    {
        if(controllerList != null)
        {
            for(int i = controllerList.size(); --i >= 0;)
            {
                Controller c = (Controller)controllerList.elementAt(i);
                c.deallocate();
            }

        }
        synchronized(this)
        {
            notify();
        }
    }

    protected boolean deviceBusy(BasicController mc)
    {
        return true;
    }

    protected void slaveToMasterTimeBase(TimeBase tb)
        throws IncompatibleTimeBaseException
    {
        setTimeBase(tb);
    }

    private void notifyIfAllEventsArrived(Vector controllerList, Vector receivedEventList)
    {
        if(receivedEventList != null && receivedEventList.size() == currentControllerList.size())
        {
            receivedAllEvents = true;
            resetReceivedEventList();
            synchronized(this)
            {
                notifyAll();
            }
        }
    }

    protected void processEvent(ControllerEvent evt)
    {
        Controller source = evt.getSourceController();
        if(evt instanceof AudioDeviceUnavailableEvent)
        {
            sendEvent(new AudioDeviceUnavailableEvent(this));
            return;
        }
        if((evt instanceof ControllerClosedEvent) && !closing && controllerList.contains(source) && !(evt instanceof ResourceUnavailableEvent))
        {
            controllerList.removeElement(source);
            if(evt instanceof ControllerErrorEvent)
                sendEvent(new ControllerErrorEvent(this, ((ControllerErrorEvent)evt).getMessage()));
            close();
        }
        if((evt instanceof SizeChangeEvent) && controllerList.contains(source))
        {
            sendEvent(new SizeChangeEvent(this, ((SizeChangeEvent)evt).getWidth(), ((SizeChangeEvent)evt).getHeight(), ((SizeChangeEvent)evt).getScale()));
            return;
        }
        if((evt instanceof DurationUpdateEvent) && controllerList.contains(source))
        {
            updateDuration();
            return;
        }
        if((evt instanceof RestartingEvent) && controllerList.contains(source))
        {
            restartFrom = source;
            int i = controllerList.size();
            super.stop();
            setTargetState(500);
            for(int ii = 0; ii < i; ii++)
            {
                Controller c = (Controller)controllerList.elementAt(ii);
                if(c != source)
                    c.stop();
            }

            super.stop();
            sendEvent(new RestartingEvent(this, 600, 400, 600, getMediaTime()));
        }
        if((evt instanceof StartEvent) && source == restartFrom)
        {
            restartFrom = null;
            start();
        }
        if((evt instanceof SeekFailedEvent) && controllerList.contains(source))
        {
            int i = controllerList.size();
            super.stop();
            setTargetState(500);
            for(int ii = 0; ii < i; ii++)
            {
                Controller c = (Controller)controllerList.elementAt(ii);
                if(c != source)
                    c.stop();
            }

            sendEvent(new SeekFailedEvent(this, 600, 500, 500, getMediaTime()));
        }
        if((evt instanceof EndOfMediaEvent) && controllerList.contains(source))
        {
            if(eomEventsReceivedFrom.contains(source))
                return;
            eomEventsReceivedFrom.addElement(source);
            if(eomEventsReceivedFrom.size() == controllerList.size())
            {
                super.stop();
                sendEvent(new EndOfMediaEvent(this, 600, 500, getTargetState(), getMediaTime()));
            }
            return;
        }
        if((evt instanceof StopAtTimeEvent) && controllerList.contains(source) && getState() == 600)
        {
            synchronized(stopAtTimeReceivedFrom)
            {
                if(stopAtTimeReceivedFrom.contains(source))
                    return;
                stopAtTimeReceivedFrom.addElement(source);
                boolean allStopped = stopAtTimeReceivedFrom.size() == controllerList.size();
                if(!allStopped)
                {
                    allStopped = true;
                    for(int i = 0; i < controllerList.size(); i++)
                    {
                        Controller c = (Controller)controllerList.elementAt(i);
                        if(stopAtTimeReceivedFrom.contains(c) || eomEventsReceivedFrom.contains(c))
                            continue;
                        allStopped = false;
                        break;
                    }

                }
                if(allStopped)
                {
                    super.stop();
                    doSetStopTime(Clock.RESET);
                    sendEvent(new StopAtTimeEvent(this, 600, 500, getTargetState(), getMediaTime()));
                }
            }
            return;
        }
        if((evt instanceof CachingControlEvent) && controllerList.contains(source))
        {
            CachingControl mcc = ((CachingControlEvent)evt).getCachingControl();
            sendEvent(new CachingControlEvent(this, mcc, mcc.getContentProgress()));
            return;
        }
        Vector eventList = potentialEventsList;
        if(controllerList != null && controllerList.contains(source) && eventList != null && eventList.contains(evt.getClass().getName()))
        {
            updateReceivedEventsList(evt);
            notifyIfAllEventsArrived(controllerList, getReceivedEventsList());
        }
    }

    private boolean trySetRate(float rate)
    {
        for(int i = controllerList.size(); --i >= 0;)
        {
            Controller c = (Controller)controllerList.elementAt(i);
            if(c.setRate(rate) != rate)
                return false;
        }

        return true;
    }

    protected float doSetRate(float factor)
    {
        return factor;
    }

    public float setRate(float rate)
    {
        if(super.state < 300)
            throwError(new NotRealizedError("Cannot set rate on an unrealized Player."));
        if(source instanceof RateConfigureable)
            rate = checkRateConfig((RateConfigureable)source, rate);
        float oldRate = getRate();
        if(oldRate == rate)
            return rate;
        if(getState() == 600)
        {
            aboutToRestart = true;
            stop(2);
        }
        float rateSet;
        if(!trySetRate(rate))
        {
            if(!trySetRate(oldRate))
            {
                trySetRate(1.0F);
                rateSet = 1.0F;
            } else
            {
                rateSet = oldRate;
            }
        } else
        {
            rateSet = rate;
        }
        super.setRate(rateSet);
        if(aboutToRestart)
        {
            syncStart(getTimeBase().getTime());
            aboutToRestart = false;
        }
        return rateSet;
    }

    float checkRateConfig(RateConfigureable rc, float rate)
    {
        RateConfiguration config[] = rc.getRateConfigurations();
        if(config == null)
            return 1.0F;
        float corrected = 1.0F;
        for(int i = 0; i < config.length; i++)
        {
            RateRange rr = config[i].getRate();
            if(rr == null || !rr.inRange(rate))
                continue;
            rr.setCurrentRate(rate);
            corrected = rate;
            RateConfiguration c = rc.setRateConfiguration(config[i]);
            if(c != null && (rr = c.getRate()) != null)
                corrected = rr.getCurrentRate();
            break;
        }

        return corrected;
    }

    public abstract void updateStats();

    public static String VERSION = JMFI18N.getResource("mediaplayer.version");
    protected DataSource source;
    protected Vector controllerList;
    private Vector optionalControllerList;
    private Vector removedControllerList;
    private Vector currentControllerList;
    private Vector potentialEventsList;
    private Vector receivedEventList;
    private boolean receivedAllEvents;
    private Vector configureEventList;
    private Vector realizeEventList;
    private Vector prefetchEventList;
    private Vector stopEventList;
    private ControllerEvent CachingControlEvent;
    private Controller restartFrom;
    private Vector eomEventsReceivedFrom;
    private Vector stopAtTimeReceivedFrom;
    private PlayThread playThread;
    private StatsThread statsThread;
    private Time duration;
    private Time startTime;
    private Time mediaTimeAtStart;
    private boolean aboutToRestart;
    private boolean closing;
    private boolean prefetchFailed;
    protected boolean framePositioning;
    protected Control controls[];
    protected Component controlComp;
    public SliderRegionControl regionControl;
    protected CachingControl cachingControl;
    protected ExtendedCachingControl extendedCachingControl;
    protected BufferControl bufferControl;
    private Object startSync;
    private Object mediaTimeSync;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    long lastTime;
    static final int LOCAL_STOP = 0;
    static final int STOP_BY_REQUEST = 1;
    static final int RESTARTING = 2;

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
    }
}
