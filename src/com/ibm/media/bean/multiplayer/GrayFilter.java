// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ImageButton.java

package com.ibm.media.bean.multiplayer;

import java.awt.image.RGBImageFilter;

class GrayFilter extends RGBImageFilter
{

    public GrayFilter()
    {
        darkness = 0xffafafaf;
        super.canFilterIndexColorModel = true;
    }

    public GrayFilter(int darkness)
    {
        this();
        this.darkness = darkness;
    }

    public int filterRGB(int x, int y, int rgb)
    {
        return rgb & darkness;
    }

    private int darkness;
}
