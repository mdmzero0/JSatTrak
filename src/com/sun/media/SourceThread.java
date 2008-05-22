// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicSourceModule.java

package com.sun.media;

import com.sun.media.rtp.util.RTPTimeBase;
import com.sun.media.util.LoopThread;
import java.io.PrintStream;
import javax.media.*;

// Referenced classes of package com.sun.media:
//            MyOutputConnector, BasicModule, ModuleListener, BasicOutputConnector, 
//            PlaybackEngine, JMD, BasicSourceModule, Log

class SourceThread extends LoopThread
    implements TrackListener
{

    public SourceThread(BasicSourceModule bsm, MyOutputConnector oc, int i)
    {
        index = 0;
        readBlocked = false;
        checkLatency = false;
        resetted = false;
        sequenceNum = 0L;
        lastRelativeTime = -1L;
        currentTime = 0L;
        counter = 0L;
        this.bsm = bsm;
        this.oc = oc;
        index = i;
        setName(getName() + ": " + oc.track);
        oc.track.setTrackListener(this);
    }

    public synchronized void start()
    {
        super.start();
        lastRelativeTime = -1L;
    }

    public void readHasBlocked(Track t)
    {
        readBlocked = true;
        if(((BasicModule) (bsm)).moduleListener != null)
            ((BasicModule) (bsm)).moduleListener.dataBlocked(bsm, true);
    }

    protected boolean process()
    {
        readBlocked = false;
        Buffer buffer = oc.getEmptyBuffer();
        if(PlaybackEngine.DEBUG)
            ((BasicModule) (bsm)).jmd.moduleOut(bsm, index, buffer, true);
        buffer.setOffset(0);
        buffer.setLength(0);
        buffer.setFlags(0);
        buffer.setSequenceNumber(sequenceNum++);
        if(resetted)
            synchronized(bsm.resetSync)
            {
                if(resetted)
                {
                    buffer.setFlags(512);
                    resetted = false;
                    pause();
                    if(bsm.checkAllPaused())
                    {
                        bsm.parser.stop();
                        bsm.parser.reset();
                    }
                    if(PlaybackEngine.DEBUG)
                        ((BasicModule) (bsm)).jmd.moduleOut(bsm, index, buffer, false);
                    oc.writeReport();
                    boolean flag = true;
                    return flag;
                }
            }
        try
        {
            oc.track.readFrame(buffer);
        }
        catch(Throwable e)
        {
            Log.dumpStack(e);
            if(((BasicModule) (bsm)).moduleListener != null)
                ((BasicModule) (bsm)).moduleListener.internalErrorOccurred(bsm);
        }
        if(PlaybackEngine.TRACE_ON && !bsm.verifyBuffer(buffer))
        {
            System.err.println("verify buffer failed: " + oc.track);
            Thread.dumpStack();
            if(((BasicModule) (bsm)).moduleListener != null)
                ((BasicModule) (bsm)).moduleListener.internalErrorOccurred(bsm);
        }
        if(buffer.getTimeStamp() != -1L && (buffer.getFlags() & remapTimeFlag) != 0)
        {
            boolean success = true;
            if((buffer.getFlags() & 0x80) != 0)
                success = remapSystemTime(buffer);
            else
            if((buffer.getFlags() & 0x100) != 0)
                success = remapRelativeTime(buffer);
            else
            if((buffer.getFlags() & 0x1000) != 0)
                success = remapRTPTime(buffer);
            if(!success)
            {
                buffer.setDiscard(true);
                oc.writeReport();
                return true;
            }
        }
        if(checkLatency)
        {
            buffer.setFlags(buffer.getFlags() | 0x400);
            if(((BasicModule) (bsm)).moduleListener != null)
                ((BasicModule) (bsm)).moduleListener.markedDataArrived(bsm, buffer);
            checkLatency = false;
        } else
        {
            buffer.setFlags(buffer.getFlags() & 0xfffffbff);
        }
        if(readBlocked && ((BasicModule) (bsm)).moduleListener != null)
            ((BasicModule) (bsm)).moduleListener.dataBlocked(bsm, false);
        if(buffer.isEOM())
            synchronized(bsm.resetSync)
            {
                if(!resetted)
                {
                    pause();
                    if(bsm.checkAllPaused())
                        bsm.parser.stop();
                }
            }
        else
            bsm.bitsRead += buffer.getLength();
        if(PlaybackEngine.DEBUG)
            ((BasicModule) (bsm)).jmd.moduleOut(bsm, index, buffer, false);
        oc.writeReport();
        return true;
    }

    private boolean remapRelativeTime(Buffer buffer)
    {
        buffer.setFlags(buffer.getFlags() & 0xfffffeff | 0x60);
        return true;
    }

    private boolean remapSystemTime(Buffer buffer)
    {
        if(!bsm.started)
            return false;
        long ts = buffer.getTimeStamp() - bsm.lastSystemTime;
        if(ts < 0L)
        {
            return false;
        } else
        {
            bsm.currentSystemTime = bsm.originSystemTime + ts;
            buffer.setTimeStamp(bsm.currentSystemTime);
            buffer.setFlags(buffer.getFlags() & 0xffffff7f | 0x60);
            return true;
        }
    }

    private boolean remapRTPTime(Buffer buffer)
    {
        if(buffer.getTimeStamp() <= 0L)
        {
            buffer.setTimeStamp(-1L);
            return true;
        }
        if(bsm.cname == null)
        {
            bsm.cname = bsm.engine.getCNAME();
            if(bsm.cname == null)
            {
                buffer.setTimeStamp(-1L);
                return true;
            }
        }
        if(bsm.rtpOffsetInvalid)
        {
            if(bsm.rtpMapperUpdatable == null)
            {
                bsm.rtpMapperUpdatable = RTPTimeBase.getMapperUpdatable(bsm.cname);
                if(bsm.rtpMapperUpdatable == null)
                    bsm.rtpOffsetInvalid = false;
            }
            if(bsm.rtpMapperUpdatable != null)
            {
                bsm.rtpMapperUpdatable.setOrigin(bsm.currentRTPTime);
                bsm.rtpMapperUpdatable.setOffset(buffer.getTimeStamp());
                bsm.rtpOffsetInvalid = false;
            }
        }
        if(bsm.rtpMapper == null)
            bsm.rtpMapper = RTPTimeBase.getMapper(bsm.cname);
        if(bsm.rtpMapper.getOffset() != bsm.oldOffset)
            bsm.oldOffset = bsm.rtpMapper.getOffset();
        long dur = buffer.getTimeStamp() - bsm.rtpMapper.getOffset();
        if(dur < 0L)
            if(bsm.rtpMapperUpdatable != null)
                bsm.rtpOffsetInvalid = true;
            else
                dur = 0L;
        bsm.currentRTPTime = bsm.rtpMapper.getOrigin() + dur;
        buffer.setTimeStamp(bsm.currentRTPTime);
        return true;
    }

    BasicSourceModule bsm;
    int index;
    protected MyOutputConnector oc;
    protected boolean readBlocked;
    protected boolean checkLatency;
    protected boolean resetted;
    long sequenceNum;
    static int remapTimeFlag = 4480;
    protected long lastRelativeTime;
    long currentTime;
    long counter;

}
