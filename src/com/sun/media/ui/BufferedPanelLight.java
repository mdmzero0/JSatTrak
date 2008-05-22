// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BufferedPanelLight.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.peer.LightweightPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;

// Referenced classes of package com.sun.media.ui:
//            Region

public class BufferedPanelLight extends Container
{

    public BufferedPanelLight(LayoutManager layout)
    {
        lock = new Object();
        setLayout(layout);
        buffered = true;
        autoFlushing = true;
        background = null;
        windowCreated = false;
        buffer = null;
        bufferGraphics = null;
        damage = new Region();
    }

    public BufferedPanelLight()
    {
        this(null);
    }

    public boolean isBuffered()
    {
        return buffered;
    }

    public void setBuffered(boolean buffered)
    {
        if(buffered != this.buffered)
        {
            this.buffered = buffered;
            if(buffered)
                repaint();
        }
    }

    public boolean isAutoFlushing()
    {
        return autoFlushing;
    }

    public void setAutoFlushing(boolean flushing)
    {
        if(flushing != autoFlushing)
            autoFlushing = flushing;
    }

    public Image getBackgroundTile()
    {
        return background;
    }

    public void setBackgroundTile(Image background)
    {
        this.background = background;
        repaint();
    }

    public void addNotify()
    {
        super.addNotify();
        windowCreated = true;
        if(buffered)
        {
            createBufferImage();
            repaint();
        }
    }

    public void reshape(int x, int y, int width, int height)
    {
        Rectangle old = getBounds();
        super.reshape(x, y, width, height);
        if(windowCreated && (width != old.width || height != old.height) && buffered)
        {
            createBufferImage();
            repaint();
        }
    }

    public void flushBuffer()
    {
        Dimension size = getSize();
        super.repaint(0L, 0, 0, size.width, size.height);
    }

    void createBufferImage()
    {
        Dimension size = getSize();
        if(size.width > 0 && size.height > 0)
        {
            buffer = createImage(size.width, size.height);
            if(buffer != null)
                bufferGraphics = buffer.getGraphics();
        }
    }

    protected void renderBuffer()
    {
        if(damage.isEmpty())
            return;
        if(buffer == null)
            return;
        Region rects;
        synchronized(damage)
        {
            rects = damage;
            damage = new Region();
        }
        Rectangle rect;
        for(Enumeration e = rects.rectangles(); e.hasMoreElements(); render(rect))
            rect = (Rectangle)e.nextElement();

    }

    protected void render(Rectangle rect)
    {
        Component children[] = getComponents();
        synchronized(buffer)
        {
            bufferGraphics.setClip(rect);
            paintBackground(bufferGraphics);
            bufferGraphics.setColor(getForeground());
            for(int c = children.length - 1; c >= 0; c--)
            {
                Component child = children[c];
                if(isLightweight(child) && child.isVisible())
                {
                    Rectangle clip = child.getBounds();
                    if(clip.intersects(rect))
                    {
                        Graphics g = bufferGraphics.create(clip.x, clip.y, clip.width, clip.height);
                        child.paint(g);
                        g.dispose();
                    }
                }
            }

            bufferGraphics.setClip(0, 0, getSize().width, getSize().height);
        }
    }

    protected void paintBackground(Graphics g)
    {
        Dimension size = getSize();
        if(background == null)
        {
            g.setColor(getBackground());
            g.fillRect(0, 0, size.width, size.height);
        } else
        {
            Rectangle tile = new Rectangle(0, 0, background.getWidth(this), background.getHeight(this));
            Rectangle clip = g.getClipBounds();
            for(; tile.y < size.height; tile.y += tile.height)
            {
                while(tile.x < size.width) 
                {
                    if(clip == null || clip.intersects(tile))
                        g.drawImage(background, tile.x, tile.y, this);
                    tile.x += tile.width;
                }
                tile.x = 0;
            }

        }
    }

    boolean isLightweight(Component comp)
    {
        return comp.getPeer() instanceof LightweightPeer;
    }

    public void repaint(long time, int x, int y, int width, int height)
    {
        if(buffered)
        {
            synchronized(damage)
            {
                damage.addRectangle(new Rectangle(x, y, width, height));
            }
            if(autoFlushing)
                flushBuffer();
        } else
        {
            super.repaint(time, x, y, width, height);
        }
    }

    public void update(Graphics g)
    {
        if(buffered)
            paint(g);
        else
            super.update(g);
    }

    public void paint(Graphics g)
    {
        if(buffered && buffer != null)
        {
            renderBuffer();
            g.drawImage(buffer, 0, 0, this);
        } else
        {
            super.paint(g);
        }
    }

    private void readObject(ObjectInputStream is)
        throws IOException, ClassNotFoundException
    {
        is.defaultReadObject();
        damage = new Region();
    }

    protected boolean buffered;
    protected boolean autoFlushing;
    protected Image background;
    protected boolean windowCreated;
    protected transient Image buffer;
    protected transient Graphics bufferGraphics;
    protected transient Region damage;
    protected Object lock;
}
