// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RawParser.java

package com.sun.media.parser;

import com.sun.media.BasicPlugIn;
import java.io.IOException;
import javax.media.*;
import javax.media.protocol.*;

public abstract class RawParser extends BasicPlugIn
    implements Demultiplexer
{

    public String getName()
    {
        return "Raw parser";
    }

    public RawParser()
    {
        supported = new ContentDescriptor[1];
        supported[0] = new ContentDescriptor("raw");
    }

    public ContentDescriptor[] getSupportedInputContentDescriptors()
    {
        return supported;
    }

    public boolean isPositionable()
    {
        return source instanceof Positionable;
    }

    public boolean isRandomAccess()
    {
        return (source instanceof Positionable) && ((Positionable)source).isRandomAccess();
    }

    public Track[] getTracks()
    {
        return null;
    }

    public Time getMediaTime()
    {
        return Time.TIME_UNKNOWN;
    }

    public Time getDuration()
    {
        return source != null ? source.getDuration() : Duration.DURATION_UNKNOWN;
    }

    public void reset()
    {
    }

    public Time setPosition(Time when, int round)
    {
        if(source instanceof Positionable)
            return ((Positionable)source).setPosition(when, round);
        else
            return when;
    }

    public Object[] getControls()
    {
        return source.getControls();
    }

    public abstract void stop();

    public abstract void start()
        throws IOException;

    public abstract void setSource(DataSource datasource)
        throws IOException, IncompatibleSourceException;

    static final String NAME = "Raw parser";
    protected DataSource source;
    ContentDescriptor supported[];
}
