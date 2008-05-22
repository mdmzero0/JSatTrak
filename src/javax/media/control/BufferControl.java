// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BufferControl.java

package javax.media.control;

import javax.media.Control;

public interface BufferControl
    extends Control
{

    public abstract long getBufferLength();

    public abstract long setBufferLength(long l);

    public abstract long getMinimumThreshold();

    public abstract long setMinimumThreshold(long l);

    public abstract void setEnabledThreshold(boolean flag);

    public abstract boolean getEnabledThreshold();

    public static final long DEFAULT_VALUE = -1L;
    public static final long MAX_VALUE = -2L;
}
