// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   IMA4.java

package com.ibm.media.codec.audio.ima4;


// Referenced classes of package com.ibm.media.codec.audio.ima4:
//            IMA4State

public class IMA4
{

    public IMA4()
    {
    }

    static void encode(byte indata[], int inOffset, byte outdata[], int outOffset, int len, IMA4State state, int stride)
    {
        int outputbuffer = 0;
        int valpred = state.valprev;
        int index = state.index;
        int step = stepsizeTable[index];
        boolean bufferstep = true;
        for(; len > 0; len--)
        {
            int temp = indata[inOffset++] & 0xff;
            int val = indata[inOffset++] << 8 | temp;
            inOffset += stride;
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
                outputbuffer = delta;
            else
                outdata[outOffset++] = (byte)(delta << 4 | outputbuffer);
            bufferstep = !bufferstep;
        }

        if(bufferstep)
            outdata[outOffset++] = (byte)outputbuffer;
        state.valprev = valpred;
        state.index = index;
    }

    public static void decode(byte indata[], int inOffset, byte outdata[], int outOffset, int len, IMA4State state, int stride)
    {
        int inputbuffer = 0;
        boolean bufferstep = false;
        byte outp[] = outdata;
        byte inp[] = indata;
        int valpred = state.valprev;
        int index = state.index;
        int lastIndex = index;
        for(; len > 0; len--)
        {
            int delta;
            if(bufferstep)
            {
                delta = inputbuffer >> 4 & 0xf;
            } else
            {
                inputbuffer = inp[inOffset++];
                delta = inputbuffer & 0xf;
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
            outp[outOffset++] = (byte)(valpred >> 8);
            outp[outOffset++] = (byte)valpred;
            outOffset += stride;
        }

        state.valprev = valpred;
        state.index = index;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1998.";
    public static int indexTable[] = {
        -1, -1, -1, -1, 2, 4, 6, 8, -1, -1, 
        -1, -1, 2, 4, 6, 8
    };
    public static int stepsizeTable[] = {
        7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 
        19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 
        50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 
        130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 
        337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 
        876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 
        2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 
        5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 
        15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
    };
    public static int diffLUT[];

    static 
    {
        diffLUT = new int[1424];
        for(int delta = 0; delta < 16; delta++)
        {
            for(int lastIndex = 0; lastIndex <= 88; lastIndex++)
            {
                int sign = delta & 8;
                int step = stepsizeTable[lastIndex];
                int vpdiff = step >> 3;
                if((delta & 4) != 0)
                    vpdiff += step;
                if((delta & 2) != 0)
                    vpdiff += step >> 1;
                if((delta & 1) != 0)
                    vpdiff += step >> 2;
                if(sign != 0)
                    vpdiff = -vpdiff;
                diffLUT[(lastIndex << 4) + delta] = vpdiff;
            }

        }

    }
}
