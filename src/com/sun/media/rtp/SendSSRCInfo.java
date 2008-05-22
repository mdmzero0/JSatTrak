// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SendSSRCInfo.java

package com.sun.media.rtp;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import javax.media.rtp.*;
import javax.media.rtp.rtcp.*;

// Referenced classes of package com.sun.media.rtp:
//            SSRCInfo, RTPSinkStream, RTPTransStats, TrueRandom, 
//            SSRCCache, RTPSessionMgr, RTCPReporter

public class SendSSRCInfo extends SSRCInfo
    implements SenderReport, SendStream
{

    public SendSSRCInfo(SSRCCache cache, int ssrc)
    {
        super(cache, ssrc);
        inited = false;
        packetsize = 0;
        myformat = null;
        totalSamples = 0L;
        lastSeq = -1L;
        lastBufSeq = -1L;
        stats = null;
        rtcprep = null;
        super.baseseq = (int)TrueRandom.rand();
        super.maxseq = super.baseseq;
        super.lasttimestamp = (int)TrueRandom.rand();
        super.sender = true;
        super.wassender = true;
        super.sinkstream = new RTPSinkStream();
        stats = new RTPTransStats();
    }

    public SendSSRCInfo(SSRCInfo info)
    {
        super(info);
        inited = false;
        packetsize = 0;
        myformat = null;
        totalSamples = 0L;
        lastSeq = -1L;
        lastBufSeq = -1L;
        stats = null;
        rtcprep = null;
        super.baseseq = (int)TrueRandom.rand();
        super.maxseq = super.baseseq;
        super.lasttimestamp = (int)TrueRandom.rand();
        super.sender = true;
        super.wassender = true;
        super.sinkstream = new RTPSinkStream();
        stats = new RTPTransStats();
    }

    public long getTimeStamp(Buffer b)
    {
        if(b.getFormat() instanceof AudioFormat)
            if(mpegAudio.matches(b.getFormat()))
            {
                if(b.getTimeStamp() >= 0L)
                    return (b.getTimeStamp() * 90L) / 0xf4240L;
                else
                    return System.currentTimeMillis() * 90L;
            } else
            {
                totalSamples += calculateSampleCount(b);
                return totalSamples;
            }
        if(b.getFormat() instanceof VideoFormat)
        {
            if(b.getTimeStamp() >= 0L)
                return (b.getTimeStamp() * 90L) / 0xf4240L;
            else
                return System.currentTimeMillis() * 90L;
        } else
        {
            return b.getTimeStamp();
        }
    }

    private int calculateSampleCount(Buffer b)
    {
        AudioFormat f = (AudioFormat)b.getFormat();
        if(f == null)
            return -1;
        long t = f.computeDuration(b.getLength());
        if(t == -1L)
            return -1;
        if(f.getSampleRate() != -1D)
            return (int)(((double)t * f.getSampleRate()) / 1000000000D);
        if(f.getFrameRate() != -1D)
            return (int)(((double)t * f.getFrameRate()) / 1000000000D);
        else
            return -1;
    }

    public long getSequenceNumber(Buffer b)
    {
        long seq = b.getSequenceNumber();
        if(lastSeq == -1L)
        {
            lastSeq = (long)((double)System.currentTimeMillis() * Math.random());
            lastBufSeq = seq;
            return lastSeq;
        }
        if(seq - lastBufSeq > 1L)
            lastSeq += seq - lastBufSeq;
        else
            lastSeq++;
        lastBufSeq = seq;
        return lastSeq;
    }

    protected void setFormat(Format fmt)
    {
        myformat = fmt;
        if(super.sinkstream != null)
        {
            int rate = 0;
            if(fmt instanceof AudioFormat)
            {
                if(ulawAudio.matches(fmt) || dviAudio.matches(fmt) || mpegAudio.matches(fmt))
                    rate = (int)((AudioFormat)fmt).getSampleRate() * ((AudioFormat)fmt).getSampleSizeInBits();
                else
                if(gsmAudio.matches(fmt))
                    rate = 13200;
                else
                if(g723Audio.matches(fmt))
                    rate = 6300;
                super.sinkstream.rate = rate;
            }
        } else
        {
            System.err.println("RTPSinkStream is NULL");
        }
    }

    public long getSenderPacketCount()
    {
        return super.lastSRpacketcount;
    }

    public long getSenderByteCount()
    {
        return super.lastSRoctetcount;
    }

    public long getNTPTimeStampMSW()
    {
        return super.lastSRntptimestamp >> 32;
    }

    public long getNTPTimeStampLSW()
    {
        return super.lastSRntptimestamp;
    }

    public long getRTPTimeStamp()
    {
        return super.lastSRrtptimestamp;
    }

    public Feedback getSenderFeedback()
    {
        SSRCCache cache = getSSRCCache();
        Report report = null;
        Vector reports = null;
        Vector feedback = null;
        Feedback reportblk = null;
        try
        {
            Participant localpartc = cache.sm.getLocalParticipant();
            reports = localpartc.getReports();
            for(int i = 0; i < reports.size(); i++)
            {
                report = (Report)reports.elementAt(i);
                feedback = report.getFeedbackReports();
                for(int j = 0; j < feedback.size(); j++)
                {
                    reportblk = (Feedback)feedback.elementAt(j);
                    long ssrc = reportblk.getSSRC();
                    if(ssrc == getSSRC())
                        return reportblk;
                }

            }

            return null;
        }
        catch(NullPointerException e)
        {
            return null;
        }
    }

    public RTPStream getStream()
    {
        return this;
    }

    public Participant getParticipant()
    {
        SSRCCache cache = getSSRCCache();
        if((super.sourceInfo instanceof LocalParticipant) && cache.sm.IsNonParticipating())
            return null;
        else
            return super.sourceInfo;
    }

    public void setSourceDescription(SourceDescription userdesclist[])
    {
        super.setSourceDescription(userdesclist);
    }

    public void close()
    {
        try
        {
            stop();
        }
        catch(IOException e) { }
        SSRCCache cache = getSSRCCache();
        cache.sm.removeSendStream(this);
    }

    public SenderReport getSenderReport()
    {
        SSRCCache cache = getSSRCCache();
        Report report = null;
        Vector reports = null;
        Vector feedback = null;
        Feedback reportblk = null;
        try
        {
            Participant localpartc = cache.sm.getLocalParticipant();
            reports = localpartc.getReports();
            for(int i = 0; i < reports.size(); i++)
            {
                report = (Report)reports.elementAt(i);
                feedback = report.getFeedbackReports();
                for(int j = 0; j < feedback.size(); j++)
                {
                    reportblk = (Feedback)feedback.elementAt(j);
                    long ssrc = reportblk.getSSRC();
                    if(ssrc == getSSRC())
                        return (SenderReport)report;
                }

            }

            return null;
        }
        catch(NullPointerException e)
        {
            return null;
        }
    }

    public DataSource getDataSource()
    {
        return super.pds;
    }

    public void stop()
        throws IOException
    {
        if(super.pds != null)
            super.pds.stop();
        if(super.sinkstream != null)
            super.sinkstream.stop();
    }

    public void start()
        throws IOException
    {
        if(!inited)
        {
            inited = true;
            super.probation = 0;
            initsource((int)TrueRandom.rand());
            super.lasttimestamp = (int)TrueRandom.rand();
        }
        if(super.pds != null)
            super.pds.start();
        if(super.sinkstream != null)
            super.sinkstream.start();
    }

    protected void createDS()
    {
    }

    public int setBitRate(int rate)
    {
        if(super.sinkstream != null)
            super.sinkstream.rate = rate;
        return rate;
    }

    public TransmissionStats getSourceTransmissionStats()
    {
        return stats;
    }

    boolean inited;
    private static final int PACKET_SIZE = 4000;
    protected int packetsize;
    protected Format myformat;
    protected long totalSamples;
    protected long lastSeq;
    protected long lastBufSeq;
    protected RTPTransStats stats;
    protected RTCPReporter rtcprep;
    static AudioFormat dviAudio = new AudioFormat("dvi/rtp");
    static AudioFormat gsmAudio = new AudioFormat("gsm/rtp");
    static AudioFormat g723Audio = new AudioFormat("g723/rtp");
    static AudioFormat ulawAudio = new AudioFormat("ULAW/rtp");
    static AudioFormat mpegAudio = new AudioFormat("mpegaudio/rtp");
    static VideoFormat mpegVideo = new VideoFormat("mpeg/rtp");

}
