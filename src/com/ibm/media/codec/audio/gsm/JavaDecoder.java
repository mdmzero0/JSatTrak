// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaDecoder.java

package com.ibm.media.codec.audio.gsm;

import com.ibm.media.codec.audio.AudioCodec;
import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.controls.SilenceSuppressionAdapter;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.gsm:
//            GsmDecoder

public class JavaDecoder extends AudioCodec
{

    public JavaDecoder()
    {
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("gsm"), new AudioFormat("gsm/rtp")
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR")
        });
        super.PLUGIN_NAME = "GSM Decoder";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        super.supportedOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", af.getSampleRate(), 16, af.getChannels(), 0, 1)
        });
        return super.supportedOutputFormats;
    }

    public void open()
        throws ResourceUnavailableException
    {
        decoder = new GsmDecoder();
        decoder.decoderInit();
    }

    public void reset()
    {
        resetDecoder();
    }

    public void close()
    {
        freeDecoder();
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(!checkInputBuffer(inputBuffer))
            return 1;
        if(isEOM(inputBuffer))
        {
            propagateEOM(outputBuffer);
            return 0;
        } else
        {
            int inpLength = inputBuffer.getLength();
            int outLength = calculateOutputSize(inputBuffer.getLength());
            byte inpData[] = (byte[])inputBuffer.getData();
            byte outData[] = validateByteArraySize(outputBuffer, outLength);
            decode(inpData, inputBuffer.getOffset(), outData, 0, inpLength);
            updateOutput(outputBuffer, super.outputFormat, outLength, 0);
            return 0;
        }
    }

    protected void freeDecoder()
    {
        decoder = null;
    }

    protected void resetDecoder()
    {
        decoder.decoderInit();
    }

    protected int calculateOutputSize(int inputSize)
    {
        return (inputSize / 33) * 320;
    }

    protected void decode(byte inpData[], int readPtr, byte outData[], int writePtr, int inpLength)
    {
        int numberOfFrames = inpLength / 33;
        for(int n = 1; n <= numberOfFrames;)
        {
            decoder.decodeFrame(inpData, readPtr, outData, writePtr);
            n++;
            writePtr += 320;
            readPtr += 33;
        }

    }

    public Object[] getControls()
    {
        if(super.controls == null)
        {
            super.controls = new Control[1];
            super.controls[0] = new SilenceSuppressionAdapter(this, true, false);
        }
        return (Object[])super.controls;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1999.";
    protected GsmDecoder decoder;
}
