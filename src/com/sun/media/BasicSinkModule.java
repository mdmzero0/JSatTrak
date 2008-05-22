// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicSinkModule.java

package com.sun.media;

import javax.media.*;

// Referenced classes of package com.sun.media:
//            BasicModule, BasicController

public abstract class BasicSinkModule extends BasicModule
{

    public BasicSinkModule()
    {
        prerolling = false;
        rate = 1.0F;
        stopTime = -1L;
    }

    public void doStart()
    {
        super.doStart();
        if(clock != null)
            clock.syncStart(clock.getTimeBase().getTime());
    }

    public void doStop()
    {
        if(clock != null)
            clock.stop();
    }

    public void doSetMediaTime(Time t)
    {
        if(clock != null)
            clock.setMediaTime(t);
    }

    public float doSetRate(float r)
    {
        if(clock != null)
            rate = clock.setRate(r);
        else
            rate = r;
        return rate;
    }

    public void setTimeBase(TimeBase tb)
        throws IncompatibleTimeBaseException
    {
        if(clock != null)
            clock.setTimeBase(tb);
    }

    public TimeBase getTimeBase()
    {
        if(clock != null)
            return clock.getTimeBase();
        else
            return super.controller.getTimeBase();
    }

    public Time getMediaTime()
    {
        if(clock != null)
            return clock.getMediaTime();
        else
            return super.controller.getMediaTime();
    }

    public long getMediaNanoseconds()
    {
        if(clock != null)
            return clock.getMediaNanoseconds();
        else
            return super.controller.getMediaNanoseconds();
    }

    public Clock getClock()
    {
        return clock;
    }

    protected void setClock(Clock c)
    {
        clock = c;
    }

    public void setStopTime(Time t)
    {
        if(t == Clock.RESET)
            stopTime = -1L;
        else
            stopTime = t.getNanoseconds();
    }

    public void setPreroll(long wanted, long actual)
    {
        if(actual < wanted)
            prerolling = true;
    }

    public void triggerReset()
    {
    }

    public void doneReset()
    {
    }

    private Clock clock;
    protected boolean prerolling;
    protected float rate;
    protected long stopTime;
}
