// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MsAdpcm.java

package com.sun.media.codec.audio.msadpcm;

import java.io.PrintStream;

// Referenced classes of package com.sun.media.codec.audio.msadpcm:
//            MsAdpcmState

public class MsAdpcm
{

    public MsAdpcm()
    {
    }

    protected static void decodeBlock(byte indata[], int inOffset, byte outdata[], int outOffset, int samplesInBlock, MsAdpcmState state[], int channels)
    {
        for(int i = 0; i < channels; i++)
        {
            state[i].bpred = indata[inOffset++] & 0xff;
            if(state[i].bpred >= 7)
            {
                state[i].bpred = 0;
                System.err.println("[MSADPCM] Illegal predictor value");
            }
        }

        for(int i = 0; i < channels; i++)
        {
            state[i].index = indata[inOffset++] & 0xff;
            state[i].index |= indata[inOffset++] << 8;
        }

        for(int i = 0; i < channels; i++)
        {
            state[i].sample1 = indata[inOffset++] & 0xff;
            state[i].sample1 |= indata[inOffset++] << 8;
        }

        for(int i = 0; i < channels; i++)
        {
            state[i].sample2 = indata[inOffset++] & 0xff;
            state[i].sample2 |= indata[inOffset++] << 8;
        }

        for(int i = 0; i < channels; i++)
        {
            outdata[outOffset++] = (byte)state[i].sample2;
            outdata[outOffset++] = (byte)(state[i].sample2 >> 8);
        }

        for(int i = 0; i < channels; i++)
        {
            outdata[outOffset++] = (byte)state[i].sample1;
            outdata[outOffset++] = (byte)(state[i].sample1 >> 8);
        }

        for(int loop = samplesInBlock; loop > 0; loop--)
        {
            int b = indata[inOffset++];
            int delta = b >> 4 & 0xf;
            MsAdpcmState localState = state[0];
            int localIndex = localState.index;
            int samplePredictor = localState.sample1 * pred1Table[localState.bpred] + localState.sample2 * pred2Table[localState.bpred] >> 8;
            int signedDelta = (delta << 28) >> 28;
            int sample = signedDelta * localIndex + samplePredictor;
            if(sample > 32767)
                sample = 32767;
            else
            if(sample < -32768)
                sample = -32768;
            localIndex = gainTable[delta] * localIndex >> 8;
            if(localIndex < 16)
                localIndex = 16;
            localState.sample2 = localState.sample1;
            localState.sample1 = sample;
            localState.index = localIndex;
            outdata[outOffset++] = (byte)sample;
            outdata[outOffset++] = (byte)(sample >> 8);
            delta = b & 0xf;
            localState = state[channels != 1 ? 1 : 0];
            localIndex = localState.index;
            samplePredictor = localState.sample1 * pred1Table[localState.bpred] + localState.sample2 * pred2Table[localState.bpred] >> 8;
            signedDelta = (delta << 28) >> 28;
            sample = signedDelta * localIndex + samplePredictor;
            if(sample > 32767)
                sample = 32767;
            else
            if(sample < -32768)
                sample = -32768;
            localIndex = gainTable[delta] * localIndex >> 8;
            if(localIndex < 16)
                localIndex = 16;
            localState.sample2 = localState.sample1;
            localState.sample1 = sample;
            localState.index = localIndex;
            outdata[outOffset++] = (byte)sample;
            outdata[outOffset++] = (byte)(sample >> 8);
        }

    }

    private static int gainTable[] = {
        230, 230, 230, 230, 307, 409, 512, 614, 768, 614, 
        512, 409, 307, 230, 230, 230
    };
    private static int pred1Table[] = {
        256, 512, 0, 192, 240, 460, 392
    };
    private static int pred2Table[] = {
        0, -256, 0, 64, 0, -208, -232
    };

}
