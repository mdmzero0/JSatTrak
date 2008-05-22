// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicPullBufferDataSource.java

package com.sun.media.protocol;

import java.io.IOException;
import java.io.PrintStream;
import javax.media.Duration;
import javax.media.Time;
import javax.media.protocol.PullBufferDataSource;

public abstract class BasicPullBufferDataSource extends PullBufferDataSource
{

    public BasicPullBufferDataSource()
    {
        controls = new Object[0];
        started = false;
        contentType = "content/unknown";
        connected = false;
        duration = Duration.DURATION_UNKNOWN;
    }

    public String getContentType()
    {
        if(!connected)
        {
            System.err.println("Error: DataSource not connected");
            return null;
        } else
        {
            return contentType;
        }
    }

    public void connect()
        throws IOException
    {
        if(connected)
        {
            return;
        } else
        {
            connected = true;
            return;
        }
    }

    public void disconnect()
    {
        try
        {
            if(started)
                stop();
        }
        catch(IOException e) { }
        connected = false;
    }

    public void start()
        throws IOException
    {
        if(!connected)
            throw new Error("DataSource must be connected before it can be started");
        if(started)
        {
            return;
        } else
        {
            started = true;
            return;
        }
    }

    public void stop()
        throws IOException
    {
        if(!connected || !started)
        {
            return;
        } else
        {
            started = false;
            return;
        }
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

    public Time getDuration()
    {
        return duration;
    }

    protected Object controls[];
    protected boolean started;
    protected String contentType;
    protected boolean connected;
    protected Time duration;
}
