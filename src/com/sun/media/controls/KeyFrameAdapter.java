// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   KeyFrameAdapter.java

package com.sun.media.controls;

import com.sun.media.ui.BasicComp;
import com.sun.media.ui.TextComp;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.media.control.KeyFrameControl;

public class KeyFrameAdapter
    implements KeyFrameControl, ActionListener
{

    public KeyFrameAdapter(int preferredInterval, boolean settable)
    {
        textComp = null;
        preferred = preferredInterval;
        this.settable = settable;
        value = preferred;
    }

    public int getKeyFrameInterval()
    {
        return value;
    }

    public int setKeyFrameInterval(int newValue)
    {
        if(settable)
        {
            if(newValue < 1)
                newValue = 1;
            value = newValue;
            if(textComp != null)
                textComp.setValue(Integer.toString(value));
            return value;
        } else
        {
            return -1;
        }
    }

    public int getPreferredKeyFrameInterval()
    {
        return preferred;
    }

    protected String getName()
    {
        return "Key Frames Every";
    }

    public Component getControlComponent()
    {
        if(textComp == null)
        {
            textComp = new TextComp(getName(), Integer.toString(value), 3, settable);
            textComp.setActionListener(this);
        }
        return textComp;
    }

    public void actionPerformed(ActionEvent ae)
    {
        int newValue = textComp.getIntValue();
        setKeyFrameInterval(newValue);
    }

    int preferred;
    int value;
    boolean settable;
    TextComp textComp;
}
