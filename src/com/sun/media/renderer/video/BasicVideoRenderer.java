// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicVideoRenderer.java

package com.sun.media.renderer.video;

import com.sun.media.BasicPlugIn;
import com.sun.media.ExtBuffer;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import javax.media.*;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.renderer.VideoRenderer;

// Referenced classes of package com.sun.media.renderer.video:
//            HeavyComponent

public abstract class BasicVideoRenderer extends BasicPlugIn
    implements VideoRenderer, FrameGrabbingControl
{
    public class CompListener extends ComponentAdapter
    {

        public void componentResized(ComponentEvent ce)
        {
            resized(ce.getComponent());
        }

        public CompListener()
        {
        }
    }


    public BasicVideoRenderer(String name)
    {
        supportedFormats = null;
        inputFormat = null;
        outWidth = -1;
        outHeight = -1;
        inWidth = -1;
        inHeight = -1;
        component = null;
        compListener = null;
        componentAvailable = false;
        bounds = null;
        started = false;
        controls = null;
        frameGrabber = null;
        lastBuffer = new ExtBuffer();
        lastData = null;
        lastHdr = null;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public Format[] getSupportedInputFormats()
    {
        return supportedFormats;
    }

    public void open()
        throws ResourceUnavailableException
    {
    }

    public void close()
    {
    }

    public void reset()
    {
    }

    public int process(Buffer inbuffer)
    {
        if(inbuffer.getLength() == 0)
            return 0;
        int result;
        synchronized(lastBuffer)
        {
            result = doProcess(inbuffer);
            if(result == 0)
                lastBuffer.copy(inbuffer, true);
        }
        return result;
    }

    protected abstract int doProcess(Buffer buffer);

    public Format setInputFormat(Format format)
    {
        if(BasicPlugIn.matches(format, supportedFormats) != null)
        {
            inputFormat = (VideoFormat)format;
            Dimension size = inputFormat.getSize();
            if(size != null)
            {
                inWidth = size.width;
                inHeight = size.height;
            }
            return format;
        } else
        {
            return null;
        }
    }

    public void start()
    {
        started = true;
    }

    public void stop()
    {
        started = false;
    }

    public Component getComponent()
    {
        if(component == null)
        {
            try
            {
                Class mshc = Class.forName("com.sun.media.renderer.video.MSHeavyComponent");
                if(mshc != null)
                    component = (Component)mshc.newInstance();
            }
            catch(Throwable t)
            {
                component = new HeavyComponent();
            }
            ((HeavyComponent)component).setRenderer(this);
            component.setBackground(getPreferredBackground());
            if(compListener == null)
                compListener = new CompListener();
            component.addComponentListener(compListener);
        }
        return component;
    }

    public synchronized boolean setComponent(Component comp)
    {
        reset();
        component = comp;
        if(compListener == null)
            compListener = new CompListener();
        component.addComponentListener(compListener);
        return true;
    }

    public void setBounds(Rectangle rect)
    {
        bounds = rect;
    }

    public Rectangle getBounds()
    {
        return bounds;
    }

    protected Color getPreferredBackground()
    {
        return Color.black;
    }

    void resized(Component c)
    {
        if(c != null && c == component)
        {
            Dimension d = component.getSize();
            outWidth = d.width;
            outHeight = d.height;
        }
    }

    protected synchronized void setAvailable(boolean on)
    {
        componentAvailable = on;
        if(!componentAvailable)
            removingComponent();
    }

    protected void removingComponent()
    {
    }

    protected Dimension myPreferredSize()
    {
        return new Dimension(inWidth, inHeight);
    }

    protected boolean isStarted()
    {
        return started;
    }

    protected void repaint()
    {
        System.err.println("repaint call not implemented on this renderer");
    }

    public Object[] getControls()
    {
        if(controls != null)
        {
            return controls;
        } else
        {
            frameGrabber = this;
            controls = new Control[1];
            controls[0] = frameGrabber;
            return controls;
        }
    }

    public Component getControlComponent()
    {
        return null;
    }

    public Buffer grabFrame()
    {
        Buffer buffer1;
        synchronized(lastBuffer)
        {
            Buffer newBuffer = new Buffer();
            newBuffer.setFormat(lastBuffer.getFormat());
            newBuffer.setFlags(lastBuffer.getFlags());
            newBuffer.setLength(lastBuffer.getLength());
            newBuffer.setOffset(0);
            newBuffer.setHeader(lastBuffer.getHeader());
            newBuffer.setData(lastBuffer.getData());
            Object data = lastBuffer.getData();
            int length = lastBuffer.getLength();
            Object newData;
            if(data instanceof byte[])
                newData = new byte[length];
            else
            if(data instanceof short[])
                newData = new short[length];
            else
            if(data instanceof int[])
            {
                newData = new int[length];
            } else
            {
                Buffer buffer = newBuffer;
                return buffer;
            }
            System.arraycopy(data, lastBuffer.getOffset(), newData, 0, length);
            newBuffer.setData(newData);
            buffer1 = newBuffer;
        }
        return buffer1;
    }

    protected String name;
    protected transient VideoFormat supportedFormats[];
    protected VideoFormat inputFormat;
    protected int outWidth;
    protected int outHeight;
    protected int inWidth;
    protected int inHeight;
    protected Component component;
    protected ComponentListener compListener;
    protected boolean componentAvailable;
    protected Rectangle bounds;
    protected boolean started;
    protected Control controls[];
    protected FrameGrabbingControl frameGrabber;
    protected ExtBuffer lastBuffer;
    protected Object lastData;
    protected Object lastHdr;
}
