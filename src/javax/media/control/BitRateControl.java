// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BitRateControl.java

package javax.media.control;

import javax.media.Control;

public interface BitRateControl
    extends Control
{

    public abstract int getBitRate();

    public abstract int setBitRate(int i);

    public abstract int getMinSupportedBitRate();

    public abstract int getMaxSupportedBitRate();
}
