// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FrameRateAdapter.java

package com.sun.media.controls;

import com.sun.media.Reparentable;
import com.sun.media.ui.BasicComp;
import com.sun.media.ui.TextComp;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import javax.media.control.FrameRateControl;

public class FrameRateAdapter
    implements FrameRateControl, ActionListener, Reparentable
{

    public FrameRateAdapter(float initialFrameRate, float minFrameRate, float maxFrameRate, boolean settable)
    {
        value = 0.0F;
        min = 0.0F;
        max = 0.0F;
        textComp = null;
        owner = null;
        value = initialFrameRate;
        min = minFrameRate;
        max = maxFrameRate;
        this.settable = settable;
    }

    public FrameRateAdapter(Object owner, float initialFrameRate, float minFrameRate, float maxFrameRate, boolean settable)
    {
        this(initialFrameRate, minFrameRate, maxFrameRate, settable);
        this.owner = owner;
    }

    public float getFrameRate()
    {
        return value;
    }

    public float setFrameRate(float newFrameRate)
    {
        if(settable)
        {
            if(newFrameRate < min)
                newFrameRate = min;
            else
            if(newFrameRate > max)
                newFrameRate = max;
            value = newFrameRate;
            if(textComp != null)
                textComp.setValue(Float.toString(value));
            return value;
        } else
        {
            return -1F;
        }
    }

    public float getMaxSupportedFrameRate()
    {
        return max;
    }

    public float getPreferredFrameRate()
    {
        return min;
    }

    protected String getName()
    {
        return "Frame Rate";
    }

    public void setEnabled(boolean enable)
    {
        if(textComp != null)
            textComp.setEnabled(enable);
    }

    public Component getControlComponent()
    {
        if(textComp == null)
        {
            textComp = new TextComp(getName(), value + "", 2, settable);
            textComp.setActionListener(this);
        }
        return textComp;
    }

    public void actionPerformed(ActionEvent ae)
    {
        System.out.println("fra:");
        float newFrameRate = textComp.getFloatValue();
        setFrameRate(newFrameRate);
    }

    public Object getOwner()
    {
        if(owner == null)
            return this;
        else
            return owner;
    }

    public void setOwner(Object newOwner)
    {
        owner = newOwner;
    }

    protected float value;
    protected float min;
    protected float max;
    protected TextComp textComp;
    protected boolean settable;
    protected Object owner;
}
