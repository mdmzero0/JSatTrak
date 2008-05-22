// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GenerateImage.java

package com.sun.media.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.*;

public class GenerateImage
{

    private native int getColors(byte abyte0[], int i);

    private native boolean generateImage(String s);

    public GenerateImage()
    {
        icm = null;
        dcm = null;
        image = null;
        data = null;
        reds = new byte[256];
        greens = new byte[256];
        blues = new byte[256];
        int ncolors = getColors(reds, 0);
        getColors(greens, 1);
        getColors(blues, 2);
        icm = new IndexColorModel(8, 256, reds, greens, blues, 0);
    }

    public Image getImage(String imageName)
    {
        image = null;
        data = null;
        if(generateImage(imageName))
        {
            createImage();
            return image;
        } else
        {
            return null;
        }
    }

    protected synchronized void createBuffer(int w, int h)
    {
        width = w;
        height = h;
        data = new byte[w * h];
    }

    protected synchronized void createImage()
    {
        MemoryImageSource mis = new MemoryImageSource(width, height, icm, data, 0, width);
        Toolkit tk = Toolkit.getDefaultToolkit();
        image = tk.createImage(mis);
        tk.prepareImage(image, width, height, null);
    }

    private IndexColorModel icm;
    private DirectColorModel dcm;
    private Image image;
    private byte data[];
    private int width;
    private int height;
    byte reds[];
    byte greens[];
    byte blues[];
}
