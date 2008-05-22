// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicMuxModule.java

package com.sun.media;

import com.sun.media.util.ElapseTime;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;

// Referenced classes of package com.sun.media:
//            BasicSinkModule, InputConnector, PlaybackEngine, Connector, 
//            BasicModule, ModuleListener, JMD, Log, 
//            BasicInputConnector, BasicConnector

public class BasicMuxModule extends BasicSinkModule
{
    class MyInputConnector extends BasicInputConnector
    {

        public String toString()
        {
            return super.toString() + ": " + getFormat();
        }

        public MyInputConnector()
        {
        }
    }


    protected BasicMuxModule(Multiplexer m, Format inputs[])
    {
        prefetching = false;
        started = false;
        closed = false;
        failed = false;
        prefetchSync = new Object();
        frameRate = 30F;
        lastFramesBehind = -1F;
        framesPlayed = 0;
        rtpVideoFormat = null;
        firstVideoFormat = null;
        bitsWritten = 0L;
        multiplexer = m;
        if(inputs != null)
        {
            ics = new InputConnector[inputs.length];
            for(int i = 0; i < inputs.length; i++)
            {
                InputConnector ic = new MyInputConnector();
                ic.setSize(1);
                ic.setModule(this);
                registerInputConnector(ConnectorNamePrefix + i, ic);
                ics[i] = ic;
                if((inputs[i] instanceof VideoFormat) && firstVideoFormat == null)
                {
                    firstVideoFormat = (VideoFormat)inputs[i];
                    String encoding = inputs[i].getEncoding().toUpperCase();
                    if(encoding.endsWith("RTP"))
                        rtpVideoFormat = firstVideoFormat;
                }
            }

            this.inputs = inputs;
        }
        if(multiplexer != null && (multiplexer instanceof Clock))
            setClock((Clock)multiplexer);
        setProtocol(0);
    }

    public boolean isThreaded()
    {
        return false;
    }

    public boolean doRealize()
    {
        if(multiplexer == null || inputs == null)
            return false;
        try
        {
            multiplexer.open();
        }
        catch(ResourceUnavailableException e)
        {
            return false;
        }
        prefetchMarkers = new boolean[ics.length];
        endMarkers = new boolean[ics.length];
        resettedMarkers = new boolean[ics.length];
        stopAtTimeMarkers = new boolean[ics.length];
        paused = new boolean[ics.length];
        prerollTrack = new boolean[ics.length];
        pauseSync = new Object[ics.length];
        elapseTime = new ElapseTime[ics.length];
        for(int i = 0; i < ics.length; i++)
        {
            prerollTrack[i] = false;
            pauseSync[i] = new Object();
            elapseTime[i] = new ElapseTime();
        }

        pause();
        return true;
    }

    public boolean doPrefetch()
    {
        if(!((PlaybackEngine)super.controller).prefetchEnabled)
        {
            return true;
        } else
        {
            resetPrefetchMarkers();
            prefetching = true;
            resume();
            return true;
        }
    }

    public void doFailedPrefetch()
    {
        prefetching = false;
    }

    public void abortPrefetch()
    {
        prefetching = false;
    }

    public void setPreroll(long wanted, long actual)
    {
        super.setPreroll(wanted, actual);
        for(int i = 0; i < elapseTime.length; i++)
        {
            elapseTime[i].setValue(actual);
            if((inputs[i] instanceof AudioFormat) && mpegAudio.matches(inputs[i]))
                prerollTrack[i] = false;
            else
                prerollTrack[i] = true;
        }

    }

    public void doStart()
    {
        super.doStart();
        resetEndMarkers();
        resetStopAtTimeMarkers();
        started = true;
        synchronized(prefetchSync)
        {
            prefetching = false;
            resume();
        }
    }

    public void doStop()
    {
        super.doStop();
        started = false;
        resetPrefetchMarkers();
        prefetching = true;
    }

    public void doDealloc()
    {
    }

    public void doClose()
    {
        multiplexer.close();
        closed = true;
        for(int i = 0; i < pauseSync.length; i++)
            synchronized(pauseSync[i])
            {
                pauseSync[i].notifyAll();
            }

    }

    void pause()
    {
        for(int i = 0; i < paused.length; i++)
            paused[i] = true;

    }

    void resume()
    {
        for(int i = 0; i < pauseSync.length; i++)
            synchronized(pauseSync[i])
            {
                paused[i] = false;
                pauseSync[i].notifyAll();
            }

    }

