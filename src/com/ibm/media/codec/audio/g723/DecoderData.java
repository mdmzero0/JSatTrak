// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   G723Dec.java

package com.ibm.media.codec.audio.g723;


class DecoderData
{

    DecoderData()
    {
        PrevLsp = new float[10];
        PrevExc = new float[1105];
        SyntIirDl = new float[10];
        PostFirDl = new float[10];
        PostIirDl = new float[10];
    }

    private static final int LpcOrder = 10;
    private static final int Frame = 240;
    private static final int NUM_OF_BLOCKS = 4;
    private static final int PitchMin = 18;
    private static final int PitchMax = 145;
    int WrkRate;
    boolean UsePf;
    int Ecount;
    float InterGain;
    int InterIndx;
    int Rseed;
    float Park;
    float Gain;
    float PrevLsp[];
    float PrevExc[];
    float SyntIirDl[];
    float PostFirDl[];
    float PostIirDl[];
    int BlockIndex;
    int BlockCount;
}
