// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaEncoder.java

package com.ibm.media.codec.audio.ima4;

import com.ibm.media.codec.audio.AudioCodec;
import com.ibm.media.codec.audio.BufferedEncoder;
import javax.media.Format;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.ima4:
//            IMA4State, IMA4

public class JavaEncoder extends BufferedEncoder
{

    public JavaEncoder()
    {
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", -1D, 16, -1, 0, 1, -1, -1D, Format.byteArray)
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("ima4")
        });
        super.PLUGIN_NAME = "IMA4 Encoder";
        super.historySize = 256;
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        super.supportedOutputFormats = (new AudioFormat[] {
            new AudioFormat("ima4", af.getSampleRate(), 16, af.getChannels(), -1, -1, 272 * af.getChannels(), -1D, Format.byteArray)
        });
        return super.supportedOutputFormats;
    }

    public void open()
    {
        ima4stateL = new IMA4State();
        ima4stateR = new IMA4State();
    }

    public void close()
    {
        ima4stateL = null;
        ima4stateR = null;
    }

    public void codecReset()
    {
        ima4stateL.index = 0;
        ima4stateL.valprev = 0;
        ima4stateR.index = 0;
        ima4stateR.valprev = 0;
    }

    protected int calculateOutputSize(int inputSize)
    {
        return calculateFramesNumber(inputSize) * 34 * 2;
    }

    protected int calculateFramesNumber(int inputSize)
    {
        return inputSize / 128;
    }

    protected boolean codecProcess(byte inpData[], int readPtr, byte outData[], int writePtr, int inpLength, int readBytes[], int writeBytes[], 
            int frameNumber[], int regions[], int regiostypes[])
    {
        int inCount = 0;
        int outCount = 0;
        int channels = super.inputFormat.getChannels();
        boolean isStereo = channels == 2;
        int stride = isStereo ? 2 : 0;
        int frames = inpLength / (channels * 128);
        regions[0] = writePtr;
        for(int frameCounter = 0; frameCounter < frames; frameCounter++)
        {
            if(ima4stateL.index > 88)
                ima4stateL.index = 88;
            else
            if(ima4stateL.index < 0)
                ima4stateL.index = 0;
            ima4stateL.valprev &= 0xffffff80;
            int stateL = ima4stateL.valprev | ima4stateL.index;
            outData[writePtr + outCount++] = (byte)(stateL >> 8);
            outData[writePtr + outCount++] = (byte)stateL;
            IMA4.encode(inpData, readPtr + inCount, outData, writePtr + outCount, 64, ima4stateL, stride);
            outCount += 32;
            if(isStereo)
            {
                if(ima4stateR.index > 88)
                    ima4stateR.index = 88;
                else
                if(ima4stateR.index < 0)
                    ima4stateR.index = 0;
                ima4stateR.valprev &= 0xffffff80;
                int stateR = ima4stateR.valprev | ima4stateR.index;
                outData[writePtr + outCount++] = (byte)(stateR >> 8);
                outData[writePtr + outCount++] = (byte)stateR;
                IMA4.encode(inpData, readPtr + inCount + 2, outData, writePtr + outCount, 64, ima4stateR, stride);
                outCount += 32;
                inCount += 256;
            } else
            {
                inCount += 128;
            }
            regions[frameCounter + 1] = outCount + writePtr;
            super.regionsTypes[frameCounter] = 0;
        }

        readBytes[0] = inCount;
        writeBytes[0] = outCount;
        frameNumber[0] = frames;
        return true;
    }

    private IMA4State ima4stateL;
    private IMA4State ima4stateR;
}
