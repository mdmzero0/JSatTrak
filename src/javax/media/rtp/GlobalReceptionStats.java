// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GlobalReceptionStats.java

package javax.media.rtp;


public interface GlobalReceptionStats
{

    public abstract int getPacketsRecd();

    public abstract int getBytesRecd();

    public abstract int getBadRTPkts();

    public abstract int getLocalColls();

    public abstract int getRemoteColls();

    public abstract int getPacketsLooped();

    public abstract int getTransmitFailed();

    public abstract int getRTCPRecd();

    public abstract int getSRRecd();

    public abstract int getBadRTCPPkts();

    public abstract int getUnknownTypes();

    public abstract int getMalformedRR();

    public abstract int getMalformedSDES();

    public abstract int getMalformedBye();

    public abstract int getMalformedSR();
}
