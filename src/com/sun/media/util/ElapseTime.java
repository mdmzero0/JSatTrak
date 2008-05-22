// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ElapseTime.java

package com.sun.media.util;

import javax.media.Format;
import javax.media.format.AudioFormat;

public class ElapseTime
{

    public ElapseTime()
    {
        value = 0L;
    }

    public void setValue(long t)
    {
        value = t;
    }

    public long getValue()
    {
        return value;
    }

    public boolean update(int len, long ts, Format f)
    {
        long t;
        if(f instanceof AudioFormat)
        {
            if((t = ((AudioFormat)f).computeDuration(len)) > 0L)
                value += t;
            else
            if(ts > 0L)
                value = ts;
            else
                return false;
        } else
        if(ts > 0L)
            value = ts;
        else
            return false;
        return true;
    }

    public static long audioLenToTime(long len, AudioFormat af)
    {
        return af.computeDuration(len);
    }

    public static long audioTimeToLen(long duration, AudioFormat af)
    {
        long units;
        long bytesPerSec;
        if(af.getSampleSizeInBits() > 0)
        {
            units = af.getSampleSizeInBits() * af.getChannels();
            bytesPerSec = (long)(((double)units * af.getSampleRate()) / 8D);
        } else
        if(af.getFrameSizeInBits() != -1 && af.getFrameRate() != -1D)
        {
            units = af.getFrameSizeInBits();
            bytesPerSec = (long)(((double)units * af.getFrameRate()) / 8D);
        } else
        {
            units = bytesPerSec = 0L;
        }
        return bytesPerSec != 0L ? ((duration * bytesPerSec) / 0x3b9aca00L / units) * units : 0L;
    }

    public long value;
}
