// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaEncoder_ms.java

package com.ibm.media.codec.audio.ima4;

import com.ibm.media.codec.audio.AudioCodec;
import com.ibm.media.codec.audio.BufferedEncoder;
import com.sun.media.format.WavAudioFormat;
import javax.media.Format;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.ima4:
//            IMA4State, IMA4

public class JavaEncoder_ms extends BufferedEncoder
{

    public JavaEncoder_ms()
    {
        inputframeSizeInBytes = 1010;
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", -1D, 16, -1, 0, 1, -1, -1D, Format.byteArray)
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new WavAudioFormat("ima4/ms")
        });
        super.PLUGIN_NAME = "IMA4 MS Encoder";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        int outFrameSizeInBits = ((inputframeSizeInBytes - 2) * 2 + 32) * af.getChannels();
        int wSamplesPerBlock = inputframeSizeInBytes / 2;
        super.supportedOutputFormats = (new AudioFormat[] {
            new WavAudioFormat("ima4/ms", af.getSampleRate(), 4, af.getChannels(), outFrameSizeInBits, ((((int)af.getSampleRate() * 2) / 8) * outFrameSizeInBits) / inputframeSizeInBytes, -1, -1, -1F, Format.byteArray, new byte[] {
                (byte)(wSamplesPerBlock & 0xff), (byte)(wSamplesPerBlock >> 8)
            })
        });
        super.historySize = inputframeSizeInBytes * af.getChannels();
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
        return calculateFramesNumber(inputSize) * ((inputframeSizeInBytes - 4) * 4 + 2);
    }

    protected int calculateFramesNumber(int inputSize)
    {
        return inputSize / inputframeSizeInBytes;
    }

    protected boolean codecProcess(byte inpData[], int readPtr, byte outData[], int writePtr, int inpLength, int readBytes[], int writeBytes[], 
            int frameNumber[], int regions[], int regiostypes[])
    {
        int inCount = 0;
        int outCount = 0;
        int channels = super.inputFormat.getChannels();
        boolean isStereo = channels == 2;
        int stride = isStereo ? 2 : 0;
        int frames = inpLength / (channels * 1010);
        int iterations = inputframeSizeInBytes - 2 >> 1;
        regions[0] = writePtr;
        for(int frameCounter = 0; frameCounter < frames; frameCounter++)
        {
            int valprev = inpData[readPtr + inCount++] & 0xff;
            valprev |= inpData[readPtr + inCount++] << 8;
            ima4stateL.valprev = valprev;
            if(ima4stateL.index > 88)
                ima4stateL.index = 88;
            else
            if(ima4stateL.index < 0)
                ima4stateL.index = 0;
            outData[writePtr + outCount++] = (byte)valprev;
            outData[writePtr + outCount++] = (byte)(valprev >> 8);
            outData[writePtr + outCount++] = (byte)ima4stateL.index;
            outCount++;
            if(isStereo)
            {
                valprev = inpData[readPtr + inCount++] & 0xff;
                valprev |= inpData[readPtr + inCount++] << 8;
                ima4stateR.valprev = valprev;
                if(ima4stateR.index > 88)
                    ima4stateR.index = 88;
                else
                if(ima4stateR.index < 0)
                    ima4stateR.index = 0;
                outData[writePtr + outCount++] = (byte)valprev;
                outData[writePtr + outCount++] = (byte)(valprev >> 8);
                outData[writePtr + outCount++] = (byte)ima4stateR.index;
                outCount++;
            }
            for(int loop = 0; loop < iterations / 8; loop++)
            {
                IMA4.encode(inpData, inCount + readPtr, outData, outCount + writePtr, 8, ima4stateL, stride);
                outCount += 4;
                if(isStereo)
                {
                    IMA4.encode(inpData, inCount + readPtr + 2, outData, outCount + writePtr, 8, ima4stateR, stride);
                    outCount += 4;
                    inCount += 32;
                } else
                {
                    inCount += 16;
                }
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
    private int inputframeSizeInBytes;
}
