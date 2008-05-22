// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicProcessor.java

package com.sun.media;

import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

// Referenced classes of package com.sun.media:
//            BasicPlayer, BasicController

public abstract class BasicProcessor extends BasicPlayer
    implements Processor
{

    public BasicProcessor()
    {
    }

    protected boolean isConfigurable()
    {
        return true;
    }

    public TrackControl[] getTrackControls()
        throws NotConfiguredError
    {
        if(getState() < 180)
            throw new NotConfiguredError("getTrackControls " + NOT_CONFIGURED_ERROR);
        else
            return new TrackControl[0];
    }

    public ContentDescriptor[] getSupportedContentDescriptors()
        throws NotConfiguredError
    {
        if(getState() < 180)
            throw new NotConfiguredError("getSupportedContentDescriptors " + NOT_CONFIGURED_ERROR);
        else
            return new ContentDescriptor[0];
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor ocd)
        throws NotConfiguredError
    {
        if(getState() < 180)
            throw new NotConfiguredError("setContentDescriptor " + NOT_CONFIGURED_ERROR);
        else
            return ocd;
    }

    public ContentDescriptor getContentDescriptor()
        throws NotConfiguredError
    {
        if(getState() < 180)
            throw new NotConfiguredError("getContentDescriptor " + NOT_CONFIGURED_ERROR);
        else
            return null;
    }

    public DataSource getDataOutput()
        throws NotRealizedError
    {
        if(getState() < 300)
            throw new NotRealizedError("getDataOutput cannot be called before the Processor is realized");
        else
            return null;
    }

    static String NOT_CONFIGURED_ERROR = "cannot be called before the Processor is configured";

}
