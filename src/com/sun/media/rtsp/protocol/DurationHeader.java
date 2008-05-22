// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DurationHeader.java

package com.sun.media.rtsp.protocol;


public class DurationHeader
{

    public DurationHeader(String str)
    {
        duration = (new Long(str)).longValue();
    }

    public long getDuration()
    {
        return duration;
    }

    private long duration;
}
