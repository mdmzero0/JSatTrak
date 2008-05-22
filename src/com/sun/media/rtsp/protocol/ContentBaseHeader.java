// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ContentBaseHeader.java

package com.sun.media.rtsp.protocol;


public class ContentBaseHeader
{

    public ContentBaseHeader(String contentBase)
    {
        this.contentBase = contentBase;
    }

    public String getContentBase()
    {
        return contentBase;
    }

    private String contentBase;
}
