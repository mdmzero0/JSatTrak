// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Packet.java

package com.sun.media.rtp.util;

import java.util.Date;

public class Packet
{

    public Packet()
    {
        received = true;
    }

    public Packet(Packet p)
    {
        received = true;
        data = p.data;
        offset = p.offset;
        length = p.length;
        received = p.received;
        receiptTime = p.receiptTime;
    }

    public String toString()
    {
        String s = "Packet of size " + length;
        if(received)
            s = s + " received at " + new Date(receiptTime);
        return s;
    }

    public Object clone()
    {
        Packet p = new Packet(this);
        p.data = (byte[])data.clone();
        return p;
    }

    public byte data[];
    public int offset;
    public int length;
    public boolean received;
    public long receiptTime;
}
