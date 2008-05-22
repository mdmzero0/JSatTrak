// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPRawReceiver.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.BadFormatException;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.PacketSource;
import com.sun.media.rtp.util.RTPPacketReceiver;
import com.sun.media.rtp.util.UDPPacketReceiver;
import java.io.*;
import java.net.*;
import java.util.Vector;
import javax.media.rtp.*;

// Referenced classes of package com.sun.media.rtp:
//            RTCPCompoundPacket, RTCPSRPacket, RTCPReportBlock, RTCPRRPacket, 
//            RTCPSDESPacket, RTCPSDES, RTCPSDESItem, RTCPBYEPacket, 
//            RTCPAPPPacket, RTCPPacket, OverallStats, StreamSynch

public class RTCPRawReceiver extends PacketFilter
{

    public String filtername()
    {
        return "RTCP Raw Receiver";
    }

    public RTCPRawReceiver()
    {
        stats = null;
    }

    public RTCPRawReceiver(DatagramSocket sock, OverallStats stats, StreamSynch streamSynch)
    {
        this.stats = null;
        setSource(new UDPPacketReceiver(sock, 1000));
        this.stats = stats;
        this.streamSynch = streamSynch;
    }

    public RTCPRawReceiver(SessionAddress localAddress, SessionAddress remoteAddress, OverallStats stats, StreamSynch streamSynch, DatagramSocket controlSocket)
        throws UnknownHostException, IOException, SocketException
    {
        this.stats = null;
        this.streamSynch = streamSynch;
        this.stats = stats;
        UDPPacketReceiver recv = new UDPPacketReceiver(localAddress.getControlPort(), localAddress.getControlHostAddress(), remoteAddress.getControlPort(), remoteAddress.getControlHostAddress(), 1000, controlSocket);
        setSource(recv);
        socket = recv.getSocket();
    }

    public RTCPRawReceiver(int localPort, String localAddress, OverallStats stats, StreamSynch streamSynch)
        throws UnknownHostException, IOException, SocketException
    {
        this.stats = null;
        this.streamSynch = streamSynch;
        this.stats = stats;
        UDPPacketReceiver recv = new UDPPacketReceiver(localPort, localAddress, -1, null, 1000, null);
        setSource(recv);
        socket = recv.getSocket();
    }

    public RTCPRawReceiver(RTPPushDataSource networkdatasource, OverallStats stats, StreamSynch streamSynch)
    {
        this.stats = null;
        this.streamSynch = streamSynch;
        setSource(new RTPPacketReceiver(networkdatasource));
        this.stats = stats;
    }

