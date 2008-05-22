// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicClock.java

package com.sun.media;

import javax.media.*;

// Referenced classes of package com.sun.media:
//            Log

public class BasicClock
    implements Clock
{

    public BasicClock()
    {
        startTime = 0x7fffffffffffffffL;
        stopTime = 0x7fffffffffffffffL;
        mediaTime = 0L;
        mediaStart = 0L;
        mediaLength = -1L;
        rate = 1.0F;
        master = new SystemTimeBase();
    }

    public void setTimeBase(TimeBase master)
        throws IncompatibleTimeBaseException
    {
        if(getState() == 1)
            throwError(new ClockStartedError("setTimeBase cannot be used on a started clock."));
        if(master == null)
        {
            if(!(this.master instanceof SystemTimeBase))
                this.master = new SystemTimeBase();
        } else
        {
            this.master = master;
        }
    }

    public void syncStart(Time tbt)
    {
        if(getState() == 1)
            throwError(new ClockStartedError("syncStart() cannot be used on an already started clock."));
        if(master.getNanoseconds() > tbt.getNanoseconds())
            startTime = master.getNanoseconds();
        else
            startTime = tbt.getNanoseconds();
    }

    public void stop()
    {
        if(getState() == 0)
        {
            return;
        } else
        {
            mediaTime = getMediaNanoseconds();
            startTime = 0x7fffffffffffffffL;
            return;
        }
    }

    public void setStopTime(Time t)
    {
        if(getState() == 1 && stopTime != 0x7fffffffffffffffL)
            throwError(new StopTimeSetError("setStopTime() may be set only once on a Started Clock"));
        stopTime = t.getNanoseconds();
    }

    public Time getStopTime()
    {
        return new Time(stopTime);
    }

    public void setMediaTime(Time now)
    {
        if(getState() == 1)
            throwError(new ClockStartedError("setMediaTime() cannot be used on a started clock."));
        long t = now.getNanoseconds();
        if(t < mediaStart)
            mediaTime = mediaStart;
        else
        if(mediaLength != -1L && t > mediaStart + mediaLength)
            mediaTime = mediaStart + mediaLength;
        else
            mediaTime = t;
    }

    public Time getMediaTime()
    {
        return new Time(getMediaNanoseconds());
    }

    public long getMediaNanoseconds()
    {
        if(getState() == 0)
            return mediaTime;
        long now = master.getNanoseconds();
        if(now > startTime)
        {
            long t = (long)((double)(now - startTime) * (double)rate) + mediaTime;
            if(mediaLength != -1L && t > mediaStart + mediaLength)
                return mediaStart + mediaLength;
            else
                return t;
        } else
        {
            return mediaTime;
        }
    }

    protected void setMediaStart(long t)
    {
        mediaStart = t;
    }

    protected void setMediaLength(long t)
    {
        mediaLength = t;
    }

    public int getState()
    {
        if(startTime == 0x7fffffffffffffffL)
            return 0;
        return stopTime != 0x7fffffffffffffffL ? 1 : 1;
    }

    public Time getSyncTime()
    {
        return new Time(0L);
    }

    public TimeBase getTimeBase()
    {
        return master;
    }

    public Time mapToTimeBase(Time t)
        throws ClockStoppedException
    {
        if(getState() == 0)
        {
            ClockStoppedException e = new ClockStoppedException();
            Log.dumpStack(e);
            throw e;
        } else
        {
            return new Time((long)((float)(t.getNanoseconds() - mediaTime) / rate) + startTime);
        }
    }

    public float setRate(float factor)
    {
        if(getState() == 1)
            throwError(new ClockStartedError("setRate() cannot be used on a started clock."));
        rate = factor;
        return rate;
    }

    public float getRate()
    {
        return rate;
    }

    protected void throwError(Error e)
    {
        Log.dumpStack(e);
        throw e;
    }

    private TimeBase master;
    private long startTime;
    private long stopTime;
    private long mediaTime;
    private long mediaStart;
    private long mediaLength;
    private float rate;
    public static final int STOPPED = 0;
    public static final int STARTED = 1;
}
