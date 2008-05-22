// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPCompoundPacket.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.Packet;
import java.io.*;

// Referenced classes of package com.sun.media.rtp:
//            RTCPPacket

public class RTCPCompoundPacket extends RTCPPacket
{

    public RTCPCompoundPacket(Packet base)
    {
        super(base);
        super.type = -1;
    }

    public RTCPCompoundPacket(RTCPPacket packets[])
    {
        this.packets = packets;
        super.type = -1;
        super.received = false;
    }

    public String toString()
    {
        return "RTCP Packet with the following subpackets:\n" + toString(packets);
    }

    public String toString(RTCPPacket packets[])
    {
        String s = "";
        for(int i = 0; i < packets.length; i++)
            s = s + packets[i];

        return s;
    }

    public int calcLength()
    {
        int len = 0;
        if(packets == null || packets.length < 1)
            throw new IllegalArgumentException("Bad RTCP Compound Packet");
        for(int i = 0; i < packets.length; i++)
            len += packets[i].calcLength();

        return len;
    }

    void assemble(DataOutputStream out)
        throws IOException
    {
        throw new IllegalArgumentException("Recursive Compound Packet");
    }

    public void assemble(int len, boolean encrypted)
    {
        super.length = len;
        super.offset = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        DataOutputStream out = new DataOutputStream(baos);
        int laststart;
        try
        {
            if(encrypted)
                super.offset += 4;
            laststart = super.offset;
            for(int i = 0; i < packets.length; i++)
            {
                laststart = baos.size();
                packets[i].assemble(out);
            }

        }
        catch(IOException e)
        {
            throw new NullPointerException("Impossible IO Exception");
        }
        int prelen = baos.size();
        super.data = baos.toByteArray();
        if(prelen > len)
            throw new NullPointerException("RTCP Packet overflow");
        if(prelen < len)
        {
            if(super.data.length < len)
                System.arraycopy(super.data, 0, super.data = new byte[len], 0, prelen);
            super.data[laststart] |= 0x20;
            super.data[len - 1] = (byte)(len - prelen);
            int temp = (super.data[laststart + 3] & 0xff) + (len - prelen >> 2);
            if(temp >= 256)
                super.data[laststart + 2] += len - prelen >> 10;
            super.data[laststart + 3] = (byte)temp;
        }
    }

    RTCPPacket packets[];
}
