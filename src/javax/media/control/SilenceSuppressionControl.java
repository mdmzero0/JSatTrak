// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SilenceSuppressionControl.java

package javax.media.control;

import javax.media.Control;

public interface SilenceSuppressionControl
    extends Control
{

    public abstract boolean getSilenceSuppression();

    public abstract boolean setSilenceSuppression(boolean flag);

    public abstract boolean isSilenceSuppressionSupported();
}
