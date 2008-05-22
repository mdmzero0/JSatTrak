// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Module.java

package com.sun.media;

import javax.media.Controls;
import javax.media.Format;

// Referenced classes of package com.sun.media:
//            InputConnector, OutputConnector, Connector, ModuleListener, 
//            JMD

public interface Module
    extends Controls
{

    public abstract String[] getInputConnectorNames();

    public abstract String[] getOutputConnectorNames();

    public abstract InputConnector getInputConnector(String s);

    public abstract OutputConnector getOutputConnector(String s);

    public abstract void registerInputConnector(String s, InputConnector inputconnector);

    public abstract void registerOutputConnector(String s, OutputConnector outputconnector);

    public abstract void reset();

    public abstract void connectorPushed(InputConnector inputconnector);

    public abstract String getName();

    public abstract void setName(String s);

    public abstract void setFormat(Connector connector, Format format);

    public abstract void setModuleListener(ModuleListener modulelistener);

    public abstract boolean isInterrupted();

    public abstract void setJMD(JMD jmd);
}
