// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicController.java

package com.sun.media;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.util.ThreadedEventQueue;
import com.sun.media.util.jdk12;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import javax.media.*;

// Referenced classes of package com.sun.media:
//            SendEventQueue, BasicClock, TimedStartThread, StopTimeThread, 
//            ConfigureWorkThread, RealizeWorkThread, PrefetchWorkThread, JMFSecurity, 
//            CreateWorkThreadAction, TimedActionThread, CreateTimedThreadAction, Log, 
//            JMFSecurityManager

public abstract class BasicController
    implements Controller, Duration
{

    public BasicController()
    {
        targetState = 100;
        state = 100;
        listenerList = null;
        configureThread = null;
        realizeThread = null;
        prefetchThread = null;
        processError = null;
        startThread = null;
        stopTimeThread = null;
        interrupted = false;
        interruptSync = new Object();
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        stopThreadEnabled = true;
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
                sendEvtQueue = (SendEventQueue)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.SendEventQueue.class, com.sun.media.BasicController.class, this
                    })
                });
                sendEvtQueue.setName(sendEvtQueue.getName() + ": SendEventQueue: " + getClass().getName());
                sendEvtQueue.start();
                clock = new BasicClock();
            }
            catch(Exception e) { }
        } else
        {
            sendEvtQueue = new SendEventQueue(this);
            sendEvtQueue.setName(sendEvtQueue.getName() + ": SendEventQueue: " + getClass().getName());
            sendEvtQueue.start();
            clock = new BasicClock();
        }
    }

    protected abstract boolean isConfigurable();

    protected void setClock(Clock c)
    {
        clock = c;
    }

    protected Clock getClock()
    {
        return clock;
    }

    protected void interrupt()
    {
        synchronized(interruptSync)
        {
            interrupted = true;
            interruptSync.notify();
        }
    }

    protected void resetInterrupt()
    {
        synchronized(interruptSync)
        {
            interrupted = false;
            interruptSync.notify();
        }
    }

    protected boolean isInterrupted()
    {
        return interrupted;
    }

    protected boolean doConfigure()
    {
        return true;
    }

    protected void abortConfigure()
    {
    }

    protected abstract boolean doRealize();

    protected abstract void abortRealize();

    protected abstract boolean doPrefetch();

    protected abstract void abortPrefetch();

    protected abstract void doStart();

    protected void doStop()
    {
    }

    public final void close()
    {
        doClose();
        interrupt();
        if(startThread != null)
            startThread.abort();
        if(stopTimeThread != null)
            stopTimeThread.abort();
        if(sendEvtQueue != null)
        {
            sendEvtQueue.kill();
            sendEvtQueue = null;
        }
    }

    protected void doClose()
    {
    }

    public void setTimeBase(TimeBase tb)
        throws IncompatibleTimeBaseException
    {
        if(state < 300)
            throwError(new NotRealizedError(TimeBaseError));
        clock.setTimeBase(tb);
    }

    public Control[] getControls()
    {
        return new Control[0];
    }

    public Control getControl(String type)
    {
        Class cls;
        try
        {
            cls = Class.forName(type);
        }
        catch(ClassNotFoundException e)
        {
            return null;
        }
        Control cs[] = getControls();
        for(int i = 0; i < cs.length; i++)
            if(cls.isInstance(cs[i]))
                return cs[i];

        return null;
    }

    public void syncStart(Time tbt)
    {
        if(state < 500)
            throwError(new NotPrefetchedError(SyncStartError));
        clock.syncStart(tbt);
        state = 600;
        setTargetState(600);
        sendEvent(new StartEvent(this, 500, 600, 600, getMediaTime(), tbt));
        long timeToStop;
        if((timeToStop = checkStopTime()) < 0L || stopThreadEnabled && activateStopThread(timeToStop))
        {
            stopAtTime();
            return;
        }
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
                Constructor cons = CreateTimedThreadAction.cons;
                startThread = (TimedStartThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.TimedStartThread.class, com.sun.media.BasicController.class, this, new Long(tbt.getNanoseconds())
                    })
                });
                startThread.setName(startThread.getName() + " ( startThread: " + this + " )");
                startThread.start();
            }
            catch(Exception e) { }
        } else
        {
            startThread = new TimedStartThread(this, tbt.getNanoseconds());
            startThread.setName(startThread.getName() + " ( startThread: " + this + " )");
            startThread.start();
        }
    }

    protected boolean syncStartInProgress()
    {
        return startThread != null && startThread.isAlive();
    }

    private long checkStopTime()
    {
        long stopTime = getStopTime().getNanoseconds();
        if(stopTime == 0x7fffffffffffffffL)
            return 1L;
        else
            return (long)((float)(stopTime - getMediaTime().getNanoseconds()) / getRate());
    }

    private boolean activateStopThread(long timeToStop)
    {
        if(getStopTime().getNanoseconds() == 0x7fffffffffffffffL)
            return false;
        if(stopTimeThread != null && stopTimeThread.isAlive())
        {
            stopTimeThread.abort();
            stopTimeThread = null;
        }
        if(timeToStop > 0x5f5e100L)
        {
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
                try
                {
                    Constructor cons = CreateTimedThreadAction.cons;
                    stopTimeThread = (StopTimeThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            com.sun.media.StopTimeThread.class, com.sun.media.BasicController.class, this, new Long(timeToStop)
                        })
                    });
                    stopTimeThread.start();
                }
                catch(Exception e) { }
            else
                (stopTimeThread = new StopTimeThread(this, timeToStop)).start();
            return false;
        } else
        {
            return true;
        }
    }

    public void stop()
    {
        if(state == 600 || state == 400)
        {
            stopControllerOnly();
            doStop();
        }
    }

    protected void stopControllerOnly()
    {
        if(state == 600 || state == 400)
        {
            clock.stop();
            state = 500;
            setTargetState(500);
            if(stopTimeThread != null && stopTimeThread.isAlive() && Thread.currentThread() != stopTimeThread)
                stopTimeThread.abort();
            if(startThread != null && startThread.isAlive())
                startThread.abort();
        }
    }

    protected void stopAtTime()
    {
        stop();
        setStopTime(Clock.RESET);
        sendEvent(new StopAtTimeEvent(this, 600, 500, getTargetState(), getMediaTime()));
    }

    public void setStopTime(Time t)
    {
        if(state < 300)
            throwError(new NotRealizedError(StopTimeError));
        Time oldStopTime = getStopTime();
        clock.setStopTime(t);
        boolean stopTimeHasPassed = false;
        long timeToStop;
        if(state == 600 && ((timeToStop = checkStopTime()) < 0L || stopThreadEnabled && activateStopThread(timeToStop)))
            stopTimeHasPassed = true;
        if(oldStopTime.getNanoseconds() != t.getNanoseconds())
            sendEvent(new StopTimeChangeEvent(this, t));
        if(stopTimeHasPassed)
            stopAtTime();
    }

    public Time getStopTime()
    {
        return clock.getStopTime();
    }

    public void setMediaTime(Time when)
    {
        if(state < 300)
            throwError(new NotRealizedError(MediaTimeError));
        clock.setMediaTime(when);
        doSetMediaTime(when);
        sendEvent(new MediaTimeSetEvent(this, when));
    }

    protected void doSetMediaTime(Time time)
    {
    }

    public Time getMediaTime()
    {
        return clock.getMediaTime();
    }

    public long getMediaNanoseconds()
    {
        return clock.getMediaNanoseconds();
    }

    public Time getSyncTime()
    {
        return new Time(0L);
    }

    public TimeBase getTimeBase()
    {
        if(state < 300)
            throwError(new NotRealizedError(GetTimeBaseError));
        return clock.getTimeBase();
    }

    public Time mapToTimeBase(Time t)
        throws ClockStoppedException
    {
        return clock.mapToTimeBase(t);
    }

    public float setRate(float factor)
    {
        if(state < 300)
            throwError(new NotRealizedError(SetRateError));
        float oldRate = getRate();
        float rateSet = doSetRate(factor);
        float newRate = clock.setRate(rateSet);
        if(newRate != oldRate)
            sendEvent(new RateChangeEvent(this, newRate));
        return newRate;
    }

    protected float doSetRate(float factor)
    {
        return factor;
    }

    public float getRate()
    {
        return clock.getRate();
    }

    public final int getState()
    {
        return state;
    }

    protected final void setTargetState(int state)
    {
        targetState = state;
    }

    public final int getTargetState()
    {
        return targetState;
    }

    public Time getStartLatency()
    {
        if(state < 300)
            throwError(new NotRealizedError(LatencyError));
        return Controller.LATENCY_UNKNOWN;
    }

    public Time getDuration()
    {
        return Duration.DURATION_UNKNOWN;
    }

    protected void setMediaLength(long t)
    {
        if(clock instanceof BasicClock)
            ((BasicClock)clock).setMediaLength(t);
    }

    public synchronized void configure()
    {
        if(getTargetState() < 180)
            setTargetState(180);
        switch(state)
        {
        case 140: 
        default:
            break;

        case 180: 
        case 200: 
        case 300: 
        case 400: 
        case 500: 
        case 600: 
            sendEvent(new ConfigureCompleteEvent(this, state, state, getTargetState()));
            break;

        case 100: // 'd'
            state = 140;
            sendEvent(new TransitionEvent(this, 100, 140, getTargetState()));
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
                    configureThread = (ConfigureWorkThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            com.sun.media.ConfigureWorkThread.class, com.sun.media.BasicController.class, this
                        })
                    });
                    configureThread.setName(configureThread.getName() + "[ " + this + " ]" + " ( configureThread)");
                    configureThread.start();
                }
                catch(Exception e) { }
            } else
            {
                configureThread = new ConfigureWorkThread(this);
                configureThread.setName(configureThread.getName() + "[ " + this + " ]" + " ( configureThread)");
                configureThread.start();
            }
            break;
        }
    }

    protected synchronized void completeConfigure()
    {
        state = 180;
        sendEvent(new ConfigureCompleteEvent(this, 140, 180, getTargetState()));
        if(getTargetState() >= 300)
            realize();
    }

    protected void doFailedConfigure()
    {
        state = 100;
        setTargetState(100);
        String msg = "Failed to configure";
        if(processError != null)
            msg = msg + ": " + processError;
        sendEvent(new ResourceUnavailableEvent(this, msg));
        processError = null;
    }

    public final synchronized void realize()
    {
        if(getTargetState() < 300)
            setTargetState(300);
        switch(state)
        {
        case 140: 
        case 200: 
        default:
            break;

        case 300: 
        case 400: 
        case 500: 
        case 600: 
            sendEvent(new RealizeCompleteEvent(this, state, state, getTargetState()));
            break;

        case 100: // 'd'
            if(isConfigurable())
            {
                configure();
                break;
            }
            // fall through

        case 180: 
            int oldState = state;
            state = 200;
            sendEvent(new TransitionEvent(this, oldState, 200, getTargetState()));
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
                    realizeThread = (RealizeWorkThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            com.sun.media.RealizeWorkThread.class, com.sun.media.BasicController.class, this
                        })
                    });
                    realizeThread.setName(realizeThread.getName() + "[ " + this + " ]" + " ( realizeThread)");
                    realizeThread.start();
                }
                catch(Exception e) { }
            } else
            {
                realizeThread = new RealizeWorkThread(this);
                realizeThread.setName(realizeThread.getName() + "[ " + this + " ]" + " ( realizeThread)");
                realizeThread.start();
            }
            break;
        }
    }

    protected synchronized void completeRealize()
    {
        state = 300;
        sendEvent(new RealizeCompleteEvent(this, 200, 300, getTargetState()));
        if(getTargetState() >= 500)
            prefetch();
    }

    protected void doFailedRealize()
    {
        state = 100;
        setTargetState(100);
        String msg = "Failed to realize";
        if(processError != null)
            msg = msg + ": " + processError;
        sendEvent(new ResourceUnavailableEvent(this, msg));
        processError = null;
    }

    public final void prefetch()
    {
        if(getTargetState() <= 300)
            setTargetState(500);
        switch(state)
        {
        case 140: 
        case 200: 
        case 400: 
        default:
            break;

        case 500: 
        case 600: 
            sendEvent(new PrefetchCompleteEvent(this, state, state, getTargetState()));
            break;

        case 100: // 'd'
        case 180: 
            realize();
            break;

        case 300: 
            state = 400;
            sendEvent(new TransitionEvent(this, 300, 400, getTargetState()));
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
                    prefetchThread = (PrefetchWorkThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            com.sun.media.PrefetchWorkThread.class, com.sun.media.BasicController.class, this
                        })
                    });
                    prefetchThread.setName(prefetchThread.getName() + "[ " + this + " ]" + " ( prefetchThread)");
                    prefetchThread.start();
                }
                catch(Exception e) { }
            } else
            {
                prefetchThread = new PrefetchWorkThread(this);
                prefetchThread.setName(prefetchThread.getName() + " ( prefetchThread)");
                prefetchThread.start();
            }
            break;
        }
    }

    protected void completePrefetch()
    {
        clock.stop();
        state = 500;
        sendEvent(new PrefetchCompleteEvent(this, 400, 500, getTargetState()));
    }

    protected void doFailedPrefetch()
    {
        state = 300;
        setTargetState(300);
        String msg = "Failed to prefetch";
        if(processError != null)
            msg = msg + ": " + processError;
        sendEvent(new ResourceUnavailableEvent(this, msg));
        processError = null;
    }

    public final void deallocate()
    {
        int previousState = getState();
        if(state == 600)
            throwError(new ClockStartedError(DeallocateError));
        switch(state)
        {
        case 140: 
        case 200: 
            interrupt();
            state = 100;
            break;

        case 400: 
            interrupt();
            state = 300;
            break;

        case 500: 
            abortPrefetch();
            state = 300;
            resetInterrupt();
            break;
        }
        setTargetState(state);
        doDeallocate();
        synchronized(interruptSync)
        {
            while(isInterrupted()) 
                try
                {
                    interruptSync.wait();
                }
                catch(InterruptedException e) { }
        }
        sendEvent(new DeallocateEvent(this, previousState, state, state, getMediaTime()));
    }

    protected void doDeallocate()
    {
    }

    public final void addControllerListener(ControllerListener listener)
    {
        if(listenerList == null)
            listenerList = new Vector();
        synchronized(listenerList)
        {
            if(!listenerList.contains(listener))
                listenerList.addElement(listener);
        }
    }

    public final void removeControllerListener(ControllerListener listener)
    {
        if(listenerList == null)
            return;
        synchronized(listenerList)
        {
            if(listenerList != null)
                listenerList.removeElement(listener);
        }
    }

    protected final void sendEvent(ControllerEvent evt)
    {
        if(sendEvtQueue != null)
            sendEvtQueue.postEvent(evt);
    }

    protected final void dispatchEvent(ControllerEvent evt)
    {
        if(listenerList == null)
            return;
        synchronized(listenerList)
        {
            ControllerListener listener;
            for(Enumeration list = listenerList.elements(); list.hasMoreElements(); listener.controllerUpdate(evt))
                listener = (ControllerListener)list.nextElement();

        }
    }

    protected void throwError(Error e)
    {
        Log.dumpStack(e);
        throw e;
    }

    private int targetState;
    protected int state;
    private Vector listenerList;
    private SendEventQueue sendEvtQueue;
    private ConfigureWorkThread configureThread;
    private RealizeWorkThread realizeThread;
    private PrefetchWorkThread prefetchThread;
    protected String processError;
    private Clock clock;
    private TimedStartThread startThread;
    private StopTimeThread stopTimeThread;
    private boolean interrupted;
    private Object interruptSync;
    static final int Configuring = 140;
    static final int Configured = 180;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    protected boolean stopThreadEnabled;
    static String TimeBaseError = "Cannot set time base on an unrealized controller.";
    static String SyncStartError = "Cannot start the controller before it has been prefetched.";
    static String StopTimeError = "Cannot set stop time on an unrealized controller.";
    static String MediaTimeError = "Cannot set media time on a unrealized controller";
    static String GetTimeBaseError = "Cannot get Time Base from an unrealized controller";
    static String SetRateError = "Cannot set rate on an unrealized controller.";
    static String LatencyError = "Cannot get start latency from an unrealized controller";
    static String DeallocateError = "deallocate cannot be used on a started controller.";

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
