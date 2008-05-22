// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GsmDecoder.java

package com.ibm.media.codec.audio.gsm;


public class GsmDecoder
{

    public GsmDecoder()
    {
        outsig = new short[160];
        inByteStream = new byte[33];
        prevLARpp = new float[9];
        rp = new float[9];
        u = new float[9];
        lastSID = new int[76];
        quantRes = new float[280];
        parameters = new int[76];
        LARpp = new float[9];
    }

    private int GSMrand()
    {
        seed = seed * 0x41c64e6d + 12345;
        return seed & 0x7fff;
    }

    public void decoderInit()
    {
        prevNc = 40;
        prevOut = 0.0F;
        for(int i = 0; i < 9; i++)
        {
            prevLARpp[i] = 0.0F;
            rp[i] = 0.0F;
            u[i] = 0.0F;
        }

        for(int i = 0; i < lastSID.length; i++)
            lastSID[i] = 0;

        lastSID[0] = 2;
        lastSID[1] = 28;
        lastSID[2] = 18;
        lastSID[3] = 12;
        lastSID[4] = 7;
        lastSID[5] = 5;
        lastSID[6] = 3;
        lastSID[7] = 2;
        for(int i = 0; i < quantRes.length; i++)
            quantRes[i] = 0.0F;

        seed = 1;
    }

    public boolean decodeFrame(byte src[], int srcoffset, byte dst[], int dstoffset)
    {
        int parameters[] = this.parameters;
        int lastSID[] = this.lastSID;
        float LARpp[] = this.LARpp;
        float u[] = this.u;
        float rp[] = this.rp;
        float prevLARpp[] = this.prevLARpp;
        float quantRes[] = this.quantRes;
        System.arraycopy(quantRes, 160, quantRes, 0, 120);
        if(!UnpackBitStream(src, srcoffset, parameters))
            return false;
        int frameType = 0;
        for(int i = 0; i < 76; i++)
        {
            if(0 == parameters[i])
                continue;
            frameType = 2;
            break;
        }

        if(frameType == 0)
        {
            System.arraycopy(lastSID, 0, parameters, 0, 76);
            frameType = 2;
        } else
        {
            for(int subFrameNumber = 0; subFrameNumber < 4; subFrameNumber++)
            {
                int subFramePulseBase = subFrameNumber * 17 + 8 + 4;
                for(int j = 0; j < 13; j++)
                {
                    if(parameters[subFramePulseBase + j] == 0)
                        continue;
                    frameType = 1;
                    subFrameNumber = 4;
                    break;
                }

            }

            if(frameType == 2)
                System.arraycopy(parameters, 0, lastSID, 0, 76);
        }
        if(frameType == 2)
        {
            for(int subFrameNumber = 0; subFrameNumber < 4; subFrameNumber++)
            {
                int subFrameParamBase = subFrameNumber * 17 + 8;
                for(int j = 0; j < 13; j++)
                    parameters[subFrameParamBase + 4 + j] = GSMrand() / 5461 + 1;

                parameters[subFrameParamBase + 2] = GSMrand() / 10923;
                parameters[subFrameParamBase + 1] = 0;
                parameters[subFrameParamBase + 0] = (subFrameNumber == 0) | (subFrameNumber == 2) ? 40 : 120;
            }

        }
        for(int subFrameNumber = 0; subFrameNumber < 4; subFrameNumber++)
        {
            int subFrameParamBase = subFrameNumber * 17 + 8;
            int tempLtpLag = parameters[subFrameParamBase + 0];
            if(tempLtpLag >= 40 && tempLtpLag <= 120)
                prevNc = tempLtpLag;
            float ltpGain = QLB[parameters[subFrameParamBase + 1]];
            int rpeGridPos = parameters[subFrameParamBase + 2];
            float xmaxp = xmaxTable[parameters[subFrameParamBase + 3]];
            int subFrameResidualBase = subFrameNumber * 40 + 120;
            for(int i = 0; i < 40; i++)
                quantRes[subFrameResidualBase + i] = ltpGain * quantRes[(subFrameResidualBase + i) - prevNc];

            for(int i = 0; i < 13; i++)
                quantRes[subFrameResidualBase + rpeGridPos + 3 * i] += (0.25D * (double)parameters[subFrameParamBase + 4 + i] - 0.875D) * (double)xmaxp;

        }

        for(int larNum = 0; larNum < 8; larNum++)
            LARpp[larNum + 1] = larTable[larNum][parameters[larNum]];

        float prevOut = this.prevOut;
        for(int larInterpNumber = 0; larInterpNumber < 4; larInterpNumber++)
        {
            for(int i = 1; i <= 8; i++)
            {
                float LARpi = prevLARpp[i] * InterpLarCoef[larInterpNumber][0] + LARpp[i] * InterpLarCoef[larInterpNumber][1];
                if((double)Math.abs(LARpi) < 0.67500000000000004D)
                    rp[i] = LARpi;
                else
                if((double)Math.abs(LARpi) < 1.2250000000000001D)
                    rp[i] = (LARpi <= 0.0F ? -1F : 1.0F) * (0.5F * Math.abs(LARpi) + 0.3375F);
                else
                    rp[i] = (LARpi <= 0.0F ? -1F : 1.0F) * (0.125F * Math.abs(LARpi) + 0.796875F);
            }

            for(int outCount = larInterpStart[larInterpNumber]; outCount < larInterpStart[larInterpNumber + 1]; outCount++)
            {
                float temp = quantRes[120 + outCount];
                temp -= rp[8] * u[7];
                u[8] = u[7] + rp[8] * temp;
                temp -= rp[7] * u[6];
                u[7] = u[6] + rp[7] * temp;
                temp -= rp[6] * u[5];
                u[6] = u[5] + rp[6] * temp;
                temp -= rp[5] * u[4];
                u[5] = u[4] + rp[5] * temp;
                temp -= rp[4] * u[3];
                u[4] = u[3] + rp[4] * temp;
                temp -= rp[3] * u[2];
                u[3] = u[2] + rp[3] * temp;
                temp -= rp[2] * u[1];
                u[2] = u[1] + rp[2] * temp;
                temp -= rp[1] * u[0];
                u[1] = u[0] + rp[1] * temp;
                prevOut = temp + prevOut * 0.8599854F;
                u[0] = temp;
                temp = 65532F * prevOut;
                if(temp > 32766F)
                    temp = 32766F;
                if(temp < -32766F)
                    temp = -32766F;
                outsig[outCount] = (short)(int)temp;
            }

        }

        for(int i = 1; i <= 8; i++)
            prevLARpp[i] = LARpp[i];

        this.prevOut = prevOut;
        int dstIndex = 0;
        for(int i = 0; i < 160; i++)
        {
            int TempInt = outsig[i];
            dst[dstoffset + dstIndex++] = (byte)(TempInt & 0xff);
            dst[dstoffset + dstIndex++] = (byte)(TempInt >> 8);
        }

        return true;
    }

