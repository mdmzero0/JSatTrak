// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NumericControlAdapter.java

package com.sun.media.controls;

import java.awt.Component;
import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            AtomicControlAdapter, NumericControl

public class NumericControlAdapter extends AtomicControlAdapter
    implements NumericControl
{

    public NumericControlAdapter()
    {
        super(null, true, null);
        lowerLimit = 0.0F;
        upperLimit = 1.0F;
        defaultValue = 0.5F;
        granularity = 0.001F;
        logarithmic = false;
    }

    public NumericControlAdapter(float ll, float ul, float dv, float gran, boolean log, Component comp, boolean def, 
            Control parent)
    {
        super(comp, def, parent);
        lowerLimit = ll;
        upperLimit = ul;
        defaultValue = dv;
        granularity = gran;
        logarithmic = log;
    }

    public float getLowerLimit()
    {
        return lowerLimit;
    }

    public float getUpperLimit()
    {
        return upperLimit;
    }

    public float getValue()
    {
        return 0.0F;
    }

    public float setValue(float value)
    {
        return value;
    }

    public float getDefaultValue()
    {
        return defaultValue;
    }

    public float setDefaultValue(float value)
    {
        return defaultValue = value;
    }

    public float getGranularity()
    {
        return granularity;
    }

    public boolean isLogarithmic()
    {
        return logarithmic;
    }

    public float getLogarithmicBase()
    {
        return 0.0F;
    }

    protected float lowerLimit;
    protected float upperLimit;
    protected float defaultValue;
    protected float granularity;
    protected boolean logarithmic;
}
