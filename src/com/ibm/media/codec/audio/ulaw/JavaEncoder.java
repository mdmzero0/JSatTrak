// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaEncoder.java

package com.ibm.media.codec.audio.ulaw;

import com.ibm.media.codec.audio.AudioCodec;
import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import javax.media.*;
import javax.media.format.AudioFormat;

public class JavaEncoder extends AudioCodec
{

    public JavaEncoder()
    {
        lastFormat = null;
        numberOfOutputChannels = 1;
        downmix = false;
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", -1D, 16, 1, -1, -1), new AudioFormat("LINEAR", -1D, 16, 2, -1, -1), new AudioFormat("LINEAR", -1D, 8, 1, -1, -1), new AudioFormat("LINEAR", -1D, 8, 2, -1, -1)
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("ULAW", 8000D, 8, 1, -1, -1)
        });
        super.PLUGIN_NAME = "pcm to mu-law converter";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat inFormat = (AudioFormat)in;
        int channels = inFormat.getChannels();
        int sampleRate = (int)inFormat.getSampleRate();
        if(channels == 2)
            super.supportedOutputFormats = (new AudioFormat[] {
                new AudioFormat("ULAW", sampleRate, 8, 2, -1, -1), new AudioFormat("ULAW", sampleRate, 8, 1, -1, -1)
            });
        else
            super.supportedOutputFormats = (new AudioFormat[] {
                new AudioFormat("ULAW", sampleRate, 8, 1, -1, -1)
            });
        return super.supportedOutputFormats;
    }

    public void open()
        throws ResourceUnavailableException
    {
    }

    public void close()
    {
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(!checkInputBuffer(inputBuffer))
            return 1;
        if(isEOM(inputBuffer))
        {
            propagateEOM(outputBuffer);
            return 0;
        }
        Format newFormat = inputBuffer.getFormat();
        if(lastFormat != newFormat)
            initConverter((AudioFormat)newFormat);
        int inpLength = inputBuffer.getLength();
        int outLength = calculateOutputSize(inputBuffer.getLength());
        byte inpData[] = (byte[])inputBuffer.getData();
        byte outData[] = validateByteArraySize(outputBuffer, outLength);
        convert(inpData, inputBuffer.getOffset(), inpLength, outData, 0);
        updateOutput(outputBuffer, super.outputFormat, outLength, 0);
        return 0;
    }

    private int calculateOutputSize(int inputLength)
    {
        if(inputSampleSize == 16)
            inputLength /= 2;
        if(downmix)
            inputLength /= 2;
        return inputLength;
    }

    private void initConverter(AudioFormat inFormat)
    {
        lastFormat = inFormat;
        numberOfInputChannels = inFormat.getChannels();
        if(super.outputFormat != null)
            numberOfOutputChannels = super.outputFormat.getChannels();
        inputSampleSize = inFormat.getSampleSizeInBits();
        if(inFormat.getEndian() == 1 || 8 == inputSampleSize)
        {
            lsbOffset = 1;
            msbOffset = 0;
        } else
        {
            lsbOffset = -1;
            msbOffset = 1;
        }
        if(inFormat.getSigned() == 1)
        {
            inputBias = 0;
            signMask = -1;
        } else
        {
            inputBias = 32768;
            signMask = 65535;
        }
        if(numberOfInputChannels == 2 && numberOfOutputChannels == 1)
            downmix = true;
        else
            downmix = false;
    }

    private void convert(byte input[], int inputOffset, int inputLength, byte outData[], int outputOffset)
    {
        for(int i = inputOffset + msbOffset; i < inputLength + inputOffset;)
        {
            int inputSample;
            if(8 == inputSampleSize)
            {
                inputSample = input[i++] << 8;
                if(downmix)
                    inputSample = (inputSample & signMask) + (input[i++] << 8 & signMask) >> 1;
            } else
            {
                inputSample = (input[i] << 8) + (0xff & input[i + lsbOffset]);
                i += 2;
                if(downmix)
                {
                    inputSample = (inputSample & signMask) + ((input[i] << 8) + (0xff & input[i + lsbOffset]) & signMask) >> 1;
                    i += 2;
                }
            }
            int sample = (short)(inputSample + inputBias);
            int signBit;
            if(sample >= 0)
            {
                signBit = 128;
            } else
            {
                sample = -sample;
                signBit = 0;
            }
            sample = 132 + sample >> 3;
            outData[outputOffset++] = sample >= 32 ? sample >= 64 ? sample >= 128 ? sample >= 256 ? sample >= 512 ? sample >= 1024 ? sample >= 2048 ? sample >= 4096 ? (byte)(signBit | 0 | 0) : (byte)(signBit | 0 | 31 - (sample >> 7)) : (byte)(signBit | 0x10 | 31 - (sample >> 6)) : (byte)(signBit | 0x20 | 31 - (sample >> 5)) : (byte)(signBit | 0x30 | 31 - (sample >> 4)) : (byte)(signBit | 0x40 | 31 - (sample >> 3)) : (byte)(signBit | 0x50 | 31 - (sample >> 2)) : (byte)(signBit | 0x60 | 31 - (sample >> 1)) : (byte)(signBit | 0x70 | 31 - (sample >> 0));
        }

    }

    private Format lastFormat;
    private int numberOfInputChannels;
    private int numberOfOutputChannels;
    private boolean downmix;
    private int inputSampleSize;
    private int lsbOffset;
    private int msbOffset;
    private int inputBias;
    private int signMask;
}
