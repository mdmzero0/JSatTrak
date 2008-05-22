// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CSeqHeader.java

package com.sun.media.rtsp.protocol;


public class CSeqHeader
{

    public CSeqHeader(String number)
    {
        sequence_number = number;
    }

    public String getSequenceNumber()
    {
        return sequence_number;
    }

    private String sequence_number;
}
