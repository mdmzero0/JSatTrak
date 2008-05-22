// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPTransStats.java

package com.sun.media.rtp;

import javax.media.rtp.TransmissionStats;

public class RTPTransStats
    implements TransmissionStats
{

    public RTPTransStats()
    {
        total_pdu = 0;
        total_bytes = 0;
        total_rtcp = 0;
    }

    public int getPDUTransmitted()
    {
        return total_pdu;
    }

    public int getBytesTransmitted()
    {
        return total_bytes;
    }

    public int getRTCPSent()
    {
        return total_rtcp;
    }

    protected int total_pdu;
    protected int total_bytes;
    protected int total_rtcp;
}
