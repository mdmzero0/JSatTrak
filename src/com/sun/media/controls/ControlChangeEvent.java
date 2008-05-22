// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ControlChangeEvent.java

package com.sun.media.controls;

import javax.media.Control;

public class ControlChangeEvent
{

    public ControlChangeEvent(Control c)
    {
        this.c = c;
    }

    public Control getControl()
    {
        return c;
    }

    private Control c;
}
