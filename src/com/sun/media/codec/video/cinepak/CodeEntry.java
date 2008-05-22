// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CodeEntry.java

package com.sun.media.codec.video.cinepak;


public class CodeEntry
{

    public CodeEntry(CodeEntry fromCode)
    {
        aRGB0 = fromCode.aRGB0;
        aRGB1 = fromCode.aRGB1;
        aRGB2 = fromCode.aRGB2;
        aRGB3 = fromCode.aRGB3;
    }

    public CodeEntry()
    {
        aRGB0 = 0xa3a3a3;
        aRGB1 = 0xa3a3a3;
        aRGB2 = 0xa3a3a3;
        aRGB3 = 0xa3a3a3;
    }

    int aRGB0;
    int aRGB1;
    int aRGB2;
    int aRGB3;
}
