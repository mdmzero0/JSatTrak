// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   QualityAdapter.java

package com.sun.media.controls;

import com.sun.media.ui.BasicComp;
import com.sun.media.ui.SliderComp;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.media.control.QualityControl;

public class QualityAdapter
    implements QualityControl, ActionListener
{

    public QualityAdapter(float preferred, float min, float max, boolean settable)
    {
        this(preferred, min, max, false, settable);
    }

    public QualityAdapter(float preferred, float min, float max, boolean isTSsupported, boolean settable)
    {
        sliderComp = null;
        scale = 100F;
        preferredValue = preferred;
        minValue = min;
        maxValue = max;
        value = preferred;
        this.settable = settable;
        this.isTSsupported = isTSsupported;
    }

    public float getQuality()
    {
        return value;
    }

    public float setQuality(float newValue)
    {
        if(newValue < minValue)
            newValue = minValue;
        else
        if(newValue > maxValue)
            newValue = maxValue;
        value = newValue;
        if(sliderComp != null)
            sliderComp.setValue(value * scale);
        if(settable)
            return value;
        else
            return -1F;
    }

    public float getPreferredQuality()
    {
        return preferredValue;
    }

    public boolean isTemporalSpatialTradeoffSupported()
    {
        return isTSsupported;
    }

    protected String getName()
    {
        return "Quality";
    }

    public Component getControlComponent()
    {
        if(sliderComp == null)
        {
            sliderComp = new SliderComp(getName(), minValue * scale, maxValue * scale, value * scale);
            sliderComp.setActionListener(this);
        }
        return sliderComp;
    }

    public void actionPerformed(ActionEvent ae)
    {
        float newValue = sliderComp.getFloatValue() / scale;
        setQuality(newValue);
    }

    protected float preferredValue;
    protected float minValue;
    protected float maxValue;
    protected float value;
    protected boolean settable;
    protected boolean isTSsupported;
    protected SliderComp sliderComp;
    private float scale;
}
