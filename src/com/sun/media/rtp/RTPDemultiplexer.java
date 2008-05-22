// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPDemultiplexer.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.RTPPacket;
import javax.media.Buffer;
import javax.media.format.AudioFormat;

// Referenced classes of package com.sun.media.rtp:
//            SourceRTPPacket, SSRCInfo, RTPSourceStream, StreamSynch, 
//            SSRCCache, RTPRawReceiver

public class RTPDemultiplexer
{

    public RTPDemultiplexer(SSRCCache c, RTPRawReceiver r, StreamSynch streamSynch)
    {
        cache = c;
        rtpr = r;
        this.streamSynch = streamSynch;
        buffer = new Buffer();
    }

    public String consumerString()
    {
        return "RTP DeMultiplexer";
    }

    public void demuxpayload(Packet p)
    {
        demuxpayload(((Packet) ((RTPPacket)p)));
    }

    public void demuxpayload(SourceRTPPacket sp)
    {
        SSRCInfo info = sp.ssrcinfo;
        RTPPacket p = sp.p;
        info.payloadType = p.payloadType;
        if(info.dstream != null)
        {
            buffer.setData(p.base.data);
            buffer.setFlags(0);
            if(p.marker == 1)
                buffer.setFlags(buffer.getFlags() | 0x800);
            buffer.setLength(p.payloadlength);
            buffer.setOffset(p.payloadoffset);
            if(info.dstream.getFormat() instanceof AudioFormat)
            {
                long ts = streamSynch.calcTimestamp(info.ssrc, p.payloadType, p.timestamp);
                buffer.setTimeStamp(ts);
            } else
            {
                long ts = streamSynch.calcTimestamp(info.ssrc, p.payloadType, p.timestamp);
                buffer.setTimeStamp(ts);
            }
            buffer.setFlags(buffer.getFlags() | 0x1000);
            buffer.setSequenceNumber(p.seqnum);
            buffer.setFormat(info.dstream.getFormat());
            info.dstream.add(buffer, info.wrapped, rtpr);
        }
    }

    private SSRCCache cache;
    private RTPRawReceiver rtpr;
    private Buffer buffer;
    private StreamSynch streamSynch;
}
