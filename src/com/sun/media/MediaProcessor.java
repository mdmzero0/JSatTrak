// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaProcessor.java

package com.sun.media;

import java.awt.Component;
import java.io.IOException;
import java.util.Vector;
import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

// Referenced classes of package com.sun.media:
//            BasicProcessor, ProcessEngine, PlaybackEngine, BasicPlayer, 
//            BasicController

public class MediaProcessor extends BasicProcessor
{

    public MediaProcessor()
    {
        engine = new ProcessEngine(this);
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        engine.setSource(source);
        manageController(engine);
        super.setSource(source);
    }

    public Component getVisualComponent()
    {
        super.getVisualComponent();
        return engine.getVisualComponent();
    }

    public GainControl getGainControl()
    {
        super.getGainControl();
        return engine.getGainControl();
    }

    public Time getMediaTime()
    {
        if(super.controllerList.size() > 1)
            return super.getMediaTime();
        else
            return engine.getMediaTime();
    }

    public long getMediaNanoseconds()
    {
        if(super.controllerList.size() > 1)
            return super.getMediaNanoseconds();
        else
            return engine.getMediaNanoseconds();
    }

    protected TimeBase getMasterTimeBase()
    {
        return engine.getTimeBase();
    }

    protected boolean audioEnabled()
    {
        return engine.audioEnabled();
    }

    protected boolean videoEnabled()
    {
        return engine.videoEnabled();
    }

    public TrackControl[] getTrackControls()
        throws NotConfiguredError
    {
        return engine.getTrackControls();
    }

    public ContentDescriptor[] getSupportedContentDescriptors()
        throws NotConfiguredError
    {
        return engine.getSupportedContentDescriptors();
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor ocd)
        throws NotConfiguredError
    {
        return engine.setContentDescriptor(ocd);
    }

    public ContentDescriptor getContentDescriptor()
        throws NotConfiguredError
    {
        return engine.getContentDescriptor();
    }

    public DataSource getDataOutput()
        throws NotRealizedError
    {
        return engine.getDataOutput();
    }

    public void updateStats()
    {
        engine.updateRates();
    }

    protected ProcessEngine engine;
}
