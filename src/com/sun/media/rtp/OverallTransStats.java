// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   OverallTransStats.java

package com.sun.media.rtp;

import javax.media.rtp.GlobalTransmissionStats;

public class OverallTransStats
    implements GlobalTransmissionStats
{

    public OverallTransStats()
    {
        rtp_sent = 0;
        bytes_sent = 0;
        rtcp_sent = 0;
        local_coll = 0;
        remote_coll = 0;
        transmit_failed = 0;
    }

    public int getRTPSent()
    {
        return rtp_sent;
    }

    public int getBytesSent()
    {
        return bytes_sent;
    }

    public int getRTCPSent()
    {
        return rtcp_sent;
    }

    public int getLocalColls()
    {
        return local_coll;
    }

    public int getRemoteColls()
    {
        return remote_coll;
    }

    public int getTransmitFailed()
    {
        return transmit_failed;
    }

    protected int rtp_sent;
    protected int bytes_sent;
    protected int rtcp_sent;
    protected int local_coll;
    protected int remote_coll;
    protected int transmit_failed;
}
