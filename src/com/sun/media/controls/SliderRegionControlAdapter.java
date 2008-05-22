// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SliderRegionControlAdapter.java

package com.sun.media.controls;

import java.awt.Component;
import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            AtomicControlAdapter, SliderRegionControl

public class SliderRegionControlAdapter extends AtomicControlAdapter
    implements SliderRegionControl
{

    public SliderRegionControlAdapter()
    {
        super(null, true, null);
        enable = true;
    }

    public SliderRegionControlAdapter(Component c, boolean def, Control parent)
    {
        super(c, def, parent);
    }

    public long setMinValue(long value)
    {
        min = value;
        informListeners();
        return min;
    }

    public long getMinValue()
    {
        return min;
    }

    public long setMaxValue(long value)
    {
        max = value;
        informListeners();
        return max;
    }

    public long getMaxValue()
    {
        return max;
    }

    public boolean isEnable()
    {
        return enable;
    }

    public void setEnable(boolean f)
    {
        enable = f;
    }

    long min;
    long max;
    boolean enable;
}
