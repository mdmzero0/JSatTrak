// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DVI.java

package com.ibm.media.codec.audio.dvi;

import com.ibm.media.codec.audio.ima4.IMA4;

// Referenced classes of package com.ibm.media.codec.audio.dvi:
//            DVIState

public class DVI
{

    public DVI()
    {
    }

    static void encode(byte indata[], int inOffset, byte outdata[], int outOffset, int len, DVIState state)
    {
        int outputbuffer = 0;
        int indexTable[] = IMA4.indexTable;
        int stepsizeTable[] = IMA4.stepsizeTable;
        int valpred = state.valprev;
        int index = state.index;
        int step = stepsizeTable[index];
        boolean bufferstep = false;
        for(; len > 0; len--)
        {
            int temp = indata[inOffset++] & 0xff;
            int val = indata[inOffset++] << 8 | temp;
            int diff = val - valpred;
            int sign;
            if(diff < 0)
            {
                sign = 8;
                diff = -diff;
            } else
            {
                sign = 0;
            }
            int delta = 0;
            int vpdiff = step >> 3;
            if(diff >= step)
            {
                delta = 4;
                diff -= step;
                vpdiff += step;
            }
            step >>= 1;
            if(diff >= step)
            {
                delta |= 2;
                diff -= step;
                vpdiff += step;
            }
            step >>= 1;
            if(diff >= step)
            {
                delta |= 1;
                vpdiff += step;
            }
            if(sign != 0)
                valpred -= vpdiff;
            else
                valpred += vpdiff;
            if(valpred > 32767)
                valpred = 32767;
            else
            if(valpred < -32768)
                valpred = -32768;
            delta |= sign;
            index += indexTable[delta];
            if(index < 0)
                index = 0;
            else
            if(index > 88)
                index = 88;
            step = stepsizeTable[index];
            if(bufferstep)
                outputbuffer = delta << 4;
            else
                outdata[outOffset++] = (byte)(delta | outputbuffer);
            bufferstep = !bufferstep;
        }

        if(bufferstep)
            outdata[outOffset++] = (byte)outputbuffer;
        state.valprev = valpred;
        state.index = index;
    }

    public static void decode(byte indata[], int inOffset, byte outdata[], int outOffset, int len, DVIState state)
    {
        int inputbuffer = 0;
        boolean bufferstep = false;
        byte outp[] = outdata;
        byte inp[] = indata;
        int valpred = state.valprev;
        int index = state.index;
        int lastIndex = index;
        int indexTable[] = IMA4.indexTable;
        int diffLUT[] = IMA4.diffLUT;
        for(; len > 0; len--)
        {
            int delta;
            if(bufferstep)
            {
                delta = inputbuffer & 0xf;
            } else
            {
                inputbuffer = inp[inOffset++];
                delta = inputbuffer >> 4 & 0xf;
            }
            bufferstep = !bufferstep;
            index += indexTable[delta];
            if(index < 0)
                index = 0;
            else
            if(index > 88)
                index = 88;
            valpred += diffLUT[(lastIndex << 4) + delta];
            if(valpred > 32767)
                valpred = 32767;
            else
            if(valpred < -32768)
                valpred = -32768;
            lastIndex = index;
            outp[outOffset++] = (byte)valpred;
            outp[outOffset++] = (byte)(valpred >> 8);
        }

        state.valprev = valpred;
        state.index = index;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1998.";
}
