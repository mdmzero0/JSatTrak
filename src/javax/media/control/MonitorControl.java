// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MonitorControl.java

package javax.media.control;

import javax.media.Control;

public interface MonitorControl
    extends Control
{

    public abstract boolean setEnabled(boolean flag);

    public abstract float setPreviewFrameRate(float f);
}
