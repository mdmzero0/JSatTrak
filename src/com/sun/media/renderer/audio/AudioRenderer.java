// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AudioRenderer.java

package com.sun.media.renderer.audio;

import com.sun.media.*;
import com.sun.media.renderer.audio.device.AudioOutput;
import java.awt.Component;
import java.io.PrintStream;
import javax.media.*;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;

public abstract class AudioRenderer extends BasicPlugIn
    implements Renderer, Prefetchable, Drainable, Clock
{
    class BC
        implements BufferControl, Owned
    {

        public long getBufferLength()
        {
            return bufLenReq;
        }

        public long setBufferLength(long time)
        {
            if(time < (long)AudioRenderer.DefaultMinBufferSize)
                bufLenReq = AudioRenderer.DefaultMinBufferSize;
            else
            if(time > (long)AudioRenderer.DefaultMaxBufferSize)
                bufLenReq = AudioRenderer.DefaultMaxBufferSize;
            else
                bufLenReq = time;
            return bufLenReq;
        }

        public long getMinimumThreshold()
        {
            return 0L;
        }

        public long setMinimumThreshold(long time)
        {
            return 0L;
        }

        public void setEnabledThreshold(boolean flag)
        {
        }

        public boolean getEnabledThreshold()
        {
            return false;
        }

        public Component getControlComponent()
        {
            return null;
        }

        public Object getOwner()
        {
            return renderer;
        }

        AudioRenderer renderer;

        BC(AudioRenderer ar)
        {
            renderer = ar;
        }
    }

    class AudioTimeBase extends MediaTimeBase
    {

        public long getMediaTime()
        {
            if(rate == 1.0F || rate == 0.0F)
                return device == null ? 0L : device.getMediaNanoseconds();
            else
                return (long)((device == null ? 0.0F : device.getMediaNanoseconds()) / rate);
        }

        AudioRenderer renderer;

        AudioTimeBase(AudioRenderer r)
        {
            renderer = r;
        }
    }


    public AudioRenderer()
    {
        device = null;
        timeBase = null;
        started = false;
        prefetched = false;
        resetted = false;
        devicePaused = true;
        peakVolumeMeter = null;
        bytesWritten = 0L;
        writeLock = new Object();
        mediaTimeAnchor = 0L;
        startTime = 0x7fffffffffffffffL;
        stopTime = 0x7fffffffffffffffL;
        ticksSinceLastReset = 0L;
        rate = 1.0F;
        master = null;
        bufLenReq = 200L;
        timeBase = new AudioTimeBase(this);
        bufferControl = new BC(this);
    }

    public Format[] getSupportedInputFormats()
    {
        return supportedFormats;
    }

    public Format setInputFormat(Format format)
    {
        for(int i = 0; i < supportedFormats.length; i++)
            if(supportedFormats[i].matches(format))
            {
                inputFormat = (AudioFormat)format;
                return format;
            }

        return null;
    }

    public void close()
    {
        stop();
        if(device != null)
        {
            pauseDevice();
            device.flush();
            mediaTimeAnchor = getMediaNanoseconds();
            ticksSinceLastReset = 0L;
            device.dispose();
        }
        device = null;
    }

    public void reset()
    {
        resetted = true;
        mediaTimeAnchor = getMediaNanoseconds();
        if(device != null)
        {
            device.flush();
            ticksSinceLastReset = device.getMediaNanoseconds();
        } else
        {
            ticksSinceLastReset = 0L;
        }
        prefetched = false;
    }

    synchronized void pauseDevice()
    {
        if(!devicePaused && device != null)
        {
            device.pause();
            devicePaused = true;
        }
        if(timeBase instanceof AudioTimeBase)
            ((AudioTimeBase)timeBase).mediaStopped();
    }

    synchronized void resumeDevice()
    {
        if(timeBase instanceof AudioTimeBase)
            ((AudioTimeBase)timeBase).mediaStarted();
        if(devicePaused && device != null)
        {
            device.resume();
            devicePaused = false;
        }
    }

    public void start()
    {
        syncStart(getTimeBase().getTime());
    }

    public synchronized void drain()
    {
        if(started && device != null)
            device.drain();
        prefetched = false;
    }

    public int process(Buffer buffer)
    {
        int rtn = processData(buffer);
        if(buffer.isEOM() && rtn != 2)
        {
            drain();
            pauseDevice();
        }
        return rtn;
    }

    protected boolean checkInput(Buffer buffer)
    {
        Format format = buffer.getFormat();
        if(device == null || devFormat == null || !devFormat.equals(format))
        {
            if(!initDevice((AudioFormat)format))
            {
                buffer.setDiscard(true);
                return false;
            }
            devFormat = (AudioFormat)format;
        }
        return true;
    }

    protected int processData(Buffer buffer)
    {
        if(!checkInput(buffer))
            return 1;
        else
            return doProcessData(buffer);
    }

    protected int doProcessData(Buffer buffer)
    {
        byte data[] = (byte[])buffer.getData();
        int remain = buffer.getLength();
        int off = buffer.getOffset();
        int len = 0;
        synchronized(this)
        {
            if(!started)
            {
                if(!devicePaused)
                    pauseDevice();
                resetted = false;
                int available = device.bufferAvailable();
                if(available > remain)
                    available = remain;
                if(available > 0)
                {
                    len = device.write(data, off, available);
                    bytesWritten += len;
                }
                buffer.setLength(remain - len);
                if(buffer.getLength() > 0 || buffer.isEOM())
                {
                    buffer.setOffset(off + len);
                    prefetched = true;
                    byte byte0 = 2;
                    return byte0;
                }
                int i = 0;
                return i;
            }
        }
        synchronized(writeLock)
        {
            if(devicePaused)
            {
                byte byte1 = 2;
                return byte1;
            }
            try
            {
                for(; remain > 0 && !resetted; remain -= len)
                {
                    len = device.write(data, off, remain);
                    bytesWritten += len;
                    off += len;
                }

            }
            catch(NullPointerException e)
            {
                int j = 0;
                return j;
            }
        }
        buffer.setLength(0);
        buffer.setOffset(0);
        return 0;
    }

    protected boolean initDevice(AudioFormat format)
    {
        if(format == null)
        {
            System.err.println("AudioRenderer: ERROR: Unknown AudioFormat");
            return false;
        }
        if(format.getSampleRate() == -1D || format.getSampleSizeInBits() == -1)
        {
            Log.error("Cannot initialize audio renderer with format: " + format);
            return false;
        }
        if(device != null)
        {
            device.drain();
            pauseDevice();
            mediaTimeAnchor = getMediaNanoseconds();
            ticksSinceLastReset = 0L;
            device.dispose();
            device = null;
        }
        AudioFormat audioFormat = new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(), format.getEndian(), format.getSigned());
        device = createDevice(audioFormat);
        if(device == null || !device.initialize(audioFormat, computeBufferSize(audioFormat)))
        {
            device = null;
            return false;
        }
        device.setMute(gainControl.getMute());
        device.setGain(gainControl.getDB());
        if(rate != 1.0F && rate != device.setRate(rate))
        {
            System.err.println("The AudioRenderer does not support the given rate: " + rate);
            device.setRate(1.0F);
        }
        if(started)
            resumeDevice();
        bytesPerSec = (int)((format.getSampleRate() * (double)format.getChannels() * (double)format.getSampleSizeInBits()) / 8D);
        return true;
    }

    protected abstract AudioOutput createDevice(AudioFormat audioformat);

    protected void processByWaiting(Buffer buffer)
    {
        synchronized(this)
        {
            if(!started)
            {
                prefetched = true;
                return;
            }
        }
        AudioFormat format = (AudioFormat)buffer.getFormat();
        int sampleRate = (int)format.getSampleRate();
        int sampleSize = format.getSampleSizeInBits();
        int channels = format.getChannels();
        long duration = (buffer.getLength() * 1000) / ((sampleSize / 8) * sampleRate * channels);
        int timeToWait = (int)((float)duration / getRate());
        try
        {
            Thread.currentThread();
            Thread.sleep(timeToWait);
        }
        catch(Exception e) { }
        buffer.setLength(0);
        buffer.setOffset(0);
        mediaTimeAnchor += duration * 0xf4240L;
    }

    public Object[] getControls()
    {
        Control c[] = {
            gainControl, bufferControl
        };
        return c;
    }

    public boolean isPrefetched()
    {
        return prefetched;
    }

    public void setTimeBase(TimeBase master)
        throws IncompatibleTimeBaseException
    {
        if(!(master instanceof AudioTimeBase))
            Log.warning("AudioRenderer cannot be controlled by time bases other than its own: " + master);
        this.master = master;
    }

    public synchronized void syncStart(Time at)
    {
        started = true;
        prefetched = true;
        resetted = false;
        resumeDevice();
        startTime = at.getNanoseconds();
    }

    public synchronized void stop()
    {
        started = false;
        prefetched = false;
        synchronized(writeLock)
        {
            pauseDevice();
        }
    }

    public void setStopTime(Time t)
    {
        stopTime = t.getNanoseconds();
    }

    public Time getStopTime()
    {
        return new Time(stopTime);
    }

    public void setMediaTime(Time now)
    {
        mediaTimeAnchor = now.getNanoseconds();
    }

    public Time getMediaTime()
    {
        return new Time(getMediaNanoseconds());
    }

    public long getMediaNanoseconds()
    {
        return (mediaTimeAnchor + (device == null ? 0L : device.getMediaNanoseconds())) - ticksSinceLastReset;
    }

    public long getLatency()
    {
        long ts = ((bytesWritten * 1000L) / (long)bytesPerSec) * 0xf4240L;
        return ts - getMediaNanoseconds();
    }

    public Time getSyncTime()
    {
        return new Time(0L);
    }

    public TimeBase getTimeBase()
    {
        if(master != null)
            return master;
        else
            return timeBase;
    }

    public Time mapToTimeBase(Time t)
        throws ClockStoppedException
    {
        return new Time((long)((float)(t.getNanoseconds() - mediaTimeAnchor) / rate) + startTime);
    }

    public float getRate()
    {
        return rate;
    }

    public float setRate(float factor)
    {
        if(device != null)
            rate = device.setRate(factor);
        else
            rate = 1.0F;
        return rate;
    }

    public int computeBufferSize(AudioFormat f)
    {
        long bytesPerSecond = (long)((f.getSampleRate() * (double)f.getChannels() * (double)f.getSampleSizeInBits()) / 8D);
        long bufLen;
        if(bufLenReq < (long)DefaultMinBufferSize)
            bufLen = DefaultMinBufferSize;
        else
        if(bufLenReq > (long)DefaultMaxBufferSize)
            bufLen = DefaultMaxBufferSize;
        else
            bufLen = bufLenReq;
        float r = (float)bufLen / 1000F;
        long bufSize = (long)((float)bytesPerSecond * r);
        return (int)bufSize;
    }

    Format supportedFormats[];
    protected AudioFormat inputFormat;
    protected AudioFormat devFormat;
    protected AudioOutput device;
    protected TimeBase timeBase;
    protected boolean started;
    protected boolean prefetched;
    protected boolean resetted;
    protected boolean devicePaused;
    protected GainControl gainControl;
    protected BufferControl bufferControl;
    protected Control peakVolumeMeter;
    protected long bytesWritten;
    protected int bytesPerSec;
    private Object writeLock;
    long mediaTimeAnchor;
    long startTime;
    long stopTime;
    long ticksSinceLastReset;
    float rate;
    TimeBase master;
    static int DefaultMinBufferSize = 62;
    static int DefaultMaxBufferSize = 4000;
    long bufLenReq;

}
