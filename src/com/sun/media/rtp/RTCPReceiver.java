// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPReceiver.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketConsumer;
import com.sun.media.rtp.util.PacketForwarder;
import com.sun.media.rtp.util.PacketSource;
import com.sun.media.rtp.util.SSRCTable;
import com.sun.media.rtp.util.UDPPacket;
import java.io.IOException;
import java.net.*;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.event.*;
import javax.media.rtp.rtcp.ReceiverReport;
import javax.media.rtp.rtcp.SenderReport;

// Referenced classes of package com.sun.media.rtp:
//            RTCPRawReceiver, RTCPPacket, RTCPCompoundPacket, RTCPSRPacket, 
//            RTCPReportBlock, RTCPRRPacket, RTCPSDESPacket, RecvSSRCInfo, 
//            RTCPBYEPacket, PassiveSSRCInfo, RTCPAPPPacket, SSRCCache, 
//            SSRCInfo, RTPSessionMgr, SSRCCacheCleaner, RTPEventHandler, 
//            RTCPSDES, RTPSourceInfo, StreamSynch

public class RTCPReceiver
    implements PacketConsumer
{

    public RTCPReceiver(SSRCCache cache)
    {
        rtcpstarted = false;
        sentrecvstrmap = false;
        type = 0;
        this.cache = cache;
        SSRCInfo info = cache.lookup(cache.ourssrc.ssrc);
    }

    public RTCPReceiver(SSRCCache cache, PacketSource source)
    {
        this(cache);
        PacketForwarder f = new PacketForwarder(source, this);
        f.startPF();
    }

    public RTCPReceiver(SSRCCache cache, DatagramSocket sock, StreamSynch streamSynch)
    {
        this(cache, ((PacketSource) (new RTCPRawReceiver(sock, cache.sm.defaultstats, streamSynch))));
    }

    public RTCPReceiver(SSRCCache cache, int port, String address, StreamSynch streamSynch)
        throws UnknownHostException, IOException
    {
        this(cache, ((PacketSource) (new RTCPRawReceiver(port | 1, address, cache.sm.defaultstats, streamSynch))));
    }

    public String consumerString()
    {
        return "RTCP Packet Receiver/Collector";
    }

    public void closeConsumer()
    {
    }

    public void sendTo(Packet p)
    {
        sendTo((RTCPPacket)p);
    }

    public void sendTo(RTCPPacket p)
    {
        SSRCInfo info = null;
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
        switch(p.type)
        {
        default:
            break;

        case -1: 
            RTCPCompoundPacket cp = (RTCPCompoundPacket)p;
            cache.updateavgrtcpsize(((Packet) (cp)).length);
            for(int i = 0; i < cp.packets.length; i++)
                sendTo(cp.packets[i]);

            if(cache.sm.cleaner != null)
                cache.sm.cleaner.setClean();
            break;

        case 200: 
            RTCPSRPacket srp = (RTCPSRPacket)p;
            type = 1;
            if(p.base instanceof UDPPacket)
                info = cache.get(srp.ssrc, ((UDPPacket)p.base).remoteAddress, ((UDPPacket)p.base).remotePort, 1);
            else
                info = cache.get(srp.ssrc, null, 0, 1);
            if(info == null)
                break;
            info.setAlive(true);
            info.lastSRntptimestamp = (srp.ntptimestampmsw << 32) + srp.ntptimestamplsw;
            info.lastSRrtptimestamp = srp.rtptimestamp;
            info.lastSRreceiptTime = ((Packet) (srp)).receiptTime;
            info.lastRTCPreceiptTime = ((Packet) (srp)).receiptTime;
            info.lastHeardFrom = ((Packet) (srp)).receiptTime;
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
            info.lastSRpacketcount = srp.packetcount;
            info.lastSRoctetcount = srp.octetcount;
            for(int i = 0; i < srp.reports.length; i++)
            {
                srp.reports[i].receiptTime = ((Packet) (srp)).receiptTime;
                int rbssrc = srp.reports[i].ssrc;
                RTCPReportBlock reporta[] = (RTCPReportBlock[])info.reports.get(rbssrc);
                if(reporta == null)
                {
                    reporta = new RTCPReportBlock[2];
                    reporta[0] = srp.reports[i];
                    info.reports.put(rbssrc, reporta);
                } else
                {
                    reporta[1] = reporta[0];
                    reporta[0] = srp.reports[i];
                }
            }

            if(info.probation > 0)
                break;
            if(!info.newpartsent && info.sourceInfo != null)
            {
                NewParticipantEvent evtsdes = new NewParticipantEvent(cache.sm, info.sourceInfo);
                cache.eventhandler.postEvent(evtsdes);
                info.newpartsent = true;
            }
            if(!info.recvstrmap && info.sourceInfo != null)
            {
                info.recvstrmap = true;
                StreamMappedEvent evt = new StreamMappedEvent(cache.sm, (ReceiveStream)info, info.sourceInfo);
                cache.eventhandler.postEvent(evt);
            }
            SenderReportEvent evtsr = new SenderReportEvent(cache.sm, (SenderReport)info);
            cache.eventhandler.postEvent(evtsr);
            break;

        case 201: 
            RTCPRRPacket rrp = (RTCPRRPacket)p;
            type = 2;
            if(p.base instanceof UDPPacket)
                info = cache.get(rrp.ssrc, ((UDPPacket)p.base).remoteAddress, ((UDPPacket)p.base).remotePort, 2);
            else
                info = cache.get(rrp.ssrc, null, 0, 2);
            if(info == null)
                break;
            info.setAlive(true);
            info.lastRTCPreceiptTime = ((Packet) (rrp)).receiptTime;
            info.lastHeardFrom = ((Packet) (rrp)).receiptTime;
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
            for(int i = 0; i < rrp.reports.length; i++)
            {
                rrp.reports[i].receiptTime = ((Packet) (rrp)).receiptTime;
                int rbssrc = rrp.reports[i].ssrc;
                RTCPReportBlock reporta[] = (RTCPReportBlock[])info.reports.get(rbssrc);
                if(reporta == null)
                {
                    reporta = new RTCPReportBlock[2];
                    reporta[0] = rrp.reports[i];
                    info.reports.put(rbssrc, reporta);
                } else
                {
                    reporta[1] = reporta[0];
                    reporta[0] = rrp.reports[i];
                }
            }

            if(!info.newpartsent && info.sourceInfo != null)
            {
                NewParticipantEvent evtsdes = new NewParticipantEvent(cache.sm, info.sourceInfo);
                cache.eventhandler.postEvent(evtsdes);
                info.newpartsent = true;
            }
            ReceiverReportEvent evt = new ReceiverReportEvent(cache.sm, (ReceiverReport)info);
            cache.eventhandler.postEvent(evt);
            break;

        case 202: 
            RTCPSDESPacket sdesp = (RTCPSDESPacket)p;
            for(int i = 0; i < sdesp.sdes.length; i++)
            {
                RTCPSDES chunk = sdesp.sdes[i];
                if(type == 1)
                    if(p.base instanceof UDPPacket)
                        info = cache.get(chunk.ssrc, ((UDPPacket)p.base).remoteAddress, ((UDPPacket)p.base).remotePort, 1);
                    else
                        info = cache.get(chunk.ssrc, null, 0, 1);
                if(type == 2)
                    if(p.base instanceof UDPPacket)
                        info = cache.get(chunk.ssrc, ((UDPPacket)p.base).remoteAddress, ((UDPPacket)p.base).remotePort, 2);
                    else
                        info = cache.get(chunk.ssrc, null, 0, 2);
                if(info == null)
                    break;
                info.setAlive(true);
                info.lastHeardFrom = ((Packet) (sdesp)).receiptTime;
                info.addSDESInfo(chunk);
            }

            if(info != null && !info.newpartsent && info.sourceInfo != null)
            {
                NewParticipantEvent evtsdes = new NewParticipantEvent(cache.sm, info.sourceInfo);
                cache.eventhandler.postEvent(evtsdes);
                info.newpartsent = true;
            }
            if(info != null && !info.recvstrmap && info.sourceInfo != null && (info instanceof RecvSSRCInfo))
            {
                info.recvstrmap = true;
                StreamMappedEvent evtr = new StreamMappedEvent(cache.sm, (ReceiveStream)info, info.sourceInfo);
                cache.eventhandler.postEvent(evtr);
            }
            type = 0;
            break;

        case 203: 
            RTCPBYEPacket byep = (RTCPBYEPacket)p;
            if(p.base instanceof UDPPacket)
                info = cache.get(byep.ssrc[0], ((UDPPacket)p.base).remoteAddress, ((UDPPacket)p.base).remotePort);
            else
                info = cache.get(byep.ssrc[0], null, 0);
            for(int i = 0; i < byep.ssrc.length; i++)
            {
                if(p.base instanceof UDPPacket)
                    info = cache.get(byep.ssrc[i], ((UDPPacket)p.base).remoteAddress, ((UDPPacket)p.base).remotePort);
                else
                    info = cache.get(byep.ssrc[i], null, 0);
                if(info == null)
                    break;
                if(!cache.byestate)
                {
                    info.setAlive(false);
                    info.byeReceived = true;
                    info.byeTime = ((Packet) (p)).receiptTime;
                    info.lastHeardFrom = ((Packet) (byep)).receiptTime;
                }
            }

            if(info == null)
                break;
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
            info.byereason = new String(byep.reason);
            if(info.byeReceived)
                break;
            boolean byepart = false;
            RTPSourceInfo sourceInfo = info.sourceInfo;
            if(sourceInfo != null && sourceInfo.getStreamCount() == 0)
                byepart = true;
            ByeEvent evtbye = null;
            if(info instanceof RecvSSRCInfo)
                evtbye = new ByeEvent(cache.sm, info.sourceInfo, (ReceiveStream)info, new String(byep.reason), byepart);
            if(info instanceof PassiveSSRCInfo)
                evtbye = new ByeEvent(cache.sm, info.sourceInfo, null, new String(byep.reason), byepart);
            cache.eventhandler.postEvent(evtbye);
            break;

        case 204: 
            RTCPAPPPacket appp = (RTCPAPPPacket)p;
            if(p.base instanceof UDPPacket)
                info = cache.get(appp.ssrc, ((UDPPacket)p.base).remoteAddress, ((UDPPacket)p.base).remotePort);
            else
                info = cache.get(appp.ssrc, null, 0);
            if(info == null)
                break;
            info.lastHeardFrom = ((Packet) (appp)).receiptTime;
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
            ApplicationEvent evnt = null;
            if(info instanceof RecvSSRCInfo)
                evnt = new ApplicationEvent(cache.sm, info.sourceInfo, (ReceiveStream)info, appp.subtype, null, appp.data);
            if(info instanceof PassiveSSRCInfo)
                evnt = new ApplicationEvent(cache.sm, info.sourceInfo, null, appp.subtype, null, appp.data);
            cache.eventhandler.postEvent(evnt);
            break;
        }
    }

    private static final int SR = 1;
    private static final int RR = 2;
    private boolean rtcpstarted;
    private boolean sentrecvstrmap;
    SSRCCache cache;
    private int type;
}
