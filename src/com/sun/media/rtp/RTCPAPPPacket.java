// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPAPPPacket.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.Packet;
import java.io.*;

// Referenced classes of package com.sun.media.rtp:
//            RTCPPacket

public class RTCPAPPPacket extends RTCPPacket
{

    public RTCPAPPPacket(RTCPPacket parent)
    {
        super(parent);
        super.type = 204;
    }

    public RTCPAPPPacket(int ssrc, int name, int subtype, byte data[])
    {
        this.ssrc = ssrc;
        this.name = name;
        this.subtype = subtype;
        this.data = data;
        super.type = 204;
        super.received = false;
        if((data.length & 3) != 0)
            throw new IllegalArgumentException("Bad data length");
        if(subtype < 0 || subtype > 31)
            throw new IllegalArgumentException("Bad subtype");
        else
            return;
    }

    public String toString()
    {
        return "\tRTCP APP Packet from SSRC " + ssrc + " with name " + nameString(name) + " and subtype " + subtype + "\n\tData (length " + data.length + "): " + new String(data) + "\n";
    }

    public String nameString(int name)
    {
        return "" + (char)(name >>> 24) + (char)(name >>> 16 & 0xff) + (char)(name >>> 8 & 0xff) + (char)(name & 0xff);
    }

    public int calcLength()
    {
        return 12 + data.length;
    }

    void assemble(DataOutputStream out)
        throws IOException
    {
        out.writeByte(128 + subtype);
        out.writeByte(204);
        out.writeShort(2 + (data.length >> 2));
        out.writeInt(ssrc);
        out.writeInt(name);
        out.write(data);
    }

    int ssrc;
    int name;
    int subtype;
    byte data[];
}
