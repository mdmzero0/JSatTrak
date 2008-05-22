// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UDPPacket.java

package com.sun.media.rtp.util;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Date;

// Referenced classes of package com.sun.media.rtp.util:
//            Packet

public class UDPPacket extends Packet
{

    public UDPPacket()
    {
    }

    public String toString()
    {
        String s = "UDP Packet of size " + super.length;
        if(super.received)
            s = s + " received at " + new Date(super.receiptTime) + " on port " + localPort + " from " + remoteAddress + " port " + remotePort;
        return s;
    }

    public DatagramPacket datagrampacket;
    public int localPort;
    public int remotePort;
    public InetAddress remoteAddress;
}
