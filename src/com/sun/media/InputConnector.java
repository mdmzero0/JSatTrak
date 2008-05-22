// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   InputConnector.java

package com.sun.media;

import javax.media.Buffer;

// Referenced classes of package com.sun.media:
//            Connector, OutputConnector

public interface InputConnector
    extends Connector
{

    public abstract OutputConnector getOutputConnector();

    public abstract void setOutputConnector(OutputConnector outputconnector);

    public abstract boolean isValidBufferAvailable();

    public abstract Buffer getValidBuffer();

    public abstract void readReport();
}
