// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReadStream.java

package com.ibm.media.codec.video.h263;


public class ReadStream
{

    public ReadStream()
    {
        incnt = 0;
        lBufferHistory = 0L;
    }

    public void initBitstream()
    {
        incnt = 0;
    }

    public void setInBuf(byte ds_rdbfr[], int ds_rdbfr_offset)
    {
        rdbfr = ds_rdbfr;
        rdptr = ds_rdbfr_offset;
    }

    public int getInBufOffset()
    {
        return rdptr;
    }

    protected final int nextBits(int n)
    {
        if(incnt < n)
            fillBuffer();
        return (int)((lBufferHistory << 64 - incnt) >>> 64 - n);
    }

    protected final void skipBits(int n)
    {
        incnt -= n;
        if(incnt < 0)
            fillBuffer();
    }

    protected final int getBits(int n)
    {
        int bits = nextBits(n);
        incnt -= n;
        return bits;
    }

    private final void fillBuffer()
    {
        lBufferHistory <<= 32;
        int newBits = rdbfr[rdptr++] << 24 | (rdbfr[rdptr++] & 0xff) << 16 | (rdbfr[rdptr++] & 0xff) << 8 | rdbfr[rdptr++] & 0xff;
        lBufferHistory |= (long)newBits & 0xffffffffL;
        incnt += 32;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997, 1998.";
    protected int rdptr;
    private byte rdbfr[];
    private int incnt;
    long lBufferHistory;
}
