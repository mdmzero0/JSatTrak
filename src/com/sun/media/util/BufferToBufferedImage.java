// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BufferToBufferedImage.java

package com.sun.media.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import javax.media.Buffer;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

public class BufferToBufferedImage extends BufferToImage
{

    public BufferToBufferedImage()
        throws ClassNotFoundException
    {
        super(null);
        Class.forName("java.awt.Graphics2D");
    }

    public BufferToBufferedImage(VideoFormat videoformat)
        throws ClassNotFoundException
    {
        super(videoformat);
        Class.forName("java.awt.Graphics2D");
    }

    public Image createImage(Buffer buffer)
    {
        RGBFormat rgbformat = (RGBFormat)buffer.getFormat();
        Object obj = buffer.getData();
        int i = rgbformat.getRedMask();
        int j = rgbformat.getGreenMask();
        int k = rgbformat.getBlueMask();
        int ai[] = new int[3];
        ai[0] = i;
        ai[1] = j;
        ai[2] = k;
        DataBufferInt databufferint = new DataBufferInt((int[])obj, rgbformat.getLineStride() * rgbformat.getSize().height);
        SinglePixelPackedSampleModel singlepixelpackedsamplemodel = new SinglePixelPackedSampleModel(3, rgbformat.getLineStride(), rgbformat.getSize().height, ai);
        java.awt.image.WritableRaster writableraster = Raster.createWritableRaster(singlepixelpackedsamplemodel, databufferint, new Point(0, 0));
        DirectColorModel directcolormodel = new DirectColorModel(24, i, j, k);
        BufferedImage bufferedimage = new BufferedImage(directcolormodel, writableraster, true, null);
        AffineTransform affinetransform = new AffineTransform(1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
        AffineTransformOp affinetransformop = new AffineTransformOp(affinetransform, null);
        BufferedImage bufferedimage1 = affinetransformop.createCompatibleDestImage(bufferedimage, directcolormodel);
        affinetransformop.filter(bufferedimage, bufferedimage1);
        return bufferedimage1;
    }
}
