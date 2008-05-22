// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GsmDecoder_ms.java

package com.ibm.media.codec.audio.gsm;


// Referenced classes of package com.ibm.media.codec.audio.gsm:
//            GsmDecoder

public class GsmDecoder_ms extends GsmDecoder
{

    public GsmDecoder_ms()
    {
    }

    public boolean decodeFrame(byte src[], int srcoffset, byte dst[], int dstoffset)
    {
        msFrameOdd = true;
        super.decodeFrame(src, srcoffset, dst, dstoffset);
        msFrameOdd = false;
        super.decodeFrame(src, srcoffset, dst, dstoffset + 320);
        return true;
    }

    protected boolean UnpackBitStream(byte inByteStream[], int inputIndex, int Parameters[])
    {
        int paramIndex = 0;
        if(msFrameOdd)
        {
            Parameters[paramIndex++] = inByteStream[inputIndex] & 0x3f;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 6 & 3 | (inByteStream[++inputIndex] & 0xf) << 2;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 4 & 0xf | (inByteStream[++inputIndex] & 1) << 4;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 0x1f;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 6 & 3 | (inByteStream[++inputIndex] & 3) << 2;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 2 & 0xf;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 6 & 3 | (inByteStream[++inputIndex] & 1) << 2;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 7;
            for(int n = 0; n < 4; n++)
            {
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 4 & 0xf | (inByteStream[++inputIndex] & 7) << 4;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 3 & 3;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 5 & 3;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 7 & 1 | (inByteStream[++inputIndex] & 0x1f) << 1;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 5 & 7;
                Parameters[paramIndex++] = inByteStream[++inputIndex] & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 3 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 6 & 3 | (inByteStream[++inputIndex] & 1) << 2;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 4 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 7 & 1 | (inByteStream[++inputIndex] & 3) << 1;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 2 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 5 & 7;
                Parameters[paramIndex++] = inByteStream[++inputIndex] & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 3 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 6 & 3 | (inByteStream[++inputIndex] & 1) << 2;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 7;
            }

        } else
        {
            inputIndex += 32;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 4 & 0xf | (inByteStream[++inputIndex] & 3) << 4;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 2 & 0x3f;
            Parameters[paramIndex++] = inByteStream[++inputIndex] & 0x1f;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 5 & 7 | (inByteStream[++inputIndex] & 3) << 3;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 2 & 0xf;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 6 & 3 | (inByteStream[++inputIndex] & 3) << 2;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 2 & 7;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 5 & 7;
            inputIndex++;
            for(int n = 0; n < 4; n++)
            {
                Parameters[paramIndex++] = inByteStream[inputIndex] & 0x7f;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 7 & 1 | (inByteStream[++inputIndex] & 1) << 1;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 3;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 3 & 0x1f | (inByteStream[++inputIndex] & 1) << 5;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 4 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 7 & 1 | (inByteStream[++inputIndex] & 3) << 1;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 2 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 5 & 7;
                Parameters[paramIndex++] = inByteStream[++inputIndex] & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 3 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 6 & 3 | (inByteStream[++inputIndex] & 1) << 2;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 4 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 7 & 1 | (inByteStream[++inputIndex] & 3) << 1;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 2 & 7;
                Parameters[paramIndex++] = inByteStream[inputIndex] >> 5 & 7;
                inputIndex++;
            }

        }
        return true;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1999.";
    private boolean msFrameOdd;
}