    public void connectorPushed(InputConnector ic)
    {
        int idx = -1;
        if(ics[0] == ic)
            idx = 0;
        else
        if(ics[1] == ic)
        {
            idx = 1;
        } else
        {
            for(int i = 2; i < ics.length; i++)
            {
                if(ics[i] != ic)
                    continue;
                idx = i;
                break;
            }

            if(idx == -1)
                throw new RuntimeException("BasicMuxModule: unmatched input connector!");
        }
        do
        {
            if(paused[idx])
                synchronized(pauseSync[idx])
                {
                    try
                    {
                        while(paused[idx] && !closed) 
                            pauseSync[idx].wait();
                    }
                    catch(Exception e) { }
                }
            if(super.stopTime <= -1L || elapseTime[idx].value < super.stopTime)
                break;
            paused[idx] = true;
            if(checkStopAtTime(idx))
            {
                if(multiplexer instanceof Drainable)
                    ((Drainable)multiplexer).drain();
                doStop();
                if(super.moduleListener != null)
                    super.moduleListener.stopAtTime(this);
            }
        } while(true);
        Buffer buffer = ic.getValidBuffer();
        int flags = buffer.getFlags();
        int rc = 0;
        if(super.resetted)
        {
            if((flags & 0x200) != 0 && checkResetted(idx))
            {
                super.resetted = false;
                doStop();
                if(super.moduleListener != null)
                    super.moduleListener.resetted(this);
            }
            ic.readReport();
            return;
        }
        if(failed || closed || buffer.isDiscard())
        {
            ic.readReport();
            return;
        }
        if(PlaybackEngine.DEBUG)
            super.jmd.moduleIn(this, 0, buffer, true);
        if((flags & 0x400) != 0 && super.moduleListener != null)
        {
            super.moduleListener.markedDataArrived(this, buffer);
            flags &= 0xfffffbff;
            buffer.setFlags(flags);
        }
        boolean dataPrerolled = false;
        Format format = buffer.getFormat();
        if(format == null)
        {
            format = ic.getFormat();
            buffer.setFormat(format);
        }
        if(elapseTime[idx].update(buffer.getLength(), buffer.getTimeStamp(), format))
        {
            if(prerollTrack[idx])
            {
                long target = getMediaNanoseconds();
                if(elapseTime[idx].value > target)
                {
                    if((format instanceof AudioFormat) && "LINEAR".equals(format.getEncoding()))
                    {
                        int remain = (int)ElapseTime.audioTimeToLen(elapseTime[idx].value - target, (AudioFormat)format);
                        int offset = (buffer.getOffset() + buffer.getLength()) - remain;
                        if(offset >= 0)
                        {
                            buffer.setOffset(offset);
                            buffer.setLength(remain);
                        }
                    }
                    prerollTrack[idx] = false;
                    elapseTime[idx].setValue(target);
                } else
                {
                    dataPrerolled = true;
                }
            }
            if(super.stopTime > -1L && elapseTime[idx].value > super.stopTime && (format instanceof AudioFormat))
            {
                long exceeded = elapseTime[idx].value - super.stopTime;
                int exceededLen = (int)ElapseTime.audioTimeToLen(exceeded, (AudioFormat)format);
                if(buffer.getLength() > exceededLen)
                    buffer.setLength(buffer.getLength() - exceededLen);
            }
        }
        if(super.moduleListener != null && (format instanceof VideoFormat))
        {
            long mt = getMediaNanoseconds();
            long lateBy = mt / 0xf4240L - buffer.getTimeStamp() / 0xf4240L - getLatency() / 0xf4240L;
            float fb = ((float)lateBy * frameRate) / 1000F;
            if(fb < 0.0F)
                fb = 0.0F;
            if(lastFramesBehind != fb && (flags & 0x20) == 0)
            {
                super.moduleListener.framesBehind(this, fb, ic);
                lastFramesBehind = fb;
            }
        }
        do
        {
            if(!dataPrerolled)
            {
                try
                {
                    rc = multiplexer.process(buffer, idx);
                }
                catch(Throwable e)
                {
                    Log.dumpStack(e);
                    if(super.moduleListener != null)
                        super.moduleListener.internalErrorOccurred(this);
                }
                if(rc == 0 && format == firstVideoFormat)
                    if(format == rtpVideoFormat)
                    {
                        if((flags & 0x800) > 0)
                            framesPlayed++;
                    } else
                    {
                        framesPlayed++;
                    }
            } else
            {
                rc = 0;
            }
            if((rc & 8) != 0)
            {
                failed = true;
                if(super.moduleListener != null)
                    super.moduleListener.pluginTerminated(this);
                ic.readReport();
                return;
            }
            if(prefetching && (!(multiplexer instanceof Prefetchable) || ((Prefetchable)multiplexer).isPrefetched()))
            {
                synchronized(prefetchSync)
                {
                    if(!started && prefetching && !super.resetted)
                        paused[idx] = true;
                    if(checkPrefetch(idx))
                        prefetching = false;
                }
                if(!prefetching && super.moduleListener != null)
                    super.moduleListener.bufferPrefetched(this);
            }
        } while(!super.resetted && rc == 2);
        bitsWritten += buffer.getLength();
        if(buffer.isEOM())
        {
            if(!super.resetted)
                paused[idx] = true;
            if(checkEnd(idx))
            {
                doStop();
                if(super.moduleListener != null)
                    super.moduleListener.mediaEnded(this);
            }
        }
        ic.readReport();
        if(PlaybackEngine.DEBUG)
            super.jmd.moduleIn(this, 0, buffer, false);
    }