    public RTCPRawReceiver(RTPConnector rtpConnector, OverallStats stats, StreamSynch streamSynch)
    {
        this.stats = null;
        this.streamSynch = streamSynch;
        try
        {
            setSource(new RTPPacketReceiver(rtpConnector.getControlInputStream()));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        this.stats = stats;
    }

    public void close()
    {
        if(socket != null)
            socket.close();
        if(getSource() instanceof RTPPacketReceiver)
            getSource().closeSource();
    }

    public Packet handlePacket(Packet p, int i)
    {
        return null;
    }

    public Packet handlePacket(Packet p, SessionAddress a, boolean control)
    {
        return null;
    }

    public Packet handlePacket(Packet p, SessionAddress a)
    {
        return null;
    }

    public Packet handlePacket(Packet p)
    {
        stats.update(0, 1);
        stats.update(11, 1);
        stats.update(1, p.length);
        RTCPPacket result;
        try
        {
            result = parse(p);
        }
        catch(BadFormatException e)
        {
            stats.update(13, 1);
            return null;
        }
        return result;
    }

    public RTCPPacket parse(Packet packet)
        throws BadFormatException
    {
        RTCPCompoundPacket base = new RTCPCompoundPacket(packet);
        Vector subpackets = new Vector(2);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(((Packet) (base)).data, ((Packet) (base)).offset, ((Packet) (base)).length));
        try
        {
            int length;
            for(int offset = 0; offset < ((Packet) (base)).length; offset += length)
            {
                int firstbyte = in.readUnsignedByte();
                if((firstbyte & 0xc0) != 128)
                    throw new BadFormatException();
                int type = in.readUnsignedByte();
                length = in.readUnsignedShort();
                length = length + 1 << 2;
                int padlen = 0;
                if(offset + length > ((Packet) (base)).length)
                    throw new BadFormatException();
                if(offset + length == ((Packet) (base)).length)
                {
                    if((firstbyte & 0x20) != 0)
                    {
                        padlen = ((Packet) (base)).data[(((Packet) (base)).offset + ((Packet) (base)).length) - 1] & 0xff;
                        if(padlen == 0)
                            throw new BadFormatException();
                    }
                } else
                if((firstbyte & 0x20) != 0)
                    throw new BadFormatException();
                int inlength = length - padlen;
                firstbyte &= 0x1f;
                RTCPPacket p;
                switch(type)
                {
                case 200: 
                    stats.update(12, 1);
                    if(inlength != 28 + 24 * firstbyte)
                    {
                        stats.update(18, 1);
                        System.out.println("bad format.");
                        throw new BadFormatException();
                    }
                    RTCPSRPacket srp = new RTCPSRPacket(base);
                    p = srp;
                    srp.ssrc = in.readInt();
                    srp.ntptimestampmsw = (long)in.readInt() & 0xffffffffL;
                    srp.ntptimestamplsw = (long)in.readInt() & 0xffffffffL;
                    srp.rtptimestamp = (long)in.readInt() & 0xffffffffL;
                    srp.packetcount = (long)in.readInt() & 0xffffffffL;
                    srp.octetcount = (long)in.readInt() & 0xffffffffL;
                    srp.reports = new RTCPReportBlock[firstbyte];
                    streamSynch.update(srp.ssrc, srp.rtptimestamp, srp.ntptimestampmsw, srp.ntptimestamplsw);
                    for(int i = 0; i < srp.reports.length; i++)
                    {
                        RTCPReportBlock report = new RTCPReportBlock();
                        srp.reports[i] = report;
                        report.ssrc = in.readInt();
                        long val = in.readInt();
                        val &= 0xffffffffL;
                        report.fractionlost = (int)(val >> 24);
                        report.packetslost = (int)(val & 0xffffffL);
                        report.lastseq = (long)in.readInt() & 0xffffffffL;
                        report.jitter = in.readInt();
                        report.lsr = (long)in.readInt() & 0xffffffffL;
                        report.dlsr = (long)in.readInt() & 0xffffffffL;
                    }

                    break;

                case 201: 
                    if(inlength != 8 + 24 * firstbyte)
                    {
                        stats.update(15, 1);
                        throw new BadFormatException();
                    }
                    RTCPRRPacket rrp = new RTCPRRPacket(base);
                    p = rrp;
                    rrp.ssrc = in.readInt();
                    rrp.reports = new RTCPReportBlock[firstbyte];
                    for(int i = 0; i < rrp.reports.length; i++)
                    {
                        RTCPReportBlock report = new RTCPReportBlock();
                        rrp.reports[i] = report;
                        report.ssrc = in.readInt();
                        long val = in.readInt();
                        val &= 0xffffffffL;
                        report.fractionlost = (int)(val >> 24);
                        report.packetslost = (int)(val & 0xffffffL);
                        report.lastseq = (long)in.readInt() & 0xffffffffL;
                        report.jitter = in.readInt();
                        report.lsr = (long)in.readInt() & 0xffffffffL;
                        report.dlsr = (long)in.readInt() & 0xffffffffL;
                    }

                    break;

                case 202: 
                    RTCPSDESPacket sdesp = new RTCPSDESPacket(base);
                    p = sdesp;
                    sdesp.sdes = new RTCPSDES[firstbyte];
                    int sdesoff = 4;
                    for(int i = 0; i < sdesp.sdes.length; i++)
                    {
                        RTCPSDES chunk = new RTCPSDES();
                        sdesp.sdes[i] = chunk;
                        chunk.ssrc = in.readInt();
                        sdesoff += 5;
                        Vector items = new Vector();
                        boolean gotcname = false;
                        int j;
                        while((j = in.readUnsignedByte()) != 0) 
                        {
                            if(j < 1 || j > 8)
                            {
                                stats.update(16, 1);
                                throw new BadFormatException();
                            }
                            if(j == 1)
                                gotcname = true;
                            RTCPSDESItem item = new RTCPSDESItem();
                            items.addElement(item);
                            item.type = j;
                            int sdeslen = in.readUnsignedByte();
                            item.data = new byte[sdeslen];
                            in.readFully(item.data);
                            sdesoff += 2 + sdeslen;
                        }
                        if(!gotcname)
                        {
                            stats.update(16, 1);
                            throw new BadFormatException();
                        }
                        chunk.items = new RTCPSDESItem[items.size()];
                        items.copyInto(chunk.items);
                        if((sdesoff & 3) != 0)
                        {
                            in.skip(4 - (sdesoff & 3));
                            sdesoff = sdesoff + 3 & -4;
                        }
                    }

                    if(inlength != sdesoff)
                    {
                        stats.update(16, 1);
                        throw new BadFormatException();
                    }
                    break;

                case 203: 
                    RTCPBYEPacket byep = new RTCPBYEPacket(base);
                    p = byep;
                    byep.ssrc = new int[firstbyte];
                    for(int i = 0; i < byep.ssrc.length; i++)
                        byep.ssrc[i] = in.readInt();

                    int reasonlen;
                    if(inlength > 4 + 4 * firstbyte)
                    {
                        reasonlen = in.readUnsignedByte();
                        byep.reason = new byte[reasonlen];
                        reasonlen++;
                    } else
                    {
                        reasonlen = 0;
                        byep.reason = new byte[0];
                    }
                    reasonlen = reasonlen + 3 & -4;
                    if(inlength != 4 + 4 * firstbyte + reasonlen)
                    {
                        stats.update(17, 1);
                        throw new BadFormatException();
                    }
                    in.readFully(byep.reason);
                    in.skip(reasonlen - byep.reason.length);
                    break;

                case 204: 
                    if(inlength < 12)
                        throw new BadFormatException();
                    RTCPAPPPacket appp = new RTCPAPPPacket(base);
                    p = appp;
                    appp.ssrc = in.readInt();
                    appp.name = in.readInt();
                    appp.subtype = firstbyte;
                    appp.data = new byte[inlength - 12];
                    in.readFully(appp.data);
                    in.skip(inlength - 12 - appp.data.length);
                    break;

                default:
                    stats.update(14, 1);
                    throw new BadFormatException();
                }
                p.offset = offset;
                p.length = length;
                subpackets.addElement(p);
                in.skipBytes(padlen);
            }

        }
        catch(EOFException e)
        {
            throw new BadFormatException("Unexpected end of RTCP packet");
        }
        catch(IOException e)
        {
            throw new IllegalArgumentException("Impossible Exception");
        }
        base.packets = new RTCPPacket[subpackets.size()];
        subpackets.copyInto(base.packets);
        return base;
    }

    public DatagramSocket socket;
    private StreamSynch streamSynch;
    private OverallStats stats;
}
