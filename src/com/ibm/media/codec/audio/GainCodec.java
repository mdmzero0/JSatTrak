// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GainCodec.java

package com.ibm.media.codec.audio;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import java.io.PrintStream;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;

public class GainCodec extends BasicCodec
{

    public void setGain(float newGain)
    {
        gain = newGain;
    }

    public String getName()
    {
        return GainCodec;
    }

    public GainCodec()
    {
        gain = 2.0F;
        super.inputFormats = (new Format[] {
            new AudioFormat("LINEAR", -1D, 16, -1, -1, -1, -1, -1D, Format.shortArray)
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
        if(!iaf.getEncoding().equals("LINEAR") || iaf.getDataType() != Format.shortArray)
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
        {
            propagateEOM(outputBuffer);
            return 0;
        }
        short inBuffer[] = (short[])inputBuffer.getData();
        int inLength = inputBuffer.getLength();
        int inOffset = inputBuffer.getOffset();
        int samplesNumber = inLength;
        short outBuffer[] = validateShortArraySize(outputBuffer, samplesNumber);
        for(int i = 0; i < samplesNumber; i++)
        {
            int sample = inBuffer[inOffset + i];
            sample = (int)((float)sample * gain);
            if(sample > 32767)
                sample = 32767;
            else
            if(sample < -32768)
                sample = -32768;
            outBuffer[i] = (short)sample;
        }

        updateOutput(outputBuffer, super.outputFormat, samplesNumber, 0);
        return 0;
    }

    public static void main(String args[])
    {
        GainCodec codec = new GainCodec();
        Format ifmt[] = codec.getSupportedInputFormats();
        Format ofmt[] = codec.getSupportedOutputFormats(null);
        Buffer inp = new Buffer();
        Buffer out = new Buffer();
        short buffer[] = new short[100];
        for(int i = 0; i < 100; i++)
            buffer[i] = (short)(i + 20500);

        inp.setData(buffer);
        inp.setLength(10);
        inp.setOffset(0);
        codec.setGain(1.6F);
        int rc = codec.process(inp, out);
        System.out.println("rc=" + rc);
        short outbuf[] = (short[])out.getData();
        System.out.println("length=" + out.getLength());
        System.out.println("offset=" + out.getOffset());
        for(int i = 0; i < outbuf.length; i++)
            System.out.println(i + " " + outbuf[i]);

        inp.setLength(0);
        inp.setEOM(true);
        rc = codec.process(inp, out);
        System.out.println("rc=" + rc);
        outbuf = (short[])out.getData();
        System.out.println("length=" + out.getLength());
        System.out.println("offset=" + out.getOffset());
        for(int i = 0; i < outbuf.length; i++)
            System.out.println(i + " " + outbuf[i]);

    }

    private static String GainCodec = "GainCodec";
    public float gain;

}
