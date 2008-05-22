// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPReceiver.java

package com.sun.media.rtp;

import com.sun.media.Log;
import com.sun.media.protocol.rtp.DataSource;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.PacketSource;
import com.sun.media.rtp.util.RTPPacket;
import com.sun.media.rtp.util.SSRCTable;
import com.sun.media.rtp.util.UDPPacket;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.*;

// Referenced classes of package com.sun.media.rtp:
//            RTPRawReceiver, RTPControlImpl, BufferControlImpl, RTPSourceStream, 
//            SourceRTPPacket, SSRCCache, RTPSessionMgr, SSRCInfo, 
//            RTPStats, RTPEventHandler, FormatInfo, RTPDemultiplexer

public class RTPReceiver extends PacketFilter
{

    public String filtername()
    {
        return "RTP Packet Receiver";
    }

    public RTPReceiver(SSRCCache cache, RTPDemultiplexer rtpdemux)
    {
        lastseqnum = -1;
        rtcpstarted = false;
        setpriority = false;
        mismatchprinted = false;
        content = "";
        probationList = new SSRCTable();
        initBC = false;
        controlstr = "javax.media.rtp.RTPControl";
        errorPayload = -1;
        this.cache = cache;
        rtpdemultiplexer = rtpdemux;
        setConsumer(null);
    }

    public RTPReceiver(SSRCCache cache, RTPDemultiplexer rtpdemux, PacketSource source)
    {
        this(cache, rtpdemux);
        setSource(source);
    }

    public RTPReceiver(SSRCCache cache, RTPDemultiplexer rtpdemux, DatagramSocket sock)
    {
        this(cache, rtpdemux, ((PacketSource) (new RTPRawReceiver(sock, cache.sm.defaultstats))));
    }

    public RTPReceiver(SSRCCache cache, RTPDemultiplexer rtpdemux, int port, String address)
        throws UnknownHostException, IOException
    {
        this(cache, rtpdemux, ((PacketSource) (new RTPRawReceiver(port & -2, address, cache.sm.defaultstats))));
    }

    public Packet handlePacket(Packet p)
    {
        return handlePacket((RTPPacket)p);
    }

    public Packet handlePacket(Packet p, int index)
    {
        return null;
    }

    public Packet handlePacket(Packet p, SessionAddress a)
    {
        return null;
    }

    public Packet handlePacket(Packet p, SessionAddress a, boolean b)
    {
        return null;
    }

