// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaEncoder.java

package com.ibm.media.codec.audio.dvi;

import javax.media.Codec;

// Referenced classes of package com.ibm.media.codec.audio.dvi:
//            JavaEncoder

class PacketSizeAdapter extends com.sun.media.controls.PacketSizeAdapter
{

    public PacketSizeAdapter(Codec newOwner, int newPacketSize, boolean newIsSetable)
    {
        super(newOwner, newPacketSize, newIsSetable);
    }

    public int setPacketSize(int numBytes)
    {
        int numOfPackets = numBytes * 2;
        if(numOfPackets < 10)
            numOfPackets = 10;
        if(numOfPackets > 4000)
            numOfPackets = 4000;
        super.packetSize = numOfPackets / 2;
        ((JavaEncoder)super.owner).setPacketSize(super.packetSize);
        return super.packetSize;
    }
}
