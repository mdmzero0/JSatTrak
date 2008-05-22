// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPTransmitter.java

package com.sun.media.rtp;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.SSRCTable;
import com.sun.media.rtp.util.UDPPacketSender;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;
import javax.media.rtp.rtcp.SourceDescription;

// Referenced classes of package com.sun.media.rtp:
//            RTCPRawSender, SendSSRCInfo, RTCPPacket, RTCPCompoundPacket, 
//            RTCPBYEPacket, RTCPReportBlock, RTCPSRPacket, RTCPRRPacket, 
//            RTCPSDESPacket, RTCPSDES, RTCPSDESItem, SSRCInfo, 
//            SSRCCache, RTPSessionMgr, RTPTransStats, OverallTransStats, 
//            OverallStats, RTPSourceInfo

public class RTCPTransmitter
{

    public RTCPTransmitter(SSRCCache cache)
    {
        stats = null;
        sdescounter = 0;
        ssrcInfo = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        this.cache = cache;
        stats = cache.sm.defaultstats;
    }

    public RTCPTransmitter(SSRCCache cache, RTCPRawSender sender)
    {
        this(cache);
        setSender(sender);
        stats = cache.sm.defaultstats;
    }

    public RTCPTransmitter(SSRCCache cache, int port, String address)
        throws UnknownHostException, IOException
    {
        this(cache, new RTCPRawSender(port, address));
    }

    public RTCPTransmitter(SSRCCache cache, int port, String address, UDPPacketSender sender)
        throws UnknownHostException, IOException
    {
        this(cache, new RTCPRawSender(port, address, sender));
    }

    public void setSender(RTCPRawSender s)
    {
        sender = s;
    }

    public void setSSRCInfo(SSRCInfo info)
    {
        ssrcInfo = info;
    }

    public RTCPRawSender getSender()
    {
        return sender;
    }

    public void close()
    {
        if(sender != null)
            sender.closeConsumer();
    }

    protected void transmit(RTCPCompoundPacket p)
    {
        try
        {
            sender.sendTo(p);
            if(ssrcInfo instanceof SendSSRCInfo)
            {
                ((SendSSRCInfo)ssrcInfo).stats.total_rtcp++;
                cache.sm.transstats.rtcp_sent++;
            }
            cache.updateavgrtcpsize(((Packet) (p)).length);
            if(cache.initial)
                cache.initial = false;
            if(!cache.rtcpsent)
                cache.rtcpsent = true;
        }
        catch(IOException e)
        {
            stats.update(6, 1);
            cache.sm.transstats.transmit_failed++;
        }
    }

    public void report()
    {
        Vector repvec = makereports();
        RTCPPacket packets[] = new RTCPPacket[repvec.size()];
        repvec.copyInto(packets);
        RTCPCompoundPacket cp = new RTCPCompoundPacket(packets);
        transmit(cp);
    }

    public void bye(String reason)
    {
        if(reason != null)
            bye(ssrcInfo.ssrc, reason.getBytes());
        else
            bye(ssrcInfo.ssrc, null);
    }

