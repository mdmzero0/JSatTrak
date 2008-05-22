// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GSMMux.java

package com.sun.media.multiplexer.audio;

import com.sun.media.multiplexer.BasicMux;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

public class GSMMux extends BasicMux
{

    public GSMMux()
    {
        super.supportedInputs = new Format[1];
        super.supportedInputs[0] = new AudioFormat("gsm");
        super.supportedOutputs = new ContentDescriptor[1];
        super.supportedOutputs[0] = new FileTypeDescriptor("audio.x_gsm");
    }

    public String getName()
    {
        return "GSM Multiplexer";
    }

    public Format setInputFormat(Format input, int trackID)
    {
        if(!(input instanceof AudioFormat))
            return null;
        AudioFormat format = (AudioFormat)input;
        double sampleRate = format.getSampleRate();
        String reason = null;
        double epsilon = 0.25D;
        if(!format.getEncoding().equalsIgnoreCase("gsm"))
            reason = "Encoding has to be GSM";
        else
        if(Math.abs(sampleRate - 8000D) > epsilon)
            reason = "Sample rate should be 8000. Cannot handle sample rate " + sampleRate;
        else
        if(format.getFrameSizeInBits() != 264)
            reason = "framesize should be 33 bytes";
        else
        if(format.getChannels() != 1)
            reason = "Number of channels should be 1";
        if(reason != null)
        {
            return null;
        } else
        {
            super.inputs[0] = format;
            return format;
        }
    }
}
