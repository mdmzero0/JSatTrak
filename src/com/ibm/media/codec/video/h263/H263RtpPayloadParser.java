// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   H263Decoder.java

package com.ibm.media.codec.video.h263;


final class H263RtpPayloadParser
{

    H263RtpPayloadParser()
    {
    }

    static int getMode(byte payload[], int offset)
    {
        int mode;
        if((payload[offset] & 0x80) == 0)
            mode = 0;
        else
        if((payload[offset] & 0x40) == 0)
            mode = 1;
        else
            mode = 2;
        return mode;
    }

    static int getStartBit(byte payload[], int offset)
    {
        int startBit = (payload[offset] & 0x38) >> 3;
        return startBit;
    }

    static int getEndBit(byte payload[], int offset)
    {
        int endBit = payload[offset] & 7;
        return endBit;
    }

    static int getSRC(byte payload[], int offset)
    {
        return (payload[offset + 1] & 0xe0) >> 5;
    }

    static int getTemporalReference(byte payload[], int offset)
    {
        int tr = payload[offset + 3] & 0xff;
        return tr;
    }

    private static final boolean DEBUG = false;
}
