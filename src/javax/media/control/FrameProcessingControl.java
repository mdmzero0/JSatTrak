// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FrameProcessingControl.java

package javax.media.control;

import javax.media.Control;

public interface FrameProcessingControl
    extends Control
{

    public abstract void setFramesBehind(float f);

    public abstract boolean setMinimalProcessing(boolean flag);

    public abstract int getFramesDropped();
}
