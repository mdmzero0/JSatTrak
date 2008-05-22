// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StateTransistor.java

package com.sun.media;

import javax.media.Time;

public interface StateTransistor
{

    public abstract boolean doRealize();

    public abstract void doFailedRealize();

    public abstract void abortRealize();

    public abstract boolean doPrefetch();

    public abstract void doFailedPrefetch();

    public abstract void abortPrefetch();

    public abstract void doStart();

    public abstract void doStop();

    public abstract void doDealloc();

    public abstract void doClose();

    public abstract void doSetMediaTime(Time time);

    public abstract float doSetRate(float f);
}
