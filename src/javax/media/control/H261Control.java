// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   H261Control.java

package javax.media.control;

import javax.media.Control;

public interface H261Control
    extends Control
{

    public abstract boolean isStillImageTransmissionSupported();

    public abstract boolean setStillImageTransmission(boolean flag);

    public abstract boolean getStillImageTransmission();
}
