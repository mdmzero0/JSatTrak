// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPTransmitter.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.RTPPacket;
import com.sun.media.rtp.util.UDPPacketSender;
import java.io.IOException;
import java.net.UnknownHostException;
import javax.media.Buffer;

// Referenced classes of package com.sun.media.rtp:
//            RTPRawSender, SSRCCache, RTPSessionMgr, OverallTransStats, 
//            SendSSRCInfo, SSRCInfo, RTPTransStats

public class RTPTransmitter
{

    public RTPTransmitter(SSRCCache cache)
    {
        this.cache = cache;
    }

    public RTPTransmitter(SSRCCache cache, RTPRawSender sender)
    {
        this(cache);
        setSender(sender);
    }

    public RTPTransmitter(SSRCCache cache, int port, String address)
        throws UnknownHostException, IOException
    {
        this(cache, new RTPRawSender(port, address));
    }

    public RTPTransmitter(SSRCCache cache, int port, String address, UDPPacketSender sender)
        throws UnknownHostException, IOException
    {
        this(cache, new RTPRawSender(port, address, sender));
    }

    public void setSender(RTPRawSender s)
    {
        sender = s;
    }

    public RTPRawSender getSender()
    {
        return sender;
    }

    public void close()
    {
        if(sender != null)
            sender.closeConsumer();
    }

    protected void transmit(RTPPacket p)
    {
        try
        {
            sender.sendTo(p);
        }
        catch(IOException e)
        {
            cache.sm.transstats.transmit_failed++;
        }
    }

    public void TransmitPacket(Buffer b, SendSSRCInfo info)
    {
        info.rtptime = info.getTimeStamp(b);
        if(b.getHeader() instanceof Long)
            info.systime = ((Long)b.getHeader()).longValue();
        else
            info.systime = System.currentTimeMillis();
        RTPPacket p = MakeRTPPacket(b, info);
        if(p == null)
        {
            return;
        } else
        {
            transmit(p);
            info.stats.total_pdu++;
            info.stats.total_bytes = info.stats.total_bytes + b.getLength();
            cache.sm.transstats.rtp_sent++;
            cache.sm.transstats.bytes_sent = cache.sm.transstats.bytes_sent + b.getLength();
            return;
        }
    }

    protected RTPPacket MakeRTPPacket(Buffer b, SendSSRCInfo info)
    {
        byte data[] = (byte[])b.getData();
        if(data == null)
            return null;
        Packet p = new Packet();
        p.data = data;
        p.offset = 0;
        p.length = b.getLength();
        p.received = false;
        RTPPacket rtp = new RTPPacket(p);
        if((b.getFlags() & 0x800) != 0)
            rtp.marker = 1;
        else
            rtp.marker = 0;
        info.packetsize += b.getLength();
        rtp.payloadType = ((SSRCInfo) (info)).payloadType;
        rtp.seqnum = (int)info.getSequenceNumber(b);
        rtp.timestamp = ((SSRCInfo) (info)).rtptime;
        rtp.ssrc = ((SSRCInfo) (info)).ssrc;
        rtp.payloadoffset = b.getOffset();
        rtp.payloadlength = b.getLength();
        info.bytesreceived += b.getLength();
        info.maxseq++;
        info.lasttimestamp = rtp.timestamp;
        return rtp;
    }

    RTPRawSender sender;
    SSRCCache cache;
}
