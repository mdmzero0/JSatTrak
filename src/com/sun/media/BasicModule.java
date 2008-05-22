// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicModule.java

package com.sun.media;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.media.*;

// Referenced classes of package com.sun.media:
//            InputConnector, OutputConnector, PlaybackEngine, Module, 
//            StateTransistor, Connector, BasicController, ModuleListener, 
//            JMD

public abstract class BasicModule
    implements Module, StateTransistor
{
    class Registry extends Hashtable
    {

        String[] getNames()
        {
            Enumeration namesEnum = keys();
            String namesArray[] = new String[size()];
            for(int i = 0; i < size(); i++)
                namesArray[i] = (String)namesEnum.nextElement();

            return namesArray;
        }

        void put(String name, Connector connector)
        {
            if(containsKey(name))
                throw new RuntimeException("Connector '" + name + "' already exists in Module '" + getClass().getName() + "::" + name + "'");
            if(def == null)
                def = connector;
            super.put(name, connector);
        }

        Object get(String name)
        {
            if(name == null)
                return def;
            else
                return super.get(name);
        }

        Connector[] getConnectors()
        {
            Enumeration connectorsEnum = elements();
            Connector connectorsArray[] = new Connector[size()];
            for(int i = 0; i < size(); i++)
                connectorsArray[i] = (Connector)connectorsEnum.nextElement();

            return connectorsArray;
        }

        Connector def;

        Registry()
        {
            def = null;
        }
    }


    public BasicModule()
    {
        inputConnectors = new Registry();
        outputConnectors = new Registry();
        protocol = 0;
        name = null;
        resetted = false;
        prefetchFailed = false;
        jmd = null;
    }

    public boolean doRealize()
    {
        return true;
    }

    public void doFailedRealize()
    {
    }

    public void abortRealize()
    {
    }

    public void connectorPushed(InputConnector inputConnector)
    {
        process();
    }

    public boolean doPrefetch()
    {
        resetted = false;
        return true;
    }

    public void doFailedPrefetch()
    {
    }

    public void abortPrefetch()
    {
    }

    public void doStart()
    {
        resetted = false;
    }

    public void doStop()
    {
    }

    public void doDealloc()
    {
    }

    public void doClose()
    {
    }

    public void doSetMediaTime(Time time)
    {
    }

    public float doSetRate(float r)
    {
        return r;
    }

    public Object[] getControls()
    {
        return null;
    }

    public Object getControl(String s)
    {
        return null;
    }

    public void setModuleListener(ModuleListener listener)
    {
        moduleListener = listener;
    }

    public void setFormat(Connector connector1, Format format1)
    {
    }

    public String[] getInputConnectorNames()
    {
        return inputConnectors.getNames();
    }

    public String[] getOutputConnectorNames()
    {
        return outputConnectors.getNames();
    }

    public InputConnector getInputConnector(String connectorName)
    {
        return (InputConnector)inputConnectors.get(connectorName);
    }

    public OutputConnector getOutputConnector(String connectorName)
    {
        return (OutputConnector)outputConnectors.get(connectorName);
    }

    public void registerInputConnector(String name, InputConnector inputConnector)
    {
        inputConnectors.put(name, inputConnector);
        inputConnector.setModule(this);
    }

    public void registerOutputConnector(String name, OutputConnector outputConnector)
    {
        outputConnectors.put(name, outputConnector);
        outputConnector.setModule(this);
    }

    public void reset()
    {
        resetted = true;
    }

    protected boolean verifyBuffer(Buffer buffer)
    {
        if(buffer.isDiscard())
            return true;
        Object data = buffer.getData();
        if(buffer.getLength() < 0)
            System.err.println("warning: data length shouldn't be negative: " + buffer.getLength());
        if(data == null)
        {
            System.err.println("warning: data buffer is null");
            if(buffer.getLength() != 0)
            {
                System.err.println("buffer advertized length = " + buffer.getLength() + " but data buffer is null!");
                return false;
            }
        } else
        if(data instanceof byte[])
        {
            if(buffer.getLength() > ((byte[])data).length)
            {
                System.err.println("buffer advertized length = " + buffer.getLength() + " but actual length = " + ((byte[])data).length);
                return false;
            }
        } else
        if((data instanceof int[]) && buffer.getLength() > ((int[])data).length)
        {
            System.err.println("buffer advertized length = " + buffer.getLength() + " but actual length = " + ((int[])data).length);
            return false;
        }
        return true;
    }

    public final boolean isInterrupted()
    {
        return controller != null ? controller.isInterrupted() : false;
    }

    public boolean isThreaded()
    {
        return true;
    }

    public boolean canRun()
    {
        for(int i = 0; i < inputConnectorsArray.length; i++)
            if(!inputConnectorsArray[i].isValidBufferAvailable())
                return false;

        for(int i = 0; i < outputConnectorsArray.length; i++)
            if(!outputConnectorsArray[i].isEmptyBufferAvailable())
                return false;

        return true;
    }

    protected abstract void process();

    protected void error()
    {
        throw new RuntimeException(getClass().getName() + " error");
    }

    public final BasicController getController()
    {
        return controller;
    }

    public final void setController(BasicController c)
    {
        controller = c;
    }

    public final int getState()
    {
        return controller.getState();
    }

    public final String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setJMD(JMD jmd)
    {
        this.jmd = jmd;
    }

    public Time getMediaTime()
    {
        return controller.getMediaTime();
    }

    public long getMediaNanoseconds()
    {
        return controller.getMediaNanoseconds();
    }

    public long getLatency()
    {
        return ((PlaybackEngine)controller).getLatency();
    }

    public void setProtocol(int protocol)
    {
        this.protocol = protocol;
        Connector connectors[] = inputConnectors.getConnectors();
        for(int i = 0; i < connectors.length; i++)
            connectors[i].setProtocol(protocol);

        connectors = outputConnectors.getConnectors();
        for(int i = 0; i < connectors.length; i++)
            connectors[i].setProtocol(protocol);

    }

    public int getProtocol()
    {
        return protocol;
    }

    public boolean prefetchFailed()
    {
        return prefetchFailed;
    }

    protected Registry inputConnectors;
    protected Registry outputConnectors;
    protected InputConnector inputConnectorsArray[];
    protected OutputConnector outputConnectorsArray[];
    protected int protocol;
    protected String name;
    protected ModuleListener moduleListener;
    protected BasicController controller;
    protected boolean resetted;
    protected boolean prefetchFailed;
    protected JMD jmd;
}
