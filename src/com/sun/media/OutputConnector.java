// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   OutputConnector.java

package com.sun.media;

import javax.media.Buffer;
import javax.media.Format;

// Referenced classes of package com.sun.media:
//            Connector, InputConnector

public interface OutputConnector
    extends Connector
{

    public abstract Format connectTo(InputConnector inputconnector, Format format);

    public abstract Format canConnectTo(InputConnector inputconnector, Format format);

    public abstract InputConnector getInputConnector();

    public abstract boolean isEmptyBufferAvailable();

    public abstract Buffer getEmptyBuffer();

    public abstract void writeReport();
}
