// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ColorControlAdapter.java

package com.sun.media.controls;

import java.awt.Component;
import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            AtomicControlAdapter, ColorControl, NumericControl, BooleanControl

public class ColorControlAdapter extends AtomicControlAdapter
    implements ColorControl
{

    public ColorControlAdapter(NumericControl b, NumericControl c, NumericControl s, NumericControl h, BooleanControl g, Component comp, boolean def, 
            Control parent)
    {
        super(comp, def, parent);
        brightness = b;
        contrast = c;
        saturation = s;
        hue = h;
        grayscale = g;
        int n = 0;
        n += b != null ? 1 : 0;
        n += c != null ? 1 : 0;
        n += s != null ? 1 : 0;
        n += h != null ? 1 : 0;
        n += g != null ? 1 : 0;
        controls = new Control[n];
        n = 0;
        if(b != null)
            controls[n++] = b;
        if(c != null)
            controls[n++] = c;
        if(s != null)
            controls[n++] = s;
        if(h != null)
            controls[n++] = h;
        if(g != null)
            controls[n++] = g;
    }

    public Control[] getControls()
    {
        return controls;
    }

    public NumericControl getBrightness()
    {
        return brightness;
    }

    public NumericControl getContrast()
    {
        return contrast;
    }

    public NumericControl getSaturation()
    {
        return saturation;
    }

    public NumericControl getHue()
    {
        return hue;
    }

    public BooleanControl getGrayscale()
    {
        return grayscale;
    }

    NumericControl brightness;
    NumericControl contrast;
    NumericControl saturation;
    NumericControl hue;
    BooleanControl grayscale;
    Control controls[];
}
