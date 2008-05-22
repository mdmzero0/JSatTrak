// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AtomicControl.java

package com.sun.media.controls;

import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            ControlChangeListener

public interface AtomicControl
    extends Control
{

    public abstract boolean isDefault();

    public abstract void setVisible(boolean flag);

    public abstract boolean getVisible();

    public abstract void setEnabled(boolean flag);

    public abstract boolean getEnabled();

    public abstract Control getParent();

    public abstract void addControlChangeListener(ControlChangeListener controlchangelistener);

    public abstract void removeControlChangeListener(ControlChangeListener controlchangelistener);

    public abstract boolean isReadOnly();

    public abstract String getTip();

    public abstract void setTip(String s);
}
