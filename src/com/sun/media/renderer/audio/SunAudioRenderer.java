// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SunAudioRenderer.java

package com.sun.media.renderer.audio;

import com.sun.media.BasicClock;
import com.sun.media.Log;
import com.sun.media.controls.GainControlAdapter;
import com.sun.media.renderer.audio.device.AudioOutput;
import com.sun.media.renderer.audio.device.SunAudioOutput;
import java.io.PrintStream;
import javax.media.*;
import javax.media.format.AudioFormat;
import sun.audio.AudioPlayer;

// Referenced classes of package com.sun.media.renderer.audio:
//            AudioRenderer

public class SunAudioRenderer extends AudioRenderer
{
    class MCA extends GainControlAdapter
    {

        public void setMute(boolean mute)
        {
            if(renderer != null && renderer.device != null)
                renderer.device.setMute(mute);
            super.setMute(mute);
        }

        public float getLevel()
        {
            return -1F;
        }

        AudioRenderer renderer;

        protected MCA(AudioRenderer r)
        {
            super(false);
            renderer = r;
        }
    }


    public SunAudioRenderer()
    {
        clock = null;
        startMediaTime = 0L;
        if(useSystemTime)
        {
            super.timeBase = new SystemTimeBase();
            clock = new BasicClock();
        }
        super.supportedFormats = new Format[1];
        super.supportedFormats[0] = new AudioFormat("ULAW", 8000D, 8, 1, -1, -1);
        super.gainControl = new MCA(this);
    }

    public String getName()
    {
        return NAME;
    }

    public void open()
        throws ResourceUnavailableException
    {
        if(!grabDevice())
            throw new ResourceUnavailableException("AudioRenderer: Failed to initialize audio device.");
        else
            return;
    }

    public void close()
    {
        super.close();
    }

    protected AudioOutput createDevice(AudioFormat format)
    {
        return new SunAudioOutput();
    }

    private static synchronized boolean grabDevice()
    {
        if(runningOnMac && !AudioPlayer.player.isAlive())
        {
            System.out.println("Audio device is busy");
            return false;
        } else
        {
            return true;
        }
    }

    public void setTimeBase(TimeBase master)
        throws IncompatibleTimeBaseException
    {
        if(useSystemTime)
        {
            if(!(master instanceof SystemTimeBase))
                Log.warning("AudioRenderer cannot be controlled by time bases other than its own: " + master);
            clock.setTimeBase(master);
        } else
        {
            super.setTimeBase(master);
        }
    }

    public void syncStart(Time at)
    {
        super.syncStart(at);
        if(useSystemTime)
            clock.syncStart(at);
    }

    public void stop()
    {
        super.stop();
        if(useSystemTime)
            clock.stop();
    }

    public void setStopTime(Time t)
    {
        if(useSystemTime)
            clock.setStopTime(t);
        else
            super.setStopTime(t);
    }

    public Time getStopTime()
    {
        if(useSystemTime)
            return clock.getStopTime();
        else
            return super.getStopTime();
    }

    public void setMediaTime(Time now)
    {
        if(useSystemTime)
        {
            clock.setMediaTime(now);
            startMediaTime = now.getNanoseconds();
        } else
        {
            super.setMediaTime(now);
        }
    }

    public Time getMediaTime()
    {
        return useSystemTime ? clock.getMediaTime() : super.getMediaTime();
    }

    public long getMediaNanoseconds()
    {
        if(useSystemTime)
        {
            long t = clock.getMediaNanoseconds();
            if(t - startMediaTime < DEVICE_LATENCY)
                return startMediaTime;
            else
                return t - DEVICE_LATENCY;
        } else
        {
            return super.getMediaNanoseconds();
        }
    }

    public Time getSyncTime()
    {
        return useSystemTime ? clock.getSyncTime() : super.getSyncTime();
    }

    public TimeBase getTimeBase()
    {
        return useSystemTime ? clock.getTimeBase() : super.getTimeBase();
    }

    public Time mapToTimeBase(Time t)
        throws ClockStoppedException
    {
        return useSystemTime ? clock.mapToTimeBase(t) : super.mapToTimeBase(t);
    }

    public float getRate()
    {
        return useSystemTime ? clock.getRate() : super.getRate();
    }

    public float setRate(float factor)
    {
        return super.setRate(1.0F);
    }

    static String NAME = "SunAudio Renderer";
    public static String vendor;
    public static String version;
    public static boolean runningOnMac;
    public static boolean useSystemTime = false;
    private BasicClock clock;
    private long startMediaTime;
    public static long DEVICE_LATENCY;

    static 
    {
        vendor = null;
        version = null;
        runningOnMac = false;
        try
        {
            vendor = System.getProperty("java.vendor");
            version = System.getProperty("java.version");
            if(vendor != null)
            {
                vendor = vendor.toUpperCase();
                if(vendor.startsWith("APPLE") && version.startsWith("1.1"))
                {
                    runningOnMac = true;
                    useSystemTime = true;
                }
            }
        }
        catch(Throwable e) { }
        DEVICE_LATENCY = runningOnMac ? 0x1a13b8600L : 0L;
    }
}