    public void bye(int ssrc, byte reason[])
    {
        if(!cache.rtcpsent)
            return;
        cache.byestate = true;
        Vector repvec = makereports();
        RTCPPacket packets[] = new RTCPPacket[repvec.size() + 1];
        repvec.copyInto(packets);
        int ssrclist[] = new int[1];
        ssrclist[0] = ssrc;
        RTCPBYEPacket byep = new RTCPBYEPacket(ssrclist, reason);
        packets[packets.length - 1] = byep;
        RTCPCompoundPacket cp = new RTCPCompoundPacket(packets);
        if(jmfSecurity != null)
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    jmfSecurity.requestPermission(m, cl, args, 128);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.NETIO);
                    PolicyEngine.assertPermission(PermissionID.NETIO);
                }
            }
            catch(Throwable e)
            {
                jmfSecurity.permissionFailureNotification(128);
            }
        RTCPTransmitter _tmp = this;
        double delay;
        if(cache.aliveCount() > 50)
        {
            cache.reset(((Packet) (byep)).length);
            delay = cache.calcReportInterval(ssrcInfo.sender, false);
        } else
        {
            delay = 0.0D;
        }
        try
        {
            Thread.sleep((long)delay);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        transmit(cp);
        sdescounter = 0;
    }

    protected Vector makereports()
    {
        Vector packets = new Vector();
        SSRCInfo ourinfo = ssrcInfo;
        boolean senderreport = false;
        if(ourinfo.sender)
            senderreport = true;
        long time = System.currentTimeMillis();
        RTCPReportBlock reports[] = makerecreports(time);
        RTCPReportBlock firstrep[] = reports;
        if(reports.length > 31)
        {
            firstrep = new RTCPReportBlock[31];
            System.arraycopy(reports, 0, firstrep, 0, 31);
        }
        if(senderreport)
        {
            RTCPSRPacket srp = new RTCPSRPacket(ourinfo.ssrc, firstrep);
            packets.addElement(srp);
            long systime = ourinfo.systime == 0L ? System.currentTimeMillis() : ourinfo.systime;
            long secs = systime / 1000L;
            double msecs = (double)(systime - secs * 1000L) / 1000D;
            srp.ntptimestamplsw = (int)(msecs * 4294967296D);
            srp.ntptimestampmsw = secs;
            srp.rtptimestamp = (int)ourinfo.rtptime;
            srp.packetcount = ourinfo.maxseq - ourinfo.baseseq;
            srp.octetcount = ourinfo.bytesreceived;
        } else
        {
            RTCPRRPacket rrp = new RTCPRRPacket(ourinfo.ssrc, firstrep);
            packets.addElement(rrp);
        }
        if(firstrep != reports)
        {
            for(int offset = 31; offset < reports.length; offset += 31)
            {
                if(reports.length - offset < 31)
                    firstrep = new RTCPReportBlock[reports.length - offset];
                System.arraycopy(reports, offset, firstrep, 0, firstrep.length);
                RTCPRRPacket rrp = new RTCPRRPacket(ourinfo.ssrc, firstrep);
                packets.addElement(rrp);
            }

        }
        RTCPSDESPacket sp = new RTCPSDESPacket(new RTCPSDES[1]);
        sp.sdes[0] = new RTCPSDES();
        sp.sdes[0].ssrc = ssrcInfo.ssrc;
        Vector itemvec = new Vector();
        itemvec.addElement(new RTCPSDESItem(1, ourinfo.sourceInfo.getCNAME()));
        if(sdescounter % 3 == 0)
        {
            if(ourinfo.name != null && ourinfo.name.getDescription() != null)
                itemvec.addElement(new RTCPSDESItem(2, ourinfo.name.getDescription()));
            if(ourinfo.email != null && ourinfo.email.getDescription() != null)
                itemvec.addElement(new RTCPSDESItem(3, ourinfo.email.getDescription()));
            if(ourinfo.phone != null && ourinfo.phone.getDescription() != null)
                itemvec.addElement(new RTCPSDESItem(4, ourinfo.phone.getDescription()));
            if(ourinfo.loc != null && ourinfo.loc.getDescription() != null)
                itemvec.addElement(new RTCPSDESItem(5, ourinfo.loc.getDescription()));
            if(ourinfo.tool != null && ourinfo.tool.getDescription() != null)
                itemvec.addElement(new RTCPSDESItem(6, ourinfo.tool.getDescription()));
            if(ourinfo.note != null && ourinfo.note.getDescription() != null)
                itemvec.addElement(new RTCPSDESItem(7, ourinfo.note.getDescription()));
        }
        sdescounter++;
        sp.sdes[0].items = new RTCPSDESItem[itemvec.size()];
        itemvec.copyInto(sp.sdes[0].items);
        packets.addElement(sp);
        return packets;
    }

    protected RTCPReportBlock[] makerecreports(long time)
    {
        Vector reports = new Vector();
        for(Enumeration enum = cache.cache.elements(); enum.hasMoreElements();)
        {
            SSRCInfo info = (SSRCInfo)enum.nextElement();
            if(!info.ours && info.sender)
            {
                RTCPReportBlock rep = new RTCPReportBlock();
                rep.ssrc = info.ssrc;
                rep.lastseq = info.maxseq + info.cycles;
                rep.jitter = (int)info.jitter;
                rep.lsr = (int)(info.lastSRntptimestamp >> 32);
                rep.dlsr = (int)((double)(time - info.lastSRreceiptTime) * 65.536000000000001D);
                rep.packetslost = (int)(((rep.lastseq - (long)info.baseseq) + 1L) - (long)info.received);
                if(rep.packetslost < 0)
                    rep.packetslost = 0;
                double frac = (double)(rep.packetslost - info.prevlost) / (double)(rep.lastseq - (long)info.prevmaxseq);
                if(frac < 0.0D)
                    frac = 0.0D;
                rep.fractionlost = (int)(frac * 256D);
                info.prevmaxseq = (int)rep.lastseq;
                info.prevlost = rep.packetslost;
                reports.addElement(rep);
            }
        }

        RTCPReportBlock reportsarr[] = new RTCPReportBlock[reports.size()];
        reports.copyInto(reportsarr);
        return reportsarr;
    }

    RTCPRawSender sender;
    OverallStats stats;
    SSRCCache cache;
    int sdescounter;
    SSRCInfo ssrcInfo;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
    }
}
