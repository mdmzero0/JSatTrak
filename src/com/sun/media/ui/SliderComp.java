// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SliderComp.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Referenced classes of package com.sun.media.ui:
//            BasicComp, Scroll

public class SliderComp extends BasicComp
    implements ActionListener
{

    public SliderComp(String label, float min, float max, float initial)
    {
        super(label);
        minValue = min;
        maxValue = max;
        initialValue = initial;
        value = initial;
        setLayout(new BorderLayout());
        Label lab = new Label(label, 0);
        add("West", lab);
        scroll = new Scroll();
        add("Center", scroll);
        scroll.setActionListener(this);
        scroll.setValue(toRatio(value));
    }

    public void setValue(int value)
    {
        this.value = value;
        scroll.setValue(toRatio(value));
    }

    public void setValue(float value)
    {
        this.value = value;
        scroll.setValue(toRatio(value));
    }

    public int getIntValue()
    {
        return (int)value;
    }

    public float getFloatValue()
    {
        return value;
    }

    public void actionPerformed(ActionEvent ae)
    {
        float scrollValue = scroll.getValue();
        value = fromRatio(scrollValue);
        informListener();
    }

    private float toRatio(float value)
    {
        float diff = maxValue - minValue;
        return (value - minValue) / diff;
    }

    private float fromRatio(float value)
    {
        return value * (maxValue - minValue) + minValue;
    }

    float value;
    float minValue;
    float maxValue;
    float initialValue;
    Scroll scroll;
    TextField tfIndicator;
    private static final int MIN = 0;
    private static final int MAX = 1000;
    private static final int PAGESIZE = 100;
}
