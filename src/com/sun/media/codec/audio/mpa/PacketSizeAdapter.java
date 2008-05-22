// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Packetizer.java

package com.sun.media.codec.audio.mpa;

import javax.media.Codec;

// Referenced classes of package com.sun.media.codec.audio.mpa:
//            Packetizer

class PacketSizeAdapter extends com.sun.media.controls.PacketSizeAdapter
{

    public PacketSizeAdapter(Codec newOwner, int newPacketSize, boolean newIsSetable)
    {
        super(newOwner, newPacketSize, newIsSetable);
    }

    public int setPacketSize(int numBytes)
    {
        if(numBytes < 110)
            numBytes = 110;
        if(numBytes > 1456)
            numBytes = 1456;
        super.packetSize = numBytes;
        ((Packetizer)super.owner).setPacketSize(super.packetSize);
        return super.packetSize;
    }
}
