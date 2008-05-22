// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Connector.java

package com.sun.media;

import javax.media.Format;

// Referenced classes of package com.sun.media:
//            Module

public interface Connector
{

    public abstract void setFormat(Format format);

    public abstract Format getFormat();

    public abstract void setSize(int i);

    public abstract int getSize();

    public abstract void reset();

    public abstract String getName();

    public abstract void setName(String s);

    public abstract void setProtocol(int i);

    public abstract int getProtocol();

    public abstract Object getCircularBuffer();

    public abstract void setCircularBuffer(Object obj);

    public abstract void setModule(Module module);

    public abstract Module getModule();

    public static final int ProtocolPush = 0;
    public static final int ProtocolSafe = 1;
}
