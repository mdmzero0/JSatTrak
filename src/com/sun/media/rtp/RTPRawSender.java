// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPRawSender.java

package com.sun.media.rtp;

import com.sun.media.Log;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.RTPPacket;
import com.sun.media.rtp.util.RTPPacketSender;
import com.sun.media.rtp.util.UDPPacket;
import com.sun.media.rtp.util.UDPPacketSender;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Vector;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.SessionAddress;

public class RTPRawSender extends PacketFilter
{

    public String filtername()
    {
        return "RTP Raw Packet Sender";
    }

    public RTPRawSender(int port, String address)
        throws UnknownHostException, IOException
    {
        socket = null;
        rtpConnector = null;
        destaddr = InetAddress.getByName(address);
        destport = port;
        super.destAddressList = null;
    }

    public RTPRawSender(int port, String address, UDPPacketSender sender)
        throws UnknownHostException, IOException
    {
        this(port, address);
        socket = sender.getSocket();
        setConsumer(sender);
        super.destAddressList = null;
    }

    public RTPRawSender(RTPPacketSender sender)
    {
        socket = null;
        rtpConnector = null;
        rtpConnector = sender.getConnector();
        setConsumer(sender);
    }

    public InetAddress getRemoteAddr()
    {
        return destaddr;
    }

    public void setSendBufSize(int size)
    {
        try
        {
            if(socket != null)
            {
                Class cls = socket.getClass();
                Method m = cls.getMethod("setSendBufferSize", new Class[] {
                    Integer.TYPE
                });
                m.invoke(socket, new Object[] {
                    new Integer(size)
                });
            } else
            if(rtpConnector != null)
                rtpConnector.setSendBufferSize(size);
        }
        catch(Exception e)
        {
            Log.comment("Cannot set send buffer size: " + e);
        }
    }

    public int getSendBufSize()
    {
        try
        {
            if(socket != null)
            {
                Class cls = socket.getClass();
                Method m = cls.getMethod("getSendBufferSize", null);
                Integer res = (Integer)m.invoke(socket, null);
                return res.intValue();
            }
            if(rtpConnector != null)
                return rtpConnector.getSendBufferSize();
        }
        catch(Exception e) { }
        return -1;
    }

    public Packet handlePacket(Packet p, int i)
    {
        return null;
    }

    public Packet handlePacket(Packet p, SessionAddress sessionAddress)
    {
        assemble((RTPPacket)p);
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
            udpp.remoteAddress = sessionAddress.getDataAddress();
            udpp.remotePort = sessionAddress.getDataPort();
            return udpp;
        }
    }

    public Packet handlePacket(Packet p)
    {
        assemble((RTPPacket)p);
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

    public void assemble(RTPPacket p)
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
    private DatagramSocket socket;
    private RTPConnector rtpConnector;
}
