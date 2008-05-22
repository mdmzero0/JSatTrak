// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPReportBlock.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.Signed;
import javax.media.rtp.rtcp.Feedback;

public class RTCPReportBlock
    implements Feedback
{

    public RTCPReportBlock()
    {
    }

    public long getSSRC()
    {
        return (long)ssrc;
    }

    public int getFractionLost()
    {
        return fractionlost;
    }

    public long getNumLost()
    {
        return (long)packetslost;
    }

    public long getXtndSeqNum()
    {
        return lastseq;
    }

    public long getJitter()
    {
        return (long)jitter;
    }

    public long getLSR()
    {
        return lsr;
    }

    public long getDLSR()
    {
        return dlsr;
    }

    public String toString()
    {
        long printssrc = ssrc;
        if(ssrc < 0)
            printssrc = Signed.UnsignedInt(ssrc);
        return "\t\tFor source " + printssrc + "\n\t\t\tFraction of packets lost: " + fractionlost + " (" + (double)fractionlost / 256D + ")" + "\n\t\t\tPackets lost: " + packetslost + "\n\t\t\tLast sequence number: " + lastseq + "\n\t\t\tJitter: " + jitter + "\n\t\t\tLast SR packet received at time " + lsr + "\n\t\t\tDelay since last SR packet received: " + dlsr + " (" + (double)dlsr / 65536D + " seconds)\n";
    }

    public static String toString(RTCPReportBlock reports[])
    {
        String s = "";
        for(int i = 0; i < reports.length; i++)
            s = s + reports[i];

        return s;
    }

    int ssrc;
    int fractionlost;
    int packetslost;
    long lastseq;
    int jitter;
    long lsr;
    long dlsr;
    long receiptTime;
}
