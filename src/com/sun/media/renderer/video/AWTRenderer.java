// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AWTRenderer.java

package com.sun.media.renderer.video;

import com.sun.media.BasicPlugIn;
import com.sun.media.util.Arch;
import java.awt.*;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.util.Vector;
import javax.media.*;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.renderer.video:
//            BasicVideoRenderer, Blitter

public class AWTRenderer extends BasicVideoRenderer
    implements Blitter
{
    public class LightComponent extends Component
    {

        public synchronized void paint(Graphics g)
        {
            AWTRenderer.this.paint(g);
        }

        public synchronized void update(Graphics g1)
        {
        }

        public Dimension getMinimumSize()
        {
            return new Dimension(1, 1);
        }

        public Dimension getPreferredSize()
        {
            return myPreferredSize();
        }

        public synchronized void addNotify()
        {
            super.addNotify();
            setAvailable(true);
        }

        public synchronized void removeNotify()
        {
            setAvailable(false);
            super.removeNotify();
        }

        public LightComponent()
        {
        }
    }


    public AWTRenderer()
    {
        this("AWT Renderer");
    }

    public AWTRenderer(String name)
    {
        super(name);
        cacheInputData = null;
        cacheInputImage = null;
        cacheOutputImage = null;
        lastImage = null;
        supportedRGB = null;
        supportedOther = null;
        lastWidth = 1;
        lastHeight = 1;
        blitter = null;
        int rMask;
        int gMask;
        int bMask;
        if((Arch.getArch() & 8) != 0 && !runningOnMac)
        {
            rMask = 255;
            gMask = 65280;
            bMask = 0xff0000;
        } else
        {
            bMask = 255;
            gMask = 65280;
            rMask = 0xff0000;
        }
        supportedRGB = new RGBFormat(null, -1, Format.intArray, -1F, 32, rMask, gMask, bMask, 1, -1, 0, -1);
        supportedOther = new RGBFormat(null, -1, Format.intArray, -1F, 32, bMask, gMask, rMask, 1, -1, 0, -1);
        super.supportedFormats = new VideoFormat[2];
        super.supportedFormats[0] = supportedRGB;
        super.supportedFormats[1] = supportedOther;
        if(runningOnMac)
            super.supportedFormats[1] = supportedRGB;
        try
        {
            Class cls = Class.forName("com.sun.media.renderer.video.Java2DRenderer");
            blitter = (Blitter)cls.newInstance();
        }
        catch(Throwable t)
        {
            if(t instanceof ThreadDeath)
                throw (ThreadDeath)t;
            blitter = this;
        }
    }

    public boolean isLightWeight()
    {
        return false;
    }

    public void open()
        throws ResourceUnavailableException
    {
        cacheInputData = new Vector();
        cacheInputImage = new Vector();
        cacheOutputImage = new Vector();
    }

    public synchronized void reset()
    {
        cacheInputData = new Vector();
        cacheInputImage = new Vector();
        cacheOutputImage = new Vector();
    }

    public Format setInputFormat(Format format)
    {
        if(super.setInputFormat(format) != null)
        {
            reset();
            return format;
        } else
        {
            return null;
        }
    }

    protected synchronized int doProcess(Buffer buffer)
    {
        if(super.component == null)
            return 0;
        if(!buffer.getFormat().equals(super.inputFormat))
        {
            Format in = buffer.getFormat();
            if(BasicPlugIn.matches(in, super.supportedFormats) == null)
                return 1;
            super.inputFormat = (RGBFormat)in;
        }
        Object data = buffer.getData();
        if(!(data instanceof int[]))
            return 1;
        int cacheSize = cacheInputData.size();
        boolean found = false;
        int i;
        for(i = 0; i < cacheSize; i++)
        {
            Object bufKnown = cacheInputData.elementAt(i);
            if(bufKnown != data)
                continue;
            found = true;
            break;
        }

        if(!found)
            i = blitter.newData(buffer, cacheInputImage, cacheOutputImage, cacheInputData);
        if(i < 0)
            return 1;
        RGBFormat format = (RGBFormat)buffer.getFormat();
        Dimension size = format.getSize();
        super.inWidth = size.width;
        super.inHeight = size.height;
        if(super.outWidth == -1)
            super.outWidth = size.width;
        if(super.outHeight == -1)
            super.outHeight = size.height;
        lastImage = blitter.process(buffer, cacheInputImage.elementAt(i), cacheOutputImage.elementAt(i), size);
        lastWidth = size.width;
        lastHeight = size.height;
        if(!isLightWeight())
        {
            Graphics g = super.component.getGraphics();
            if(g != null)
                blitter.draw(g, super.component, lastImage, 0, 0, super.outWidth, super.outHeight, 0, 0, size.width, size.height);
        } else
        {
            super.component.repaint();
        }
        return 0;
    }

    protected void repaint()
    {
        if(!isStarted() && lastImage != null)
        {
            Graphics g = super.component.getGraphics();
            blitter.draw(g, super.component, lastImage, 0, 0, super.outWidth, super.outHeight, 0, 0, lastWidth, lastHeight);
        }
    }

    public Component getComponent()
    {
        if(super.component == null)
            if(isLightWeight())
            {
                super.component = new LightComponent();
                super.component.setBackground(getPreferredBackground());
                if(super.compListener == null)
                    super.compListener = new BasicVideoRenderer.CompListener(this);
                super.component.addComponentListener(super.compListener);
            } else
            {
                super.component = super.getComponent();
            }
        return super.component;
    }

    public synchronized void resized(Component c)
    {
        super.resized(c);
        if(blitter != this)
            blitter.resized(c);
    }

    public Image process(Buffer buffer, Object cacheInputImage, Object cacheOutputImage, Dimension size)
    {
        MemoryImageSource sourceImage = (MemoryImageSource)cacheInputImage;
        Image lastImage = (Image)cacheOutputImage;
        sourceImage.newPixels(0, 0, size.width, size.height);
        return lastImage;
    }

    public void draw(Graphics g, Component component, Image lastImage, int dx, int dy, int dw, int dh, 
            int sx, int sy, int sw, int sh)
    {
        if(g != null)
            g.drawImage(lastImage, dx, dy, dw, dh, sx, sy, sw, sh, component);
    }

    public void paint(Graphics g)
    {
        if(g != null && lastImage != null)
            blitter.draw(g, super.component, lastImage, 0, 0, super.outWidth, super.outHeight, 0, 0, lastWidth, lastHeight);
    }

    public int newData(Buffer buffer, Vector cacheInputImage, Vector cacheOutputImage, Vector cacheInputData)
    {
        Object data = buffer.getData();
        if(!(data instanceof int[]))
            return -1;
        RGBFormat format = (RGBFormat)buffer.getFormat();
        DirectColorModel dcm = new DirectColorModel(format.getBitsPerPixel(), format.getRedMask(), format.getGreenMask(), format.getBlueMask());
        MemoryImageSource sourceImage = new MemoryImageSource(format.getLineStride(), format.getSize().height, dcm, (int[])data, 0, format.getLineStride());
        sourceImage.setAnimated(true);
        sourceImage.setFullBufferUpdates(true);
        Image destImage = null;
        if(super.component != null)
        {
            destImage = super.component.createImage(sourceImage);
            super.component.prepareImage(destImage, super.component);
        }
        cacheOutputImage.addElement(destImage);
        cacheInputData.addElement(data);
        cacheInputImage.addElement(sourceImage);
        return cacheInputImage.size() - 1;
    }

    private static final String MyName = "AWT Renderer";
    private transient Vector cacheInputData;
    private transient Vector cacheInputImage;
    private transient Vector cacheOutputImage;
    private transient Image lastImage;
    private RGBFormat supportedRGB;
    private RGBFormat supportedOther;
    private transient int lastWidth;
    private transient int lastHeight;
    private Blitter blitter;
    public static String vendor;
    public static boolean runningOnMac = false;

    static 
    {
        vendor = null;
        try
        {
            vendor = System.getProperty("java.vendor");
            if(vendor != null)
            {
                vendor = vendor.toUpperCase();
                if(vendor.startsWith("APPLE"))
                    runningOnMac = true;
            }
        }
        catch(Throwable e) { }
    }
}
