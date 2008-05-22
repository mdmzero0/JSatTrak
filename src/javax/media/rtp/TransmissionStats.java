// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TransmissionStats.java

package javax.media.rtp;


public interface TransmissionStats
{

    public abstract int getPDUTransmitted();

    public abstract int getBytesTransmitted();

    public abstract int getRTCPSent();
}
