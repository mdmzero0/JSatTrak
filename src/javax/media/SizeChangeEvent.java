// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SizeChangeEvent.java

package javax.media;

import javax.media.format.FormatChangeEvent;

// Referenced classes of package javax.media:
//            Controller

public class SizeChangeEvent extends FormatChangeEvent
{

    public SizeChangeEvent(Controller from, int width, int height, float scale)
    {
        super(from);
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public float getScale()
    {
        return scale;
    }

    protected int width;
    protected int height;
    protected float scale;
}
