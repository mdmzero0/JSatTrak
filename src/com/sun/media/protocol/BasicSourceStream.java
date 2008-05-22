// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicSourceStream.java

package com.sun.media.protocol;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.SourceStream;

public class BasicSourceStream
    implements SourceStream
{

    public BasicSourceStream()
    {
        contentDescriptor = null;
        contentLength = -1L;
        controls = new Object[0];
    }

    public BasicSourceStream(ContentDescriptor cd, long contentLength)
    {
        contentDescriptor = null;
        this.contentLength = -1L;
        controls = new Object[0];
        contentDescriptor = cd;
        this.contentLength = contentLength;
    }

    public ContentDescriptor getContentDescriptor()
    {
        return contentDescriptor;
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public boolean endOfStream()
    {
        return false;
    }

    public Object[] getControls()
    {
        return controls;
    }

    public Object getControl(String controlType)
    {
        try
        {
            Class cls = Class.forName(controlType);
            Object cs[] = getControls();
            for(int i = 0; i < cs.length; i++)
                if(cls.isInstance(cs[i]))
                    return cs[i];

            return null;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    protected ContentDescriptor contentDescriptor;
    protected long contentLength;
    protected Object controls[];
    public static final int LENGTH_DISCARD = -2;
}
