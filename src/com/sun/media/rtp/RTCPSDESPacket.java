// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPSDESPacket.java

package com.sun.media.rtp;

import java.io.*;

// Referenced classes of package com.sun.media.rtp:
//            RTCPPacket, RTCPSDES, RTCPSDESItem

public class RTCPSDESPacket extends RTCPPacket
{

    public RTCPSDESPacket(RTCPPacket parent)
    {
        super(parent);
        super.type = 202;
    }

    public RTCPSDESPacket(RTCPSDES sdes[])
    {
        this.sdes = sdes;
        if(sdes.length > 31)
            throw new IllegalArgumentException("Too many SDESs");
        else
            return;
    }

    public String toString()
    {
        return "\tRTCP SDES Packet:\n" + RTCPSDES.toString(sdes);
    }

    public int calcLength()
    {
        int len = 4;
        for(int i = 0; i < sdes.length; i++)
        {
            int sublen = 5;
            for(int j = 0; j < sdes[i].items.length; j++)
                sublen += 2 + sdes[i].items[j].data.length;

            sublen = sublen + 3 & -4;
            len += sublen;
        }

        return len;
    }

    void assemble(DataOutputStream out)
        throws IOException
    {
        out.writeByte(128 + sdes.length);
        out.writeByte(202);
        out.writeShort(calcLength() - 4 >> 2);
        for(int i = 0; i < sdes.length; i++)
        {
            out.writeInt(sdes[i].ssrc);
            int sublen = 0;
            for(int j = 0; j < sdes[i].items.length; j++)
            {
                out.writeByte(sdes[i].items[j].type);
                out.writeByte(sdes[i].items[j].data.length);
                out.write(sdes[i].items[j].data);
                sublen += 2 + sdes[i].items[j].data.length;
            }

            for(int j = (sublen + 4 & -4) - sublen; j > 0; j--)
                out.writeByte(0);

        }

    }

    public RTCPSDES sdes[];
}
