// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicConnector.java

package com.sun.media;

import javax.media.Format;

// Referenced classes of package com.sun.media:
//            CircularBuffer, Connector, Module

public abstract class BasicConnector
    implements Connector
{

    public BasicConnector()
    {
        module = null;
        minSize = 1;
        format = null;
        circularBuffer = null;
        name = null;
        protocol = 0;
    }

    public Object getCircularBuffer()
    {
        return circularBuffer;
    }

    public void setCircularBuffer(Object cicularBuffer)
    {
        circularBuffer = (CircularBuffer)cicularBuffer;
    }

    public void setFormat(Format format)
    {
        module.setFormat(this, format);
        this.format = format;
    }

    public Format getFormat()
    {
        return format;
    }

    public Module getModule()
    {
        return module;
    }

    public void setModule(Module module)
    {
        this.module = module;
    }

    public void setSize(int numOfBufferObjects)
    {
        minSize = numOfBufferObjects;
    }

    public int getSize()
    {
        return minSize;
    }

    public void reset()
    {
        circularBuffer.reset();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setProtocol(int protocol)
    {
        this.protocol = protocol;
    }

    public int getProtocol()
    {
        return protocol;
    }

    public void print()
    {
        circularBuffer.print();
    }

    protected Module module;
    protected int minSize;
    protected Format format;
    protected CircularBuffer circularBuffer;
    protected String name;
    protected int protocol;
}
