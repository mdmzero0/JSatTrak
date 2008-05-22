// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StreamSynch.java

package com.sun.media.rtp;


class SynchSource
{

    public SynchSource(int ssrc, long rtpTimestamp, long ntpTimestamp)
    {
        this.ssrc = ssrc;
        this.rtpTimestamp = rtpTimestamp;
        this.ntpTimestamp = ntpTimestamp;
        factor = 0.0D;
    }

    int ssrc;
    long rtpTimestamp;
    long ntpTimestamp;
    double factor;
}
