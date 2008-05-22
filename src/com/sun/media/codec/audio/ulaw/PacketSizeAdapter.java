// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Packetizer.java

package com.sun.media.codec.audio.ulaw;

import javax.media.Codec;

// Referenced classes of package com.sun.media.codec.audio.ulaw:
//            Packetizer

class PacketSizeAdapter extends com.sun.media.controls.PacketSizeAdapter
{

    public PacketSizeAdapter(Codec newOwner, int newPacketSize, boolean newIsSetable)
    {
        super(newOwner, newPacketSize, newIsSetable);
    }

    public int setPacketSize(int numBytes)
    {
        int numOfPackets = numBytes;
        if(numOfPackets < 10)
            numOfPackets = 10;
        if(numOfPackets > 8000)
            numOfPackets = 8000;
        super.packetSize = numOfPackets;
        ((Packetizer)super.owner).setPacketSize(super.packetSize);
        return super.packetSize;
    }
}
