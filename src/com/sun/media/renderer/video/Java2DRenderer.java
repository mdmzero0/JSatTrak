// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Java2DRenderer.java

package com.sun.media.renderer.video;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.renderer.video:
//            Blitter

public class Java2DRenderer
    implements Blitter
{

    public Java2DRenderer()
    {
        savedATO = null;
        dcm = null;
        destImage = null;
        try
        {
            Class.forName("java.awt.Graphics2D");
        }
        catch(ClassNotFoundException _ex)
        {
            throw new RuntimeException("No Java2D");
        }
    }

    public synchronized void draw(Graphics g, Component component, Image image, int i, int j, int k, int l, 
            int i1, int j1, int k1, int l1)
    {
        if(image == null || k < 1 || l < 1)
            return;
        if(savedATO == null)
        {
            AffineTransform affinetransform = new AffineTransform((float)k / (float)k1, 0.0F, 0.0F, (float)l / (float)l1, 0.0F, 0.0F);
            AffineTransformOp affinetransformop = new AffineTransformOp(affinetransform, null);
            savedATO = affinetransformop;
            destImage = affinetransformop.createCompatibleDestImage((BufferedImage)image, dcm);
        }
        savedATO.filter((BufferedImage)image, (BufferedImage)destImage);
        if(g != null && image != null && (g instanceof Graphics2D))
            ((Graphics2D)g).drawImage(destImage, 0, 0, component);
    }

    public int newData(Buffer buffer, Vector vector, Vector vector1, Vector vector2)
    {
        Object obj = buffer.getData();
        if(!(obj instanceof int[]))
            return -1;
        RGBFormat rgbformat = (RGBFormat)buffer.getFormat();
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
        dcm = new DirectColorModel(24, i, j, k);
        BufferedImage bufferedimage = new BufferedImage(dcm, writableraster, true, null);
        vector2.addElement(obj);
        vector.addElement(bufferedimage);
        vector1.addElement(bufferedimage);
        synchronized(this)
        {
            savedATO = null;
        }
        return vector.size() - 1;
    }

    public Image process(Buffer buffer, Object obj, Object obj1, Dimension dimension)
    {
        return (Image)obj;
    }

    public void resized(Component component)
    {
        savedATO = null;
    }

    private transient AffineTransformOp savedATO;
    private transient DirectColorModel dcm;
    private transient Image destImage;
}