    protected boolean UnpackBitStream(byte inByteStream[], int inputIndex, int Parameters[])
    {
        int paramIndex = 0;
        if((inByteStream[inputIndex] >> 4 & 0xf) != 13)
            return false;
        Parameters[paramIndex++] = (inByteStream[inputIndex] & 0xf) << 2 | inByteStream[++inputIndex] >> 6 & 3;
        Parameters[paramIndex++] = inByteStream[inputIndex] & 0x3f;
        Parameters[paramIndex++] = inByteStream[++inputIndex] >> 3 & 0x1f;
        Parameters[paramIndex++] = (inByteStream[inputIndex] & 7) << 2 | inByteStream[++inputIndex] >> 6 & 3;
        Parameters[paramIndex++] = inByteStream[inputIndex] >> 2 & 0xf;
        Parameters[paramIndex++] = (inByteStream[inputIndex] & 3) << 2 | inByteStream[++inputIndex] >> 6 & 3;
        Parameters[paramIndex++] = inByteStream[inputIndex] >> 3 & 7;
        Parameters[paramIndex++] = inByteStream[inputIndex] & 7;
        inputIndex++;
        for(int n = 0; n < 4; n++)
        {
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 0x7f;
            Parameters[paramIndex++] = (inByteStream[inputIndex] & 1) << 1 | inByteStream[++inputIndex] >> 7 & 1;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 5 & 3;
            Parameters[paramIndex++] = (inByteStream[inputIndex] & 0x1f) << 1 | inByteStream[++inputIndex] >> 7 & 1;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 4 & 7;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 7;
            Parameters[paramIndex++] = (inByteStream[inputIndex] & 1) << 2 | inByteStream[++inputIndex] >> 6 & 3;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 3 & 7;
            Parameters[paramIndex++] = inByteStream[inputIndex] & 7;
            Parameters[paramIndex++] = inByteStream[++inputIndex] >> 5 & 7;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 2 & 7;
            Parameters[paramIndex++] = (inByteStream[inputIndex] & 3) << 1 | inByteStream[++inputIndex] >> 7 & 1;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 4 & 7;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 1 & 7;
            Parameters[paramIndex++] = (inByteStream[inputIndex] & 1) << 2 | inByteStream[++inputIndex] >> 6 & 3;
            Parameters[paramIndex++] = inByteStream[inputIndex] >> 3 & 7;
            Parameters[paramIndex++] = inByteStream[inputIndex] & 7;
            inputIndex++;
        }

        return true;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1999.";
    private static final int OutputSize = 160;
    private static final int InputSize = 33;
    private static final int LpcOrder = 8;
    private static final int SubFrameSize = 40;
    private static final int NumOfSubframes = 4;
    private static final int NumOfParameters = 76;
    private static final int NumOfPulses = 13;
    private static final int NumOfLarInterp = 4;
    private static final int QuantResBase = 120;
    private static final int SidFrame = 2;
    private static final int SpeechFrame = 1;
    private static final int NullFrame = 0;
    private static final float QLB[] = {
        0.1F, 0.35F, 0.65F, 1.0F
    };
    private static final int larInterpStart[] = {
        0, 13, 27, 40, 160
    };
    private static final float InterpLarCoef[][] = {
        {
            0.75F, 0.25F
        }, {
            0.5F, 0.5F
        }, {
            0.25F, 0.75F
        }, {
            0.0F, 1.0F
        }
    };
    private static float xmaxTable[];
    private static float larTable[][];
    private static final int Parameter_SubFramesBase = 8;
    private static final int Parameter_SubFramesLength = 17;
    private static final int Parameter_LtpLag = 0;
    private static final int Parameter_LtpGain = 1;
    private static final int Parameter_RpeGridPosition = 2;
    private static final int Parameter_BlockAmplitude = 3;
    private static final int Parameter_RpePulsesBase = 4;
    private short outsig[];
    private byte inByteStream[];
    private float prevLARpp[];
    private float rp[];
    private float u[];
    private int lastSID[];
    private int prevNc;
    private float prevOut;
    private float quantRes[];
    private int seed;
    private int parameters[];
    private float LARpp[];
    private static final int GSM_MAGIC = 13;

    static 
    {
        short B[] = {
            0, 0, 2048, -2560, 94, -1792, -341, -1144
        };
        short MIC[] = {
            -32, -32, -16, -16, -8, -8, -4, -4
        };
        short INVA[] = {
            13107, 13107, 13107, 13107, 19223, 17476, 31454, 29708
        };
        xmaxTable = new float[64];
        for(int xmaxc = 0; xmaxc < 64; xmaxc++)
        {
            int xmaxp;
            if(xmaxc < 16)
            {
                xmaxp = 31 + (xmaxc << 5);
            } else
            {
                int exp = xmaxc - 16 >> 3;
                xmaxp = ((576 << exp) - 1) + (xmaxc - 16 - 8 * exp) * (64 << exp);
            }
            xmaxTable[xmaxc] = (float)xmaxp / 32768F;
        }

        larTable = new float[8][];
        for(int larNum = 0; larNum < 8; larNum++)
        {
            larTable[larNum] = new float[-MIC[larNum] * 2];
            for(int larQuant = 0; larQuant < -MIC[larNum] * 2; larQuant++)
            {
                short temp = (short)((larQuant + MIC[larNum] << 10) - B[larNum] * 2);
                temp = (short)(int)((long)(temp * INVA[larNum]) + 16384L >> 15);
                larTable[larNum][larQuant] = (float)(temp * 2) / 16384F;
            }

        }

    }
}
