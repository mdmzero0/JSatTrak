// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VideoFormatChooser.java

package com.sun.media.ui;

import java.awt.Dimension;

class VideoSize extends Dimension
{

    public VideoSize()
    {
    }

    public VideoSize(int nWidth, int nHeight)
    {
        super(nWidth, nHeight);
    }

    public VideoSize(Dimension dim)
    {
        super(dim);
    }

    public boolean equals(Dimension dim)
    {
        boolean boolResult = true;
        if(dim == null)
            boolResult = false;
        if(boolResult)
            boolResult = super.width == dim.width;
        if(boolResult)
            boolResult = super.height == dim.height;
        return boolResult;
    }

    public String toString()
    {
        return "" + super.width + " x " + super.height;
    }
}
