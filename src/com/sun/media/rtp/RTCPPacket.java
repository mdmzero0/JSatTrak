// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPPacket.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.Packet;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class RTCPPacket extends Packet
{

    public RTCPPacket()
    {
    }

    public RTCPPacket(Packet p)
    {
        super(p);
        base = p;
    }

    public RTCPPacket(RTCPPacket parent)
    {
        super(parent);
        base = parent.base;
    }

    public abstract int calcLength();

    abstract void assemble(DataOutputStream dataoutputstream)
        throws IOException;

    public Packet base;
    public int type;
    public static final int SR = 200;
    public static final int RR = 201;
    public static final int SDES = 202;
    public static final int BYE = 203;
    public static final int APP = 204;
    public static final int COMPOUND = -1;
}
