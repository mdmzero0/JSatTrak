// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DePacketizer.java

package com.sun.media.codec.audio.ulaw;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.codec.audio.AudioCodec;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;

public class DePacketizer extends AudioCodec
{

    public DePacketizer()
    {
        super.inputFormats = (new Format[] {
            new AudioFormat("ULAW/rtp")
        });
    }

    public String getName()
    {
        return "ULAW DePacketizer";
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        if(in == null)
            return (new Format[] {
                new AudioFormat("ULAW")
            });
        if(BasicPlugIn.matches(in, super.inputFormats) == null)
            return new Format[1];
        if(!(in instanceof AudioFormat))
        {
            return (new Format[] {
                new AudioFormat("ULAW")
            });
        } else
        {
            AudioFormat af = (AudioFormat)in;
            return (new Format[] {
                new AudioFormat("ULAW", af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels())
            });
        }
    }

    public void open()
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
        } else
        {
            Object outData = outputBuffer.getData();
            outputBuffer.setData(inputBuffer.getData());
            inputBuffer.setData(outData);
            outputBuffer.setLength(inputBuffer.getLength());
            outputBuffer.setFormat(super.outputFormat);
            outputBuffer.setOffset(inputBuffer.getOffset());
            return 0;
        }
    }
}
