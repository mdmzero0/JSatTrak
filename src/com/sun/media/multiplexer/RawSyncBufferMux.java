// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RawSyncBufferMux.java

package com.sun.media.multiplexer;

import com.sun.media.BasicClock;
import com.sun.media.controls.MonitorAdapter;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.multiplexer:
//            RawBufferMux

public class RawSyncBufferMux extends RawBufferMux
{

    public RawSyncBufferMux()
    {
        mpegBFrame = false;
        mpegPFrame = false;
        monoIncrTime = false;
        monoStartTime = 0L;
        monoTime = 0L;
        waitLock = new Object();
        resetted = false;
        masterTrackEnded = false;
        super.timeBase = new RawBufferMux.RawMuxTimeBase(this);
        super.allowDrop = true;
        super.clock = new BasicClock();
        try
        {
            super.clock.setTimeBase(super.timeBase);
        }
        catch(Exception e) { }
    }

    public boolean initializeTracks(Format trackFormats[])
    {
        if(!super.initializeTracks(trackFormats))
            return false;
        super.masterTrackID = 0;
        for(int i = 0; i < trackFormats.length; i++)
            if(trackFormats[i] instanceof AudioFormat)
                super.masterTrackID = i;

        return true;
    }

    public void reset()
    {
        super.reset();
        mpegBFrame = false;
        mpegPFrame = false;
        synchronized(waitLock)
        {
            resetted = true;
            waitLock.notify();
        }
    }

    public String getName()
    {
        return "Raw Sync Buffer Multiplexer";
    }

    public int process(Buffer buffer, int trackID)
    {
        if((buffer.getFlags() & 0x1000) != 0)
            buffer.setFlags(buffer.getFlags() & 0xffffefff | 0x100);
        if(super.mc[trackID] != null && super.mc[trackID].isEnabled())
            super.mc[trackID].process(buffer);
        if(super.streams == null || buffer == null || trackID >= super.streams.length)
            return 1;
        if(buffer.isDiscard())
            return 0;
        if((buffer.getFlags() & 0x40) == 0)
            if(buffer.getFormat() instanceof AudioFormat)
            {
                if(mpegAudio.matches(buffer.getFormat()))
                    waitForPT(buffer.getTimeStamp(), trackID);
                else
                    waitForPT(super.mediaTime[trackID], trackID);
            } else
            if(buffer.getTimeStamp() >= 0L)
                if(mpegVideo.matches(buffer.getFormat()) && (buffer.getFlags() & 0x800) != 0)
                {
                    byte payload[] = (byte[])buffer.getData();
                    int offset = buffer.getOffset();
                    int ptype = payload[offset + 2] & 7;
                    if(ptype > 2)
                        mpegBFrame = true;
                    else
                    if(ptype == 2)
                        mpegPFrame = true;
                    if(ptype > 2 || ptype == 2 && !mpegBFrame || ptype == 1 && !(mpegBFrame | mpegPFrame))
                        waitForPT(buffer.getTimeStamp(), trackID);
                } else
                {
                    waitForPT(buffer.getTimeStamp(), trackID);
                }
        updateTime(buffer, trackID);
        buffer.setFlags(buffer.getFlags() | 0x60);
        if((!(buffer.getFormat() instanceof AudioFormat) || mpegAudio.matches(buffer.getFormat())) && monoIncrTime)
        {
            monoTime = (monoStartTime + buffer.getTimeStamp()) - super.mediaStartTime * 0xf4240L;
            buffer.setTimeStamp(monoTime);
        }
        if(buffer.isEOM() && trackID == super.masterTrackID)
            masterTrackEnded = true;
        buffer.setHeader(new Long(System.currentTimeMillis()));
        return super.streams[trackID].process(buffer);
    }

    public void syncStart(Time at)
    {
        masterTrackEnded = false;
        super.syncStart(at);
    }

    public void setMediaTime(Time now)
    {
        super.setMediaTime(now);
        monoStartTime = monoTime + 10L;
    }

    protected void updateTime(Buffer buf, int trackID)
    {
        if(buf.getFormat() instanceof AudioFormat)
        {
            if(mpegAudio.matches(buf.getFormat()))
            {
                if(buf.getTimeStamp() < 0L)
                {
                    if(super.systemStartTime >= 0L)
                        super.mediaTime[trackID] = ((super.mediaStartTime + System.currentTimeMillis()) - super.systemStartTime) * 0xf4240L;
                } else
                {
                    super.mediaTime[trackID] = buf.getTimeStamp();
                }
            } else
            {
                long t = ((AudioFormat)buf.getFormat()).computeDuration(buf.getLength());
                if(t >= 0L)
                    super.mediaTime[trackID] += t;
                else
                    super.mediaTime[trackID] = buf.getTimeStamp();
            }
        } else
        if(buf.getTimeStamp() < 0L && super.systemStartTime >= 0L)
            super.mediaTime[trackID] = ((super.mediaStartTime + System.currentTimeMillis()) - super.systemStartTime) * 0xf4240L;
        else
            super.mediaTime[trackID] = buf.getTimeStamp();
        super.timeBase.update();
    }

    private void waitForPT(long pt, int trackID)
    {
        pt /= 0xf4240L;
        long delay;
        if(super.masterTrackID == -1 || trackID == super.masterTrackID)
        {
            if(super.systemStartTime < 0L)
                delay = 0L;
            else
                delay = pt - super.mediaStartTime - (System.currentTimeMillis() - super.systemStartTime);
        } else
        {
            delay = pt - super.mediaTime[super.masterTrackID] / 0xf4240L;
        }
        if(delay > 2000L)
            return;
        while(delay > (long)LEEWAY && !masterTrackEnded) 
        {
            if(delay > (long)THRESHOLD)
                delay = THRESHOLD;
            synchronized(waitLock)
            {
                try
                {
                    waitLock.wait(delay);
                }
                catch(Exception e)
                {
                    break;
                }
                if(resetted)
                {
                    resetted = false;
                    break;
                }
            }
            if(super.masterTrackID == -1 || trackID == super.masterTrackID)
                delay = pt - super.mediaStartTime - (System.currentTimeMillis() - super.systemStartTime);
            else
                delay = pt - super.mediaTime[super.masterTrackID] / 0xf4240L;
        }
    }

    boolean mpegBFrame;
    boolean mpegPFrame;
    protected boolean monoIncrTime;
    private long monoStartTime;
    private long monoTime;
    private Object waitLock;
    private boolean resetted;
    private boolean masterTrackEnded;
    static AudioFormat mpegAudio = new AudioFormat("mpegaudio/rtp");
    static VideoFormat mpegVideo = new VideoFormat("mpeg/rtp");
    static int THRESHOLD = 80;
    static int LEEWAY = 5;

}
