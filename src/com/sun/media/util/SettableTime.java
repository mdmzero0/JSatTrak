// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SettableTime.java

package com.sun.media.util;

import javax.media.Time;

public class SettableTime extends Time
{

    public SettableTime()
    {
        super(0L);
    }

    public SettableTime(long nanoseconds)
    {
        super(nanoseconds);
    }

    public SettableTime(double seconds)
    {
        super(seconds);
    }

    public final Time set(long nanoseconds)
    {
        super.nanoseconds = nanoseconds;
        return this;
    }

    public final Time set(double seconds)
    {
        super.nanoseconds = secondsToNanoseconds(seconds);
        return this;
    }
}
