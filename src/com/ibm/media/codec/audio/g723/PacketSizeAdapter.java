// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Packetizer.java

package com.ibm.media.codec.audio.g723;

import javax.media.Codec;

// Referenced classes of package com.ibm.media.codec.audio.g723:
//            Packetizer

class PacketSizeAdapter extends com.sun.media.controls.PacketSizeAdapter
{

    public PacketSizeAdapter(Codec newOwner, int newPacketSize, boolean newIsSetable)
    {
        super(newOwner, newPacketSize, newIsSetable);
    }

    public int setPacketSize(int numBytes)
    {
        int numOfPackets = numBytes / 24;
        if(numOfPackets < 1)
            numOfPackets = 1;
        if(numOfPackets > 100)
            numOfPackets = 100;
        super.packetSize = numOfPackets * 24;
        ((Packetizer)super.owner).setPacketSize(super.packetSize);
        return super.packetSize;
    }
}
