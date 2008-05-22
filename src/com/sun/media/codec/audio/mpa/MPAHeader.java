// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MPAHeader.java

package com.sun.media.codec.audio.mpa;


public class MPAHeader
{

    public MPAHeader()
    {
    }

    public String toString()
    {
        String s = "\n   Layer = " + layer + "\n" + "   HeaderOffset = " + headerOffset + "\n" + "   BitsInFrame = " + bitsInFrame + "\n" + "   BitRate = " + bitRate + "\n" + "   SamplingRate = " + samplingRate + "\n" + "   Channels = " + nChannels + "\n" + "   Samples = " + nSamples + "\n";
        return s;
    }

    public int layer;
    public int headerOffset;
    public int bitsInFrame;
    public int bitRate;
    public int samplingRate;
    public int nChannels;
    public int nSamples;
    public int negOffset;
}
