// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaDecoder.java

package com.ibm.media.codec.audio.alaw;

import com.ibm.media.codec.audio.AudioCodec;
import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.controls.SilenceSuppressionAdapter;
import javax.media.*;
import javax.media.format.AudioFormat;

public class JavaDecoder extends AudioCodec
{

    public JavaDecoder()
    {
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("alaw")
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR")
        });
        super.PLUGIN_NAME = "A-Law Decoder";
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
    {
        initTables();
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
        int channels = super.outputFormat.getChannels();
        byte inData[] = (byte[])inputBuffer.getData();
        byte outData[] = validateByteArraySize(outputBuffer, inData.length * 2);
        int inpLength = inputBuffer.getLength();
        int outLength = 2 * inpLength;
        int inOffset = 0;
        int outOffset = 0;
        for(int i = 0; i < inpLength; i++)
        {
            int temp = inData[inOffset++] & 0xff;
            outData[outOffset++] = lutTableL[temp];
            outData[outOffset++] = lutTableH[temp];
        }

        updateOutput(outputBuffer, super.outputFormat, outLength, 0);
        return 0;
    }

    private void initTables()
    {
        for(int i = 0; i < 256; i++)
        {
            int input = i ^ 0x55;
            int mantissa = (input & 0xf) << 4;
            int segment = (input & 0x70) >> 4;
            int value = mantissa + 8;
            if(segment >= 1)
                value += 256;
            if(segment > 1)
                value <<= segment - 1;
            if((input & 0x80) == 0)
                value = -value;
            lutTableL[i] = (byte)value;
            lutTableH[i] = (byte)(value >> 8);
        }

    }

    public Object[] getControls()
    {
        if(super.controls == null)
        {
            super.controls = new Control[1];
            super.controls[0] = new SilenceSuppressionAdapter(this, false, false);
        }
        return (Object[])super.controls;
    }

    private static final byte lutTableL[] = new byte[256];
    private static final byte lutTableH[] = new byte[256];

}
