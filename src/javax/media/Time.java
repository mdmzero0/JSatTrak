// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Time.java

package javax.media;

import java.io.Serializable;

public class Time
    implements Serializable
{

    public Time(long nanoseconds)
    {
        this.nanoseconds = nanoseconds;
    }

    public Time(double seconds)
    {
        nanoseconds = secondsToNanoseconds(seconds);
    }

    protected long secondsToNanoseconds(double seconds)
    {
        return (long)(seconds * 1000000000D);
    }

    public long getNanoseconds()
    {
        return nanoseconds;
    }

    public double getSeconds()
    {
        return (double)nanoseconds * 1.0000000000000001E-009D;
    }

    public static final long ONE_SECOND = 0x3b9aca00L;
    public static final Time TIME_UNKNOWN = new Time(0x7ffffffffffffffeL);
    private static final double NANO_TO_SEC = 1.0000000000000001E-009D;
    protected long nanoseconds;

}
