// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPRawReceiver.java

package com.sun.media.rtp;

import com.sun.media.Log;
import com.sun.media.rtp.util.BadFormatException;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.PacketSource;
import com.sun.media.rtp.util.RTPPacket;
import com.sun.media.rtp.util.RTPPacketReceiver;
import com.sun.media.rtp.util.UDPPacketReceiver;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import javax.media.rtp.*;

// Referenced classes of package com.sun.media.rtp:
//            OverallStats

public class RTPRawReceiver extends PacketFilter
{

    public String filtername()
    {
        return "RTP Raw Packet Receiver";
    }

    public RTPRawReceiver()
    {
        stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
    }

    public RTPRawReceiver(DatagramSocket sock, OverallStats stats)
    {
        this.stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        setSource(new UDPPacketReceiver(sock, 2000));
        this.stats = stats;
    }

    public RTPRawReceiver(SessionAddress localAddress, SessionAddress remoteAddress, OverallStats stats, DatagramSocket dataSocket)
        throws UnknownHostException, IOException, SocketException
    {
        this.stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        this.stats = stats;
        UDPPacketReceiver recv = new UDPPacketReceiver(localAddress.getDataPort(), localAddress.getDataHostAddress(), remoteAddress.getDataPort(), remoteAddress.getDataHostAddress(), 2000, dataSocket);
        setSource(recv);
        socket = recv.getSocket();
    }

    public RTPRawReceiver(int localPort, String localAddress, OverallStats stats)
        throws UnknownHostException, IOException, SocketException
    {
        this.stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        UDPPacketReceiver recv;
        setSource(recv = new UDPPacketReceiver(localPort & -2, localAddress, -1, null, 2000, null));
        socket = recv.getSocket();
        this.stats = stats;
    }

    public RTPRawReceiver(RTPPushDataSource networkdatasource, OverallStats stats)
    {
        this.stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        setSource(new RTPPacketReceiver(networkdatasource));
        this.stats = stats;
    }

    public RTPRawReceiver(RTPConnector rtpConnector, OverallStats stats)
    {
        this.stats = null;
        recvBufSizeSet = false;
        this.rtpConnector = null;
        try
        {
            setSource(new RTPPacketReceiver(rtpConnector.getDataInputStream()));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        this.rtpConnector = rtpConnector;
        this.stats = stats;
    }

    public void setRecvBufSize(int size)
    {
        try
        {
            if(socket != null)
            {
                Class cls = socket.getClass();
                Method m = cls.getMethod("setReceiveBufferSize", new Class[] {
                    Integer.TYPE
                });
                m.invoke(socket, new Object[] {
                    new Integer(size)
                });
            } else
            if(rtpConnector != null)
                rtpConnector.setReceiveBufferSize(size);
        }
        catch(Exception e)
        {
            Log.comment("Cannot set receive buffer size: " + e);
        }
    }

    public int getRecvBufSize()
    {
        try
        {
            if(socket != null)
            {
                Class cls = socket.getClass();
                Method m = cls.getMethod("getReceiveBufferSize", null);
                Integer res = (Integer)m.invoke(socket, null);
                return res.intValue();
            }
            if(rtpConnector != null)
                return rtpConnector.getReceiveBufferSize();
        }
        catch(Exception e) { }
        return -1;
    }

    public void close()
    {
        if(socket != null)
            socket.close();
        if(getSource() instanceof RTPPacketReceiver)
            getSource().closeSource();
    }

    public Packet handlePacket(Packet p, int index)
    {
        return null;
    }

    public Packet handlePacket(Packet p, SessionAddress a, boolean b)
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
        stats.update(1, p.length);
        RTPPacket result;
        try
        {
            result = parse(p);
        }
        catch(BadFormatException e)
        {
            stats.update(2, 1);
            return null;
        }
        if(!recvBufSizeSet)
        {
            recvBufSizeSet = true;
            switch(result.payloadType)
            {
            case 14: // '\016'
            case 26: // '\032'
            case 34: // '"'
            case 42: // '*'
                setRecvBufSize(64000);
                break;

            case 31: // '\037'
                setRecvBufSize(0x1f400);
                break;

            case 32: // ' '
                setRecvBufSize(0x1f400);
                break;

            default:
                if(result.payloadType >= 96 && result.payloadType <= 127)
                    setRecvBufSize(64000);
                break;
            }
        }
        return result;
    }

    public RTPPacket parse(Packet packet)
        throws BadFormatException
    {
        RTPPacket p = new RTPPacket(packet);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(((Packet) (p)).data, ((Packet) (p)).offset, ((Packet) (p)).length));
        try
        {
            int firstbyte = in.readUnsignedByte();
            if((firstbyte & 0xc0) != 128)
                throw new BadFormatException();
            if((firstbyte & 0x10) != 0)
                p.extensionPresent = true;
            int padlen = 0;
            if((firstbyte & 0x20) != 0)
                padlen = ((Packet) (p)).data[(((Packet) (p)).offset + ((Packet) (p)).length) - 1] & 0xff;
            firstbyte &= 0xf;
            p.payloadType = in.readUnsignedByte();
            p.marker = p.payloadType >> 7;
            p.payloadType &= 0x7f;
            p.seqnum = in.readUnsignedShort();
            p.timestamp = (long)in.readInt() & 0xffffffffL;
            p.ssrc = in.readInt();
            int offset = 0;
            if(p.extensionPresent)
            {
                p.extensionType = in.readUnsignedShort();
                int extlen = in.readUnsignedShort();
                extlen <<= 2;
                p.extension = new byte[extlen];
                in.readFully(p.extension);
                offset += extlen + 4;
            }
            p.csrc = new int[firstbyte];
            for(int i = 0; i < p.csrc.length; i++)
                p.csrc[i] = in.readInt();

            offset += 12 + (p.csrc.length << 2);
            p.payloadlength = ((Packet) (p)).length - (offset + padlen);
            if(p.payloadlength < 1)
                throw new BadFormatException();
            p.payloadoffset = offset + ((Packet) (p)).offset;
        }
        catch(EOFException e)
        {
            throw new BadFormatException("Unexpected end of RTP packet");
        }
        catch(IOException e)
        {
            throw new IllegalArgumentException("Impossible Exception");
        }
        return p;
    }

    private OverallStats stats;
    private boolean recvBufSizeSet;
    public DatagramSocket socket;
    private RTPConnector rtpConnector;
}
