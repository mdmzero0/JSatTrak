// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BitRateAdapter.java

package com.sun.media.controls;

import com.sun.media.ui.BasicComp;
import com.sun.media.ui.TextComp;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.media.control.BitRateControl;

public class BitRateAdapter
    implements BitRateControl, ActionListener
{

    public BitRateAdapter(int initialBitRate, int minBitRate, int maxBitRate, boolean settable)
    {
        value = initialBitRate;
        min = minBitRate;
        max = maxBitRate;
        this.settable = settable;
    }

    public int getBitRate()
    {
        return value;
    }

    public int setBitRate(int newValue)
    {
        if(settable)
        {
            if(newValue < min)
                newValue = min;
            if(newValue > max)
                newValue = max;
            value = newValue;
            if(textComp != null)
                textComp.setValue(Integer.toString(newValue));
            return value;
        } else
        {
            return -1;
        }
    }

    public int getMinSupportedBitRate()
    {
        return min;
    }

    public int getMaxSupportedBitRate()
    {
        return max;
    }

    protected String getName()
    {
        return "Bit Rate";
    }

    public Component getControlComponent()
    {
        if(textComp == null)
        {
            textComp = new TextComp(getName(), Integer.toString(value), 7, settable);
            textComp.setActionListener(this);
        }
        return textComp;
    }

    public void actionPerformed(ActionEvent ae)
    {
        if(textComp != null)
            setBitRate(textComp.getIntValue());
    }

    protected int value;
    protected int min;
    protected int max;
    protected boolean settable;
    protected TextComp textComp;
}
