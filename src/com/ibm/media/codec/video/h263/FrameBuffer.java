// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FrameBuffer.java

package com.ibm.media.codec.video.h263;


public class FrameBuffer
{

    public FrameBuffer(int Fwidth, int Fheight)
    {
        width = Fwidth;
        height = Fheight;
        Y = new int[Fheight * Fwidth];
        Cr = new int[(Fheight >> 1) * (Fwidth >> 1)];
        Cb = new int[(Fheight >> 1) * (Fwidth >> 1)];
        for(int i = 0; i < width * height >> 2; i++)
        {
            Cb[i] = 128;
            Cr[i] = 128;
        }

        for(int i = 0; i < width * height; i++)
            Y[i] = 128;

    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997, 1998.";
    public int Y[];
    public int Cr[];
    public int Cb[];
    public int width;
    public int height;
}
