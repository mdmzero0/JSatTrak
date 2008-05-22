// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MergingDataSource.java

package com.ibm.media.protocol;

import java.io.IOException;
import java.util.Vector;
import javax.media.Time;
import javax.media.protocol.DataSource;
import javax.media.protocol.SourceStream;

class MergingDataSource extends DataSource
{

    MergingDataSource(DataSource sources[])
    {
        streams = null;
        controls = null;
        this.sources = sources;
    }

    public String getContentType()
    {
        if(sources.length == 1)
            return sources[0].getContentType();
        boolean isRaw = true;
        for(int index = 0; index < sources.length; index++)
        {
            if(sources[index].getContentType().equals("raw"))
                continue;
            isRaw = false;
            break;
        }

        if(isRaw)
            return "raw";
        if(sources.length == 1)
            return sources[0].getContentType();
        else
            return "application.mixed-data";
    }

    public void connect()
        throws IOException
    {
        for(int i = 0; i < sources.length; i++)
            sources[i].connect();

    }

    public void disconnect()
    {
        for(int i = 0; i < sources.length; i++)
            sources[i].disconnect();

    }

    public void start()
        throws IOException
    {
        for(int i = 0; i < sources.length; i++)
            sources[i].start();

    }

    public void stop()
        throws IOException
    {
        for(int i = 0; i < sources.length; i++)
            sources[i].stop();

    }

    public Object[] getControls()
    {
        if(controls == null)
        {
            Vector vcontrols = new Vector(1);
            for(int i = 0; i < sources.length; i++)
            {
                Object cs[] = (Object[])sources[i].getControls();
                if(cs.length > 0)
                {
                    for(int j = 0; j < cs.length; j++)
                        vcontrols.addElement(cs[j]);

                }
            }

            controls = new Object[vcontrols.size()];
            for(int c = 0; c < vcontrols.size(); c++)
                controls[c] = vcontrols.elementAt(c);

        }
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
        Time longest = new Time(0L);
        for(int i = 0; i < sources.length; i++)
        {
            Time sourceDuration = sources[i].getDuration();
            if(sourceDuration.getSeconds() > longest.getSeconds())
                longest = sourceDuration;
        }

        return longest;
    }

    DataSource sources[];
    SourceStream streams[];
    Object controls[];
}
