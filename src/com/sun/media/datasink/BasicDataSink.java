// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicDataSink.java

package com.sun.media.datasink;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.media.*;
import javax.media.datasink.*;
import javax.media.protocol.DataSource;

public abstract class BasicDataSink
    implements DataSink
{

    public BasicDataSink()
    {
        listeners = new Vector(1);
    }

    public void addDataSinkListener(DataSinkListener dsl)
    {
        if(dsl != null && !listeners.contains(dsl))
            listeners.addElement(dsl);
    }

    public void removeDataSinkListener(DataSinkListener dsl)
    {
        if(dsl != null)
            listeners.removeElement(dsl);
    }

    protected void sendEvent(DataSinkEvent event)
    {
        if(!listeners.isEmpty())
            synchronized(listeners)
            {
                DataSinkListener listener;
                for(Enumeration list = listeners.elements(); list.hasMoreElements(); listener.dataSinkUpdate(event))
                    listener = (DataSinkListener)list.nextElement();

            }
    }

    protected void removeAllListeners()
    {
        listeners.removeAllElements();
    }

    protected final void sendEndofStreamEvent()
    {
        sendEvent(new EndOfStreamEvent(this));
    }

    protected final void sendDataSinkErrorEvent(String reason)
    {
        sendEvent(new DataSinkErrorEvent(this, reason));
    }

    public abstract String getContentType();

    public abstract void close();

    public abstract void open()
        throws IOException, SecurityException;

    public abstract void stop()
        throws IOException;

    public abstract void start()
        throws IOException;

    public abstract MediaLocator getOutputLocator();

    public abstract void setOutputLocator(MediaLocator medialocator);

    public abstract void setSource(DataSource datasource)
        throws IOException, IncompatibleSourceException;

    public abstract Object getControl(String s);

    public abstract Object[] getControls();

    protected Vector listeners;
}
