// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RangeHeader.java

package com.sun.media.rtsp.protocol;


public class RangeHeader
{

    public RangeHeader(String str)
    {
        int start = str.indexOf('=') + 1;
        int end = str.indexOf('-');
        String startPosStr = str.substring(start, end);
        startPos = (new Long(startPosStr)).longValue();
    }

    public long getStartPos()
    {
        return startPos;
    }

    private long startPos;
}
