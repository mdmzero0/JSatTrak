// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicTrackControl.java

package com.sun.media;

import com.sun.media.controls.ProgressControl;
import com.sun.media.controls.StringControl;
import com.sun.media.util.JMFI18N;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;
import javax.media.*;
import javax.media.control.FrameRateControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media:
//            BasicModule, BasicRendererModule, BasicController, Log, 
//            Connector, Module, OutputConnector, BasicPlugIn, 
//            BasicMuxModule, PlaybackEngine

public class BasicTrackControl
    implements TrackControl
{

    public BasicTrackControl(PlaybackEngine engine, Track track, OutputConnector oc)
    {
        modules = new Vector(7);
        muxModule = null;
        prefetchFailed = false;
        rendererFailed = false;
        lastFrameRate = 0.0F;
        lastStatsTime = 0L;
        this.engine = engine;
        this.track = track;
        firstOC = oc;
        lastOC = oc;
        setEnabled(track.isEnabled());
    }

    public Format getOriginalFormat()
    {
        return track.getFormat();
    }

    public Format getFormat()
    {
        return track.getFormat();
    }

    public Format[] getSupportedFormats()
    {
        return (new Format[] {
            track.getFormat()
        });
    }

    public boolean buildTrack(int trackID, int numTracks)
    {
        return false;
    }

    public Format setFormat(Format format)
    {
        if(format != null && format.matches(getFormat()))
            return getFormat();
        else
            return null;
    }

    public void setCodecChain(Codec codec[])
        throws NotConfiguredError, UnsupportedPlugInException
    {
        BasicTrackControl _tmp = this;
        if(engine.getState() > 180)
            throw new NotConfiguredError("Cannot set a PlugIn before reaching the configured state.");
        if(codec.length < 1)
            throw new UnsupportedPlugInException("No codec specified in the array.");
        else
            return;
    }

    public void setRenderer(Renderer renderer)
        throws NotConfiguredError
    {
        BasicTrackControl _tmp = this;
        if(engine.getState() > 180)
            throw new NotConfiguredError("Cannot set a PlugIn before reaching the configured state.");
        else
            return;
    }

    public boolean prefetchTrack()
    {
        for(int j = 0; j < modules.size(); j++)
        {
            BasicModule bm = (BasicModule)modules.elementAt(j);
            if(!bm.doPrefetch())
            {
                setEnabled(false);
                prefetchFailed = true;
                if(bm instanceof BasicRendererModule)
                    rendererFailed = true;
                return false;
            }
        }

        if(prefetchFailed)
        {
            setEnabled(true);
            prefetchFailed = false;
            rendererFailed = false;
        }
        return true;
    }

    public void startTrack()
    {
        for(int j = 0; j < modules.size(); j++)
            ((BasicModule)modules.elementAt(j)).doStart();

    }

    public void stopTrack()
    {
        for(int j = 0; j < modules.size(); j++)
            ((BasicModule)modules.elementAt(j)).doStop();

    }

    public boolean isCustomized()
    {
        return false;
    }

    public boolean isTimeBase()
    {
        return false;
    }

    public boolean isEnabled()
    {
        return track.isEnabled();
    }

    public void setEnabled(boolean enabled)
    {
        track.setEnabled(enabled);
    }

    protected ProgressControl progressControl()
    {
        return null;
    }

    protected FrameRateControl frameRateControl()
    {
        return null;
    }

    public void prError()
    {
        Log.error("  Unable to handle format: " + getOriginalFormat());
        Log.write("\n");
    }

    public Object[] getControls()
        throws NotRealizedError
    {
        if(engine.getState() < 300)
            throw new NotRealizedError("Cannot get CodecControl before reaching the realized state.");
        OutputConnector oc = firstOC;
        javax.media.PlugIn p = null;
        Vector cv = new Vector();
        InputConnector inputconnector;
        Module m;
        for(; oc != null && (inputconnector = oc.getInputConnector()) != null; oc = m.getOutputConnector(null))
        {
            m = inputconnector.getModule();
            Object cs[] = m.getControls();
            if(cs != null)
            {
                for(int i = 0; i < cs.length; i++)
                    cv.addElement(cs[i]);

            }
        }

        int size = cv.size();
        Control controls[] = new Control[size];
        for(int i = 0; i < size; i++)
            controls[i] = (Control)cv.elementAt(i);

        return controls;
    }

    public Object getControl(String type)
    {
        Class cls;
        try
        {
            cls = BasicPlugIn.getClassForName(type);
        }
        catch(ClassNotFoundException e)
        {
            return null;
        }
        Object cs[] = getControls();
        for(int i = 0; i < cs.length; i++)
            if(cls.isInstance(cs[i]))
                return cs[i];

        return null;
    }

    public Component getControlComponent()
    {
        return null;
    }

    public void updateFormat()
    {
        if(!track.isEnabled())
            return;
        ProgressControl pc;
        if((pc = progressControl()) == null)
            return;
        if(track.getFormat() instanceof AudioFormat)
        {
            String channel = "";
            AudioFormat afmt = (AudioFormat)track.getFormat();
            StringControl sc = pc.getAudioCodec();
            sc.setValue(afmt.getEncoding());
            sc = pc.getAudioProperties();
            if(afmt.getChannels() == 1)
                channel = JMFI18N.getResource("mediaengine.mono");
            else
                channel = JMFI18N.getResource("mediaengine.stereo");
            sc.setValue(afmt.getSampleRate() / 1000D + JMFI18N.getResource("mediaengine.khz") + ", " + afmt.getSampleSizeInBits() + JMFI18N.getResource("mediaengine.-bit") + ", " + channel);
        }
        if(track.getFormat() instanceof VideoFormat)
        {
            VideoFormat vfmt = (VideoFormat)track.getFormat();
            StringControl sc = pc.getVideoCodec();
            sc.setValue(vfmt.getEncoding());
            sc = pc.getVideoProperties();
            if(vfmt.getSize() != null)
                sc.setValue(vfmt.getSize().width + " X " + vfmt.getSize().height);
        }
    }

    public void updateRates(long now)
    {
        FrameRateControl prc;
        if((prc = frameRateControl()) == null)
            return;
        if(!track.isEnabled() || !(track.getFormat() instanceof VideoFormat) || rendererModule == null && muxModule == null)
            return;
        float rate;
        if(now == lastStatsTime)
        {
            rate = lastFrameRate;
        } else
        {
            int framesPlayed;
            if(rendererModule != null)
                framesPlayed = rendererModule.getFramesPlayed();
            else
                framesPlayed = muxModule.getFramesPlayed();
            rate = ((float)framesPlayed / (float)(now - lastStatsTime)) * 1000F;
        }
        float avg = (float)(int)(((lastFrameRate + rate) / 2.0F) * 10F) / 10F;
        prc.setFrameRate(avg);
        lastFrameRate = rate;
        lastStatsTime = now;
        if(rendererModule != null)
            rendererModule.resetFramesPlayed();
        else
            muxModule.resetFramesPlayed();
    }

    static final String realizeErr = "Cannot get CodecControl before reaching the realized state.";
    static final String connectErr = "Cannot set a PlugIn before reaching the configured state.";
    PlaybackEngine engine;
    Track track;
    OutputConnector firstOC;
    OutputConnector lastOC;
    protected Vector modules;
    protected BasicRendererModule rendererModule;
    protected BasicMuxModule muxModule;
    protected boolean prefetchFailed;
    protected boolean rendererFailed;
    float lastFrameRate;
    long lastStatsTime;
}
