// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReceptionStats.java

package javax.media.rtp;


public interface ReceptionStats
{

    public abstract int getPDUlost();

    public abstract int getPDUProcessed();

    public abstract int getPDUMisOrd();

    public abstract int getPDUInvalid();

    public abstract int getPDUDuplicate();
}
