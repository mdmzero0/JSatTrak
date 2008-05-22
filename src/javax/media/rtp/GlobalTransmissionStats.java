// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GlobalTransmissionStats.java

package javax.media.rtp;


public interface GlobalTransmissionStats
{

    public abstract int getRTPSent();

    public abstract int getBytesSent();

    public abstract int getRTCPSent();

    public abstract int getLocalColls();

    public abstract int getRemoteColls();

    public abstract int getTransmitFailed();
}