    public Packet handlePacket(RTPPacket p)
    {
        SSRCInfo info = null;
        if(p.base instanceof UDPPacket)
        {
            InetAddress remoteAddress = ((UDPPacket)p.base).remoteAddress;
            if(cache.sm.bindtome && !cache.sm.isBroadcast(cache.sm.dataaddress) && !remoteAddress.equals(cache.sm.dataaddress))
                return null;
        } else
        if(p.base instanceof Packet)
            p.base.toString();
        if(info == null)
            if(p.base instanceof UDPPacket)
                info = cache.get(p.ssrc, ((UDPPacket)p.base).remoteAddress, ((UDPPacket)p.base).remotePort, 1);
            else
                info = cache.get(p.ssrc, null, 0, 1);
        if(info == null)
            return null;
        for(int i = 0; i < p.csrc.length; i++)
        {
            SSRCInfo cinfo = null;
            if(p.base instanceof UDPPacket)
                cinfo = cache.get(p.csrc[i], ((UDPPacket)p.base).remoteAddress, ((UDPPacket)p.base).remotePort, 1);
            else
                cinfo = cache.get(p.csrc[i], null, 0, 1);
            if(cinfo != null)
                cinfo.lastHeardFrom = ((Packet) (p)).receiptTime;
        }

        if(info.lastPayloadType != -1 && info.lastPayloadType == p.payloadType && mismatchprinted)
            return null;
        if(!info.sender)
        {
            info.initsource(p.seqnum);
            info.payloadType = p.payloadType;
        }
        int deltaseq = p.seqnum - info.maxseq;
        if(info.maxseq + 1 != p.seqnum && deltaseq > 0)
            info.stats.update(0, deltaseq - 1);
        if(info.wrapped)
            info.wrapped = false;
        boolean justOutOfProbation = false;
        if(info.probation > 0)
        {
            if(p.seqnum == info.maxseq + 1)
            {
                info.probation--;
                info.maxseq = p.seqnum;
                if(info.probation == 0)
                    justOutOfProbation = true;
            } else
            {
                info.probation = 1;
                info.maxseq = p.seqnum;
                info.stats.update(2);
            }
        } else
        if(deltaseq < 3000)
        {
            if(p.seqnum < info.baseseq)
            {
                info.cycles += 0x10000;
                info.wrapped = true;
            }
            info.maxseq = p.seqnum;
        } else
        if(deltaseq <= 65436)
        {
            info.stats.update(3);
            if(p.seqnum == info.lastbadseq)
                info.initsource(p.seqnum);
            else
                info.lastbadseq = p.seqnum + 1 & 0xffff;
        } else
        {
            info.stats.update(4);
        }
        boolean unicast = cache.sm.isUnicast();
        if(unicast)
            if(!rtcpstarted)
            {
                cache.sm.startRTCPReports(((UDPPacket)p.base).remoteAddress);
                rtcpstarted = true;
                byte lsb[] = cache.sm.controladdress.getAddress();
                int address = lsb[3] & 0xff;
                if((address & 0xff) == 255)
                {
                    cache.sm.addUnicastAddr(cache.sm.controladdress);
                } else
                {
                    InetAddress localaddr = null;
                    boolean localfound = true;
                    try
                    {
                        localaddr = InetAddress.getLocalHost();
                    }
                    catch(UnknownHostException e)
                    {
                        localfound = false;
                    }
                    if(localfound)
                        cache.sm.addUnicastAddr(localaddr);
                }
            } else
            if(!cache.sm.isSenderDefaultAddr(((UDPPacket)p.base).remoteAddress))
                cache.sm.addUnicastAddr(((UDPPacket)p.base).remoteAddress);
        info.received++;
        info.stats.update(1);
        if(info.probation > 0)
        {
            probationList.put(info.ssrc, p.clone());
            return null;
        }
        info.maxseq = p.seqnum;
        if(info.lastPayloadType != -1 && info.lastPayloadType != p.payloadType)
        {
            info.currentformat = null;
            if(info.dsource != null)
            {
                RTPControlImpl control = (RTPControlImpl)info.dsource.getControl(controlstr);
                if(control != null)
                {
                    control.currentformat = null;
                    control.payload = -1;
                }
            }
            info.lastPayloadType = p.payloadType;
            if(info.dsource != null)
                try
                {
                    info.dsource.stop();
                }
                catch(IOException e)
                {
                    System.err.println("Stopping DataSource after PCE " + e.getMessage());
                }
            RemotePayloadChangeEvent event = new RemotePayloadChangeEvent(cache.sm, (ReceiveStream)info, info.lastPayloadType, p.payloadType);
            cache.eventhandler.postEvent(event);
        }
        if(info.currentformat == null)
        {
            info.currentformat = cache.sm.formatinfo.get(p.payloadType);
            if(info.currentformat == null)
            {
                if(errorPayload != p.payloadType)
                {
                    Log.error("No format has been registered for RTP Payload type " + p.payloadType);
                    errorPayload = p.payloadType;
                }
                return p;
            }
            if(info.dstream != null)
                info.dstream.setFormat(info.currentformat);
        }
        if(info.currentformat == null)
        {
            System.err.println("No Format for PT= " + p.payloadType);
            return p;
        }
        if(info.dsource != null)
        {
            RTPControlImpl control = (RTPControlImpl)info.dsource.getControl(controlstr);
            if(control != null)
            {
                javax.media.Format fmt = cache.sm.formatinfo.get(p.payloadType);
                control.currentformat = fmt;
            }
        }
        if(!initBC)
        {
            ((BufferControlImpl)cache.sm.buffercontrol).initBufferControl(info.currentformat);
            initBC = true;
        }
        if(!info.streamconnect)
        {
            DataSource source = (DataSource)cache.sm.dslist.get(info.ssrc);
            if(source == null)
            {
                DataSource defaultsource = cache.sm.getDataSource(null);
                if(defaultsource == null)
                {
                    source = cache.sm.createNewDS(null);
                    cache.sm.setDefaultDSassigned(info.ssrc);
                } else
                if(!cache.sm.isDefaultDSassigned())
                {
                    source = defaultsource;
                    cache.sm.setDefaultDSassigned(info.ssrc);
                } else
                {
                    source = cache.sm.createNewDS(info.ssrc);
                }
            }
            javax.media.protocol.PushBufferStream streams[] = source.getStreams();
            info.dsource = source;
            info.dstream = (RTPSourceStream)streams[0];
            info.dstream.setContentDescriptor(content);
            info.dstream.setFormat(info.currentformat);
            RTPControlImpl control = (RTPControlImpl)info.dsource.getControl(controlstr);
            if(control != null)
            {
                javax.media.Format fmt = cache.sm.formatinfo.get(p.payloadType);
                control.currentformat = fmt;
                control.stream = info;
            }
            info.streamconnect = true;
        }
        if(info.dsource != null)
            info.active = true;
        if(!info.newrecvstream)
        {
            NewReceiveStreamEvent evt = new NewReceiveStreamEvent(cache.sm, (ReceiveStream)info);
            info.newrecvstream = true;
            cache.eventhandler.postEvent(evt);
        }
        if(info.lastRTPReceiptTime != 0L && info.lastPayloadType == p.payloadType)
        {
            long abstimediff = ((Packet) (p)).receiptTime - info.lastRTPReceiptTime;
            abstimediff = (abstimediff * (long)cache.clockrate[info.payloadType]) / 1000L;
            long rtptimediff = p.timestamp - info.lasttimestamp;
            double timediff = abstimediff - rtptimediff;
            if(timediff < 0.0D)
                timediff = -timediff;
            info.jitter += 0.0625D * (timediff - info.jitter);
        }
        info.lastRTPReceiptTime = ((Packet) (p)).receiptTime;
        info.lasttimestamp = p.timestamp;
        info.payloadType = p.payloadType;
        info.lastPayloadType = p.payloadType;
        info.bytesreceived += p.payloadlength;
        info.lastHeardFrom = ((Packet) (p)).receiptTime;
        if(info.quiet)
        {
            info.quiet = false;
            ActiveReceiveStreamEvent event = null;
            if(info instanceof ReceiveStream)
                event = new ActiveReceiveStreamEvent(cache.sm, info.sourceInfo, (ReceiveStream)info);
            else
                event = new ActiveReceiveStreamEvent(cache.sm, info.sourceInfo, null);
            cache.eventhandler.postEvent(event);
        }
        SourceRTPPacket sp = new SourceRTPPacket(p, info);
        if(info.dsource != null)
        {
            if(mismatchprinted)
                mismatchprinted = false;
            if(justOutOfProbation)
            {
                RTPPacket pp = (RTPPacket)probationList.remove(info.ssrc);
                if(pp != null)
                    rtpdemultiplexer.demuxpayload(new SourceRTPPacket(pp, info));
            }
            rtpdemultiplexer.demuxpayload(sp);
        }
        return p;
    }

    SSRCCache cache;
    RTPDemultiplexer rtpdemultiplexer;
    int lastseqnum;
    private boolean rtcpstarted;
    private boolean setpriority;
    private boolean mismatchprinted;
    private String content;
    SSRCTable probationList;
    static final int MAX_DROPOUT = 3000;
    static final int MAX_MISORDER = 100;
    static final int SEQ_MOD = 0x10000;
    static final int MIN_SEQUENTIAL = 2;
    private boolean initBC;
    public String controlstr;
    private int errorPayload;
}
