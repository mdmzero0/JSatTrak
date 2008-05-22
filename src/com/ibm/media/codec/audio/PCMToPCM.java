// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PCMToPCM.java

package com.ibm.media.codec.audio;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio:
//            AudioCodec

public class PCMToPCM extends AudioCodec
{

    public PCMToPCM()
    {
        lastInputFormat = null;
        lastOutputFormat = null;
        bias = 0;
        signMask = 0;
        inputSampleSize = 8;
        outputSampleSize = 8;
        numberOfInputChannels = 1;
        numberOfOutputChannels = 1;
        channels2To1 = false;
        channels1To2 = false;
        channels2To2 = false;
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", -1D, 16, 1, -1, -1), new AudioFormat("LINEAR", -1D, 16, 2, -1, -1), new AudioFormat("LINEAR", -1D, 8, 1, -1, -1), new AudioFormat("LINEAR", -1D, 8, 2, -1, -1)
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR")
        });
        super.PLUGIN_NAME = "PCM to PCM converter";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        int otherChnl = af.getChannels() != 1 ? 1 : 2;
        super.supportedOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", af.getSampleRate(), 16, af.getChannels(), 0, 1, 16 * af.getChannels(), af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 16, otherChnl, 0, 1, 16 * otherChnl, af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 16, af.getChannels(), 1, 1, 16 * af.getChannels(), af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 16, otherChnl, 1, 1, 16 * otherChnl, af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 16, af.getChannels(), 0, 0, 16 * af.getChannels(), af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 16, otherChnl, 0, 0, 16 * otherChnl, af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 16, af.getChannels(), 1, 0, 16 * af.getChannels(), af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 16, otherChnl, 1, 0, 16 * otherChnl, af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 8, af.getChannels(), -1, 1, 8 * af.getChannels(), af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 8, otherChnl, -1, 1, 8 * otherChnl, af.getFrameRate(), af.getDataType()), 
            new AudioFormat("LINEAR", af.getSampleRate(), 8, af.getChannels(), -1, 0, 8 * af.getChannels(), af.getFrameRate(), af.getDataType()), new AudioFormat("LINEAR", af.getSampleRate(), 8, otherChnl, -1, 0, 8 * otherChnl, af.getFrameRate(), af.getDataType())
        });
        return super.supportedOutputFormats;
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
        if(lastInputFormat != super.inputFormat || lastOutputFormat != super.outputFormat)
            initConverter(super.inputFormat, super.outputFormat);
        int inpLength = inputBuffer.getLength();
        int outLength = calculateOutputSize(inputBuffer.getLength());
        byte inpData[] = (byte[])inputBuffer.getData();
        byte outData[] = validateByteArraySize(outputBuffer, outLength);
        convert(inpData, inputBuffer.getOffset(), inpLength, outData, outputBuffer.getOffset());
        updateOutput(outputBuffer, super.outputFormat, outLength, outputBuffer.getOffset());
        return 0;
    }

    private int calculateOutputSize(int inputLength)
    {
        int outputLength = inputLength;
        if(inputSampleSize == 8 && outputSampleSize == 16)
            outputLength *= 2;
        if(inputSampleSize == 16 && outputSampleSize == 8)
            outputLength /= 2;
        if(numberOfInputChannels == 1 && numberOfOutputChannels == 2)
            outputLength *= 2;
        if(numberOfInputChannels == 2 && numberOfOutputChannels == 1)
            outputLength /= 2;
        return outputLength;
    }

    private void initConverter(AudioFormat inFormat, AudioFormat outFormat)
    {
        lastInputFormat = inFormat;
        lastOutputFormat = outFormat;
        numberOfInputChannels = inFormat.getChannels();
        numberOfOutputChannels = outFormat.getChannels();
        inputSampleSize = inFormat.getSampleSizeInBits();
        outputSampleSize = outFormat.getSampleSizeInBits();
        if(inFormat.getEndian() == 1 || 8 == inputSampleSize)
        {
            inputLsbOffset = 1;
            inputMsbOffset = 0;
        } else
        {
            inputLsbOffset = -1;
            inputMsbOffset = 1;
        }
        int outputEndianess = outFormat.getEndian();
        if(outputEndianess == -1)
            outputEndianess = inFormat.getEndian();
        if(outputEndianess == 1 || 8 == outputSampleSize)
        {
            outputLsbOffset = 1;
            outputMsbOffset = 0;
        } else
        {
            outputLsbOffset = -1;
            outputMsbOffset = 1;
        }
        if(inFormat.getSigned() == 1)
            signMask = -1;
        else
            signMask = 65535;
        if(inFormat.getSigned() == outFormat.getSigned() || outFormat.getSigned() == -1)
            bias = 0;
        else
            bias = 32768;
        if(numberOfInputChannels == 2 && numberOfOutputChannels == 1)
            channels2To1 = true;
        else
            channels2To1 = false;
        if(numberOfInputChannels == 1 && numberOfOutputChannels == 2)
            channels1To2 = true;
        else
            channels1To2 = false;
        if(numberOfInputChannels == 2 && numberOfOutputChannels == 2)
            channels2To2 = true;
        else
            channels2To2 = false;
    }

    private void convert(byte input[], int inputOffset, int inputLength, byte outData[], int outputOffset)
    {
        int sample1 = 0;
        int sample2 = 0;
        outputOffset += outputMsbOffset;
        for(int i = inputOffset + inputMsbOffset; i < inputLength + inputOffset;)
        {
            if(8 == inputSampleSize)
            {
                sample1 = input[i++] << 8;
                if(numberOfInputChannels == 2)
                    sample2 = input[i++] << 8;
            } else
            {
                sample1 = (input[i] << 8) + (0xff & input[i + inputLsbOffset]);
                i += 2;
                if(numberOfInputChannels == 2)
                {
                    sample2 = (input[i] << 8) + (0xff & input[i + inputLsbOffset]);
                    i += 2;
                }
            }
            if(channels2To1)
                sample1 = (sample1 & signMask) + (sample2 & signMask) >> 1;
            sample1 = (short)(sample1 + bias);
            if(channels2To2)
                sample2 = (short)(sample2 + bias);
            if(channels1To2)
                sample2 = sample1;
            if(8 == outputSampleSize)
            {
                outData[outputOffset++] = (byte)(sample1 >> 8);
                if(numberOfOutputChannels == 2)
                    outData[outputOffset++] = (byte)(sample2 >> 8);
            } else
            {
                outData[outputOffset + outputLsbOffset] = (byte)sample1;
                outData[outputOffset] = (byte)(sample1 >> 8);
                outputOffset += 2;
                if(numberOfOutputChannels == 2)
                {
                    outData[outputOffset + outputLsbOffset] = (byte)sample2;
                    outData[outputOffset] = (byte)(sample2 >> 8);
                    outputOffset += 2;
                }
            }
        }

    }

    private Format lastInputFormat;
    private Format lastOutputFormat;
    private int bias;
    private int signMask;
    private int inputSampleSize;
    private int outputSampleSize;
    private int numberOfInputChannels;
    private int numberOfOutputChannels;
    private boolean channels2To1;
    private boolean channels1To2;
    private boolean channels2To2;
    private int inputLsbOffset;
    private int inputMsbOffset;
    private int outputLsbOffset;
    private int outputMsbOffset;
}
