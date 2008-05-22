// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PacketFilter.java

package com.sun.media.rtp.util;

import java.io.IOException;
import java.util.Vector;
import javax.media.rtp.SessionAddress;

// Referenced classes of package com.sun.media.rtp.util:
//            UDPPacket, PacketSource, PacketConsumer, Packet

public abstract class PacketFilter
    implements PacketSource, PacketConsumer
{

    public PacketFilter()
    {
        destAddressList = null;
        peerlist = null;
        control = false;
    }

    public abstract Packet handlePacket(Packet packet);

    public abstract Packet handlePacket(Packet packet, int i);

    public abstract Packet handlePacket(Packet packet, SessionAddress sessionaddress);

    public Packet receiveFrom()
        throws IOException
    {
        Packet p = null;
        Packet rawp = source.receiveFrom();
        if(rawp != null)
            p = handlePacket(rawp);
        return p;
    }

    public void sendTo(Packet p)
        throws IOException
    {
        Packet origpacket = p;
        if(peerlist != null)
        {
            p = handlePacket(origpacket);
            for(int i = 0; i < peerlist.size(); i++)
            {
                SessionAddress a = (SessionAddress)peerlist.elementAt(i);
                if(!control)
                {
                    ((UDPPacket)p).remoteAddress = a.getDataAddress();
                    ((UDPPacket)p).remotePort = a.getDataPort();
                } else
                {
                    ((UDPPacket)p).remoteAddress = a.getControlAddress();
                    ((UDPPacket)p).remotePort = a.getControlPort();
                }
                if(p != null && consumer != null)
                    consumer.sendTo(p);
            }

        } else
        if(destAddressList != null)
        {
            for(int i = 0; i < destAddressList.size(); i++)
            {
                SessionAddress sa = (SessionAddress)destAddressList.elementAt(i);
                p = handlePacket(origpacket, sa);
                if(p != null && consumer != null)
                    consumer.sendTo(p);
            }

        } else
        if(destAddressList == null)
        {
            p = handlePacket(p);
            if(p != null && consumer != null)
                consumer.sendTo(p);
        }
    }

    public void close()
    {
    }

    public void closeSource()
    {
        close();
        if(source != null)
            source.closeSource();
    }

    public void closeConsumer()
    {
        close();
        if(consumer != null)
            consumer.closeConsumer();
    }

    public PacketConsumer getConsumer()
    {
        return consumer;
    }

    public PacketSource getSource()
    {
        return source;
    }

    public Vector getDestList()
    {
        return null;
    }

    public void setConsumer(PacketConsumer c)
    {
        consumer = c;
    }

    public void setSource(PacketSource s)
    {
        source = s;
    }

    public String filtername()
    {
        return getClass().getName();
    }

    public String consumerString()
    {
        if(consumer == null)
            return filtername();
        else
            return filtername() + " connected to " + consumer.consumerString();
    }

    public String sourceString()
    {
        if(source == null)
            return filtername();
        else
            return filtername() + " attached to " + source.sourceString();
    }

    PacketSource source;
    PacketConsumer consumer;
    public Vector destAddressList;
    public Vector peerlist;
    public boolean control;
}