    void resetPrefetchMarkers()
    {
        synchronized(prefetchMarkers)
        {
            for(int i = 0; i < prefetchMarkers.length; i++)
                prefetchMarkers[i] = false;

        }
    }

    boolean checkPrefetch(int idx)
    {
        boolean flag1;
        synchronized(prefetchMarkers)
        {
            prefetchMarkers[idx] = true;
            for(int i = 0; i < prefetchMarkers.length; i++)
                if(!prefetchMarkers[i])
                {
                    boolean flag = false;
                    return flag;
                }

            flag1 = true;
        }
        return flag1;
    }

    void resetEndMarkers()
    {
        synchronized(endMarkers)
        {
            for(int i = 0; i < endMarkers.length; i++)
                endMarkers[i] = false;

        }
    }

    boolean checkEnd(int idx)
    {
        boolean flag1;
        synchronized(endMarkers)
        {
            endMarkers[idx] = true;
            for(int i = 0; i < endMarkers.length; i++)
                if(!endMarkers[i])
                {
                    boolean flag = false;
                    return flag;
                }

            flag1 = true;
        }
        return flag1;
    }

    void resetResettedMarkers()
    {
        synchronized(resettedMarkers)
        {
            for(int i = 0; i < resettedMarkers.length; i++)
                resettedMarkers[i] = false;

        }
    }

    boolean checkResetted(int idx)
    {
        boolean flag1;
        synchronized(resettedMarkers)
        {
            resettedMarkers[idx] = true;
            for(int i = 0; i < resettedMarkers.length; i++)
                if(!resettedMarkers[i])
                {
                    boolean flag = false;
                    return flag;
                }

            flag1 = true;
        }
        return flag1;
    }

    void resetStopAtTimeMarkers()
    {
        synchronized(stopAtTimeMarkers)
        {
            for(int i = 0; i < stopAtTimeMarkers.length; i++)
                stopAtTimeMarkers[i] = false;

        }
    }

    boolean checkStopAtTime(int idx)
    {
        boolean flag1;
        synchronized(stopAtTimeMarkers)
        {
            stopAtTimeMarkers[idx] = true;
            for(int i = 0; i < stopAtTimeMarkers.length; i++)
                if(!stopAtTimeMarkers[i])
                {
                    boolean flag = false;
                    return flag;
                }

            flag1 = true;
        }
        return flag1;
    }

    protected void process()
    {
    }

    public void reset()
    {
        super.reset();
        resetResettedMarkers();
        prefetching = false;
    }

    public void triggerReset()
    {
        multiplexer.reset();
        synchronized(prefetchSync)
        {
            prefetching = false;
            if(super.resetted)
                resume();
        }
    }

    public DataSource getDataOutput()
    {
        return multiplexer.getDataOutput();
    }

    public Multiplexer getMultiplexer()
    {
        return multiplexer;
    }

    public Object[] getControls()
    {
        return multiplexer.getControls();
    }

    public Object getControl(String s)
    {
        return multiplexer.getControl(s);
    }

    public void setFormat(Connector connector, Format format)
    {
        if(format instanceof VideoFormat)
        {
            float fr = ((VideoFormat)format).getFrameRate();
            if(fr != -1F)
                frameRate = fr;
        }
    }

    public int getFramesPlayed()
    {
        return framesPlayed;
    }

    public void resetFramesPlayed()
    {
        framesPlayed = 0;
    }

    public long getBitsWritten()
    {
        return bitsWritten;
    }

    public void resetBitsWritten()
    {
        bitsWritten = 0L;
    }

    protected Multiplexer multiplexer;
    protected Format inputs[];
    protected InputConnector ics[];
    protected boolean prefetchMarkers[];
    protected boolean endMarkers[];
    protected boolean resettedMarkers[];
    protected boolean stopAtTimeMarkers[];
    protected boolean paused[];
    protected boolean prerollTrack[];
    private Object pauseSync[];
    protected ElapseTime elapseTime[];
    protected boolean prefetching;
    protected boolean started;
    private boolean closed;
    private boolean failed;
    private Object prefetchSync;
    private float frameRate;
    private float lastFramesBehind;
    private int framesPlayed;
    private VideoFormat rtpVideoFormat;
    private VideoFormat firstVideoFormat;
    public static String ConnectorNamePrefix = "input";
    private long bitsWritten;
    static AudioFormat mpegAudio = new AudioFormat("mpegaudio/rtp");

}
