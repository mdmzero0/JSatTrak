// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JPEGRenderer.java

package com.sun.media.renderer.video;

import com.sun.media.*;
import java.awt.*;
import javax.media.*;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.renderer.video:
//            BasicVideoRenderer

public class JPEGRenderer extends BasicVideoRenderer
    implements SlowPlugIn
{

    public void forceToUse()
    {
        forceToFail = false;
    }

    public JPEGRenderer()
    {
        super("JPEG Renderer");
        supportedJPEG = null;
        supportedMJPG = null;
        forceToFail = false;
        if(BasicPlugIn.plugInExists("com.sun.media.codec.video.jpeg.NativeDecoder", 2))
            try
            {
                JMFSecurityManager.loadLibrary("jmutil");
                JMFSecurityManager.loadLibrary("jmjpeg");
                forceToFail = true;
            }
            catch(Throwable t) { }
        supportedJPEG = new VideoFormat("jpeg", null, -1, Format.byteArray, -1F);
        supportedMJPG = new VideoFormat("mjpg", null, -1, Format.byteArray, -1F);
        super.supportedFormats = new VideoFormat[1];
        super.supportedFormats[0] = supportedJPEG;
    }

    public void open()
        throws ResourceUnavailableException
    {
    }

    public void reset()
    {
    }

    public Format setInputFormat(Format format)
    {
        if(forceToFail)
            return null;
        if(super.setInputFormat(format) != null)
        {
            reset();
            return format;
        } else
        {
            return null;
        }
    }

    public synchronized int doProcess(Buffer buffer)
    {
        if(super.component == null)
            return 0;
        if(!buffer.getFormat().equals(super.inputFormat))
        {
            Format in = buffer.getFormat();
            if(!in.matches(supportedJPEG))
                return 1;
            super.inputFormat = (VideoFormat)in;
        }
        Dimension size = super.inputFormat.getSize();
        Object data = buffer.getData();
        if(!(data instanceof byte[]))
            return 1;
        java.awt.Image im = Toolkit.getDefaultToolkit().createImage((byte[])data);
        MediaTracker tracker = new MediaTracker(super.component);
        Dimension d = super.component.getSize();
        super.outWidth = d.width;
        super.outHeight = d.height;
        tracker.addImage(im, 0);
        try
        {
            tracker.waitForAll();
        }
        catch(Exception e) { }
        Graphics g = super.component.getGraphics();
        if(g != null)
            g.drawImage(im, 0, 0, super.outWidth, super.outHeight, 0, 0, size.width, size.height, super.component);
        return 0;
    }

    protected void repaint()
    {
    }

    private static final String MyName = "JPEG Renderer";
    private VideoFormat supportedJPEG;
    private VideoFormat supportedMJPG;
    private boolean forceToFail;
}
