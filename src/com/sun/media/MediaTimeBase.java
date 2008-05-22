// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaTimeBase.java

package com.sun.media;

import javax.media.*;

public abstract class MediaTimeBase
    implements TimeBase
{

    public MediaTimeBase()
    {
        origin = 0L;
        offset = 0L;
        time = 0L;
        systemTimeBase = null;
        mediaStopped();
    }

    public Time getTime()
    {
        return new Time(getNanoseconds());
    }

    public synchronized long getNanoseconds()
    {
        if(systemTimeBase != null)
            time = (origin + systemTimeBase.getNanoseconds()) - offset;
        else
            time = (origin + getMediaTime()) - offset;
        return time;
    }

    public abstract long getMediaTime();

    public synchronized void mediaStarted()
    {
        systemTimeBase = null;
        offset = getMediaTime();
        origin = time;
    }

    public synchronized void mediaStopped()
    {
        systemTimeBase = new SystemTimeBase();
        offset = systemTimeBase.getNanoseconds();
        origin = time;
    }

    long origin;
    long offset;
    long time;
    TimeBase systemTimeBase;
}
