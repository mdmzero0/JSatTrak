// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MPEGMux.java

package com.sun.media.multiplexer.audio;

import com.sun.media.multiplexer.BasicMux;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

public class MPEGMux extends BasicMux
{

    public MPEGMux()
    {
        super.supportedInputs = new Format[2];
        super.supportedInputs[0] = new AudioFormat("mpeglayer3");
        super.supportedInputs[1] = new AudioFormat("mpegaudio");
        super.supportedOutputs = new ContentDescriptor[1];
        super.supportedOutputs[0] = new FileTypeDescriptor("audio.mpeg");
    }

    public String getName()
    {
        return "MPEG Audio Multiplexer";
    }

    public Format setInputFormat(Format input, int trackID)
    {
        if(!(input instanceof AudioFormat))
            return null;
        AudioFormat format = (AudioFormat)input;
        double sampleRate = format.getSampleRate();
        String reason = null;
        double epsilon = 0.25D;
        if(!format.getEncoding().equalsIgnoreCase("mpeglayer3") && !format.getEncoding().equalsIgnoreCase("mpegaudio"))
            reason = "Encoding has to be MPEG audio";
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
