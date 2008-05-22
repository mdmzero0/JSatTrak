// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPDePacketizer.java

package com.sun.media.codec.video.jpeg;

import javax.media.Buffer;

// Referenced classes of package com.sun.media.codec.video.jpeg:
//            JPEGFrame

public class RTPDePacketizer
{

    public RTPDePacketizer()
    {
        currentFrame = null;
        frameBuffer = null;
        sequenceNumber = 0;
        quality = 0;
        type = -1;
        lastJFIFHeader = null;
        lastQuality = -2;
        lastType = -1;
        lastWidth = -1;
        lastHeight = -1;
    }

    public int getQuality()
    {
        return quality;
    }

    public int getType()
    {
        return type;
    }

    public int process(Buffer inBuffer, Buffer outBuffer)
    {
        if(currentFrame != null && inBuffer.getTimeStamp() != currentFrame.rtptimestamp)
            currentFrame = null;
        if(getFragOffset((byte[])inBuffer.getData(), inBuffer.getOffset()) == 0)
            currentFrame = new JPEGFrame(this, inBuffer, (byte[])outBuffer.getData());
        else
        if(currentFrame != null)
            currentFrame.add(inBuffer, 0);
        else
            return 4;
        if((inBuffer.getFlags() & 0x800) != 0)
        {
            if(currentFrame.gotAllPackets(inBuffer.getSequenceNumber()))
            {
                currentFrame.completeTransfer(inBuffer, outBuffer);
                currentFrame = null;
                return 0;
            } else
            {
                currentFrame = null;
                return 4;
            }
        } else
        {
            return 4;
        }
    }

    public int getFragOffset(byte data[], int doff)
    {
        int foff = 0;
        foff |= (data[doff + 1] & 0xff) << 16;
        foff |= (data[doff + 2] & 0xff) << 8;
        foff |= data[doff + 3] & 0xff;
        return foff;
    }

    private JPEGFrame currentFrame;
    protected byte frameBuffer[];
    protected int sequenceNumber;
    protected int quality;
    protected int type;
    byte lastJFIFHeader[];
    int lastQuality;
    int lastType;
    int lastWidth;
    int lastHeight;
}
