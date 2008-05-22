// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ArrayToPCM.java

package com.ibm.media.codec.audio;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import java.io.PrintStream;
import javax.media.*;
import javax.media.format.AudioFormat;

public class ArrayToPCM extends BasicCodec
{

    public String getName()
    {
        return ArrayToPCM;
    }

    public ArrayToPCM()
    {
        super.inputFormats = (new Format[] {
            new AudioFormat("LINEAR", -1D, -1, -1, -1, -1, -1, -1D, Format.byteArray)
        });
        super.outputFormats = (new Format[] {
            new AudioFormat("LINEAR", -1D, 16, -1, -1, -1, -1, -1D, Format.shortArray)
        });
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        if(!(in instanceof AudioFormat))
            return super.outputFormats;
        AudioFormat iaf = (AudioFormat)in;
        if(!iaf.getEncoding().equals("LINEAR") || iaf.getDataType() != Format.byteArray)
        {
            return new Format[0];
        } else
        {
            AudioFormat oaf = new AudioFormat("LINEAR", iaf.getSampleRate(), 16, iaf.getChannels(), 0, 1, iaf.getFrameSizeInBits(), iaf.getFrameRate(), Format.shortArray);
            return (new Format[] {
                oaf
            });
        }
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(isEOM(inputBuffer))
            return 0;
        byte inBuffer[] = (byte[])inputBuffer.getData();
        AudioFormat inFormat = (AudioFormat)inputBuffer.getFormat();
        boolean isSigned = inFormat.getSigned() == 1;
        boolean isBigEndian = inFormat.getEndian() == 1;
        int sampleSize = inFormat.getFrameSizeInBits() + 7 >> 3;
        int inLength = inputBuffer.getLength();
        int samplesNumber = sampleSize != 1 ? inLength >> 1 : inLength;
        int outLength = samplesNumber;
        short outBuffer[] = validateShortArraySize(outputBuffer, outLength);
        int offset = isSigned ? 0 : 32768;
        int inOffset = 0;
        int outOffset = 0;
        if(sampleSize == 1)
        {
            for(int i = samplesNumber - 1; i >= 0; i--)
                outBuffer[i] = (short)((inBuffer[i] << 8) + offset);

        } else
        if(isBigEndian)
        {
            for(int i = samplesNumber - 1; i >= 0; i--)
            {
                int sample1 = inBuffer[inOffset++] << 8;
                int sample2 = inBuffer[inOffset++] & 0xff;
                outBuffer[outOffset++] = (short)((sample1 | sample2) + offset);
            }

        } else
        {
            for(int i = samplesNumber - 1; i >= 0; i--)
            {
                int sample1 = inBuffer[inOffset++] & 0xff;
                int sample2 = inBuffer[inOffset++] << 8;
                outBuffer[outOffset++] = (short)((sample1 | sample2) + offset);
            }

        }
        outputBuffer.setLength(samplesNumber);
        outputBuffer.setFormat(super.outputFormat);
        return 0;
    }

    public static void main(String args[])
    {
        Codec codec = new ArrayToPCM();
        Format ifmt[] = codec.getSupportedInputFormats();
        Format ofmt[] = codec.getSupportedOutputFormats(new AudioFormat("LINEAR", 8000D, 8, 2, 0, 0));
        for(int i = 0; i < ifmt.length; i++)
            System.out.println(ifmt[i]);

        System.out.println("* out *");
        for(int i = 0; i < ofmt.length; i++)
            System.out.println(ofmt[i]);

    }

    private static String ArrayToPCM = "ArrayToPCM";

}
