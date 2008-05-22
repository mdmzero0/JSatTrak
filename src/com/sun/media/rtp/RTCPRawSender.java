// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPRawSender.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.RTPPacketSender;
import com.sun.media.rtp.util.UDPPacket;
import com.sun.media.rtp.util.UDPPacketSender;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.media.rtp.SessionAddress;

// Referenced classes of package com.sun.media.rtp:
//            RTCPCompoundPacket

public class RTCPRawSender extends PacketFilter
{

    public String filtername()
    {
        return "RTCP Raw Packet Sender";
    }

    public RTCPRawSender(int port, String address)
        throws UnknownHostException, IOException
    {
        destaddr = InetAddress.getByName(address);
        destport = port | 1;
        super.destAddressList = null;
    }

    public RTCPRawSender(int port, String address, UDPPacketSender sender)
        throws UnknownHostException, IOException
    {
        this(port, address);
        setConsumer(sender);
        super.destAddressList = null;
    }

    public RTCPRawSender(RTPPacketSender sender)
    {
        setConsumer(sender);
    }

    public void addDestAddr(InetAddress newaddr)
    {
        int i = 0;
        if(super.destAddressList == null)
        {
            super.destAddressList = new Vector();
            super.destAddressList.addElement(destaddr);
        }
        for(i = 0; i < super.destAddressList.size(); i++)
        {
            InetAddress curraddr = (InetAddress)super.destAddressList.elementAt(i);
            if(curraddr.equals(newaddr))
                break;
        }

        if(i == super.destAddressList.size())
            super.destAddressList.addElement(newaddr);
    }

    public InetAddress getRemoteAddr()
    {
        return destaddr;
    }

    public Packet handlePacket(Packet p, SessionAddress sessionAddress)
    {
        assemble((RTCPCompoundPacket)p);
        com.sun.media.rtp.util.PacketConsumer consumer = getConsumer();
        if(consumer instanceof RTPPacketSender)
        {
            return p;
        } else
        {
            UDPPacket udpp = new UDPPacket();
            udpp.received = false;
            udpp.data = p.data;
            udpp.offset = p.offset;
            udpp.length = p.length;
            udpp.remoteAddress = sessionAddress.getControlAddress();
            udpp.remotePort = sessionAddress.getControlPort();
            return udpp;
        }
    }

    public Packet handlePacket(Packet p, int index)
    {
        assemble((RTCPCompoundPacket)p);
        UDPPacket udpp = new UDPPacket();
        udpp.received = false;
        udpp.data = p.data;
        udpp.offset = p.offset;
        udpp.length = p.length;
        udpp.remoteAddress = (InetAddress)super.destAddressList.elementAt(index);
        udpp.remotePort = destport;
        return udpp;
    }

    public Packet handlePacket(Packet p)
    {
        assemble((RTCPCompoundPacket)p);
        com.sun.media.rtp.util.PacketConsumer consumer = getConsumer();
        if(consumer instanceof RTPPacketSender)
        {
            return p;
        } else
        {
            UDPPacket udpp = new UDPPacket();
            udpp.received = false;
            udpp.data = p.data;
            udpp.offset = p.offset;
            udpp.length = p.length;
            udpp.remoteAddress = destaddr;
            udpp.remotePort = destport;
            return udpp;
        }
    }

    public void assemble(RTCPCompoundPacket p)
    {
        int len = p.calcLength();
        p.assemble(len, false);
    }

    public void setDestAddresses(Vector destAddresses)
    {
        super.destAddressList = destAddresses;
    }

    private InetAddress destaddr;
    private int destport;
}
