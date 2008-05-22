// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPBYEPacket.java

package com.sun.media.rtp;

import java.io.*;

// Referenced classes of package com.sun.media.rtp:
//            RTCPPacket

public class RTCPBYEPacket extends RTCPPacket
{

    public RTCPBYEPacket(RTCPPacket parent)
    {
        super(parent);
        super.type = 203;
    }

    public RTCPBYEPacket(int ssrc[], byte reason[])
    {
        this.ssrc = ssrc;
        if(reason != null)
            this.reason = reason;
        else
            this.reason = new byte[0];
        if(ssrc.length > 31)
            throw new IllegalArgumentException("Too many SSRCs");
        else
            return;
    }

    public String toString()
    {
        return "\tRTCP BYE packet for sync source(s) " + toString(ssrc) + " for " + (reason.length <= 0 ? "no reason" : "reason " + new String(reason)) + "\n";
    }

    public String toString(int ints[])
    {
        if(ints.length == 0)
            return "(none)";
        String s = "" + ints[0];
        for(int i = 1; i < ints.length; i++)
            s = s + ", " + ints[i];

        return s;
    }

    public int calcLength()
    {
        return 4 + (ssrc.length << 2) + (reason.length <= 0 ? 0 : reason.length + 4 & -4);
    }

    void assemble(DataOutputStream out)
        throws IOException
    {
        out.writeByte(128 + ssrc.length);
        out.writeByte(203);
        out.writeShort(ssrc.length + (reason.length <= 0 ? 0 : reason.length + 4 >> 2));
        for(int i = 0; i < ssrc.length; i++)
            out.writeInt(ssrc[i]);

        if(reason.length > 0)
        {
            out.writeByte(reason.length);
            out.write(reason);
            for(int i = (reason.length + 4 & -4) - reason.length - 1; i > 0; i--)
                out.writeByte(0);

        }
    }

    int ssrc[];
    byte reason[];
}
