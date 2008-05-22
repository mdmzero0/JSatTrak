// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaPlayer.java

package com.sun.media;

import com.sun.media.controls.ProgressControl;
import java.awt.Component;
import java.io.IOException;
import java.util.Vector;
import javax.media.*;
import javax.media.protocol.DataSource;

// Referenced classes of package com.sun.media:
//            BasicPlayer, PlaybackEngine, BasicController

public class MediaPlayer extends BasicPlayer
{

    public MediaPlayer()
    {
        engine = new PlaybackEngine(this);
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
        int state = getState();
        if(state < 300)
            throwError(new NotRealizedError("Cannot get gain control on an unrealized player"));
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

    public void updateStats()
    {
        engine.updateRates();
    }

    public void setProgressControl(ProgressControl p)
    {
        engine.setProgressControl(p);
    }

    protected PlaybackEngine engine;
}
