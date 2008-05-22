// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPRRPacket.java

package com.sun.media.rtp;

import java.io.DataOutputStream;
import java.io.IOException;

// Referenced classes of package com.sun.media.rtp:
//            RTCPPacket, RTCPReportBlock

public class RTCPRRPacket extends RTCPPacket
{

    RTCPRRPacket(RTCPPacket parent)
    {
        super(parent);
        super.type = 201;
    }

    RTCPRRPacket(int ssrc, RTCPReportBlock reports[])
    {
        this.ssrc = ssrc;
        this.reports = reports;
        if(reports.length > 31)
            throw new IllegalArgumentException("Too many reports");
        else
            return;
    }

    public String toString()
    {
        return "\tRTCP RR (receiver report) packet for sync source " + ssrc + ":\n" + RTCPReportBlock.toString(reports);
    }

    public int calcLength()
    {
        return 8 + reports.length * 24;
    }

    void assemble(DataOutputStream out)
        throws IOException
    {
        out.writeByte(128 + reports.length);
        out.writeByte(201);
        out.writeShort(1 + reports.length * 6);
        out.writeInt(ssrc);
        for(int i = 0; i < reports.length; i++)
        {
            out.writeInt(reports[i].ssrc);
            out.writeInt((reports[i].packetslost & 0xffffff) + (reports[i].fractionlost << 24));
            out.writeInt((int)reports[i].lastseq);
            out.writeInt(reports[i].jitter);
            out.writeInt((int)reports[i].lsr);
            out.writeInt((int)reports[i].dlsr);
        }

    }

    int ssrc;
    RTCPReportBlock reports[];
}
