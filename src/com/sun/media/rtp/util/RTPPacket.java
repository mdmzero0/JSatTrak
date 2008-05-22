// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPPacket.java

package com.sun.media.rtp.util;

import java.io.*;

// Referenced classes of package com.sun.media.rtp.util:
//            Packet

public class RTPPacket extends Packet
{

    public RTPPacket()
    {
    }

    public RTPPacket(Packet p)
    {
        super(p);
        base = p;
    }

    public String toString()
    {
        String s = "RTP Packet:\n\tPayload Type: " + payloadType + "    Marker: " + marker + "\n\tSequence Number: " + seqnum + "\n\tTimestamp: " + timestamp + "\n\tSSRC (Sync Source): " + ssrc + "\n\tPayload Length: " + payloadlength + "    Payload Offset: " + payloadoffset + "\n";
        if(csrc.length > 0)
        {
            s = s + "Contributing sources:  " + csrc[0];
            for(int i = 1; i < csrc.length; i++)
                s = s + ", " + csrc[i];

            s = s + "\n";
        }
        if(extensionPresent)
            s = s + "\tExtension:  type " + extensionType + ", length " + extension.length + "\n";
        return s;
    }

    public int calcLength()
    {
        return payloadlength + 12;
    }

    public void assemble(int len, boolean encrypted)
    {
        super.length = len;
        super.offset = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        DataOutputStream out = new DataOutputStream(baos);
        try
        {
            out.writeByte(128);
            int mp = payloadType;
            if(marker == 1)
                mp = payloadType | 0x80;
            out.writeByte((byte)mp);
            out.writeShort(seqnum);
            out.writeInt((int)timestamp);
            out.writeInt(ssrc);
            out.write(base.data, payloadoffset, payloadlength);
            super.data = baos.toByteArray();
        }
        catch(IOException e)
        {
            System.out.println("caught IOException in DOS");
        }
    }

    public Object clone()
    {
        RTPPacket p = new RTPPacket((Packet)base.clone());
        p.extensionPresent = extensionPresent;
        p.marker = marker;
        p.payloadType = payloadType;
        p.seqnum = seqnum;
        p.timestamp = timestamp;
        p.ssrc = ssrc;
        p.csrc = (int[])csrc.clone();
        p.extensionType = extensionType;
        p.extension = extension;
        p.payloadoffset = payloadoffset;
        p.payloadlength = payloadlength;
        return p;
    }

    public Packet base;
    public boolean extensionPresent;
    public int marker;
    public int payloadType;
    public int seqnum;
    public long timestamp;
    public int ssrc;
    public int csrc[];
    public int extensionType;
    public byte extension[];
    public int payloadoffset;
    public int payloadlength;
}
