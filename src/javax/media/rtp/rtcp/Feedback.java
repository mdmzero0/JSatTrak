// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Feedback.java

package javax.media.rtp.rtcp;


public interface Feedback
{

    public abstract long getSSRC();

    public abstract int getFractionLost();

    public abstract long getNumLost();

    public abstract long getXtndSeqNum();

    public abstract long getJitter();

    public abstract long getLSR();

    public abstract long getDLSR();
}
