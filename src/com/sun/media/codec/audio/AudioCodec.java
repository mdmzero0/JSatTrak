// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AudioCodec.java

package com.sun.media.codec.audio;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import javax.media.Format;
import javax.media.format.AudioFormat;

public abstract class AudioCodec extends BasicCodec
{

    public AudioCodec()
    {
    }

    public Format setInputFormat(Format format)
    {
        if(BasicPlugIn.matches(format, super.inputFormats) == null)
        {
            return null;
        } else
        {
            super.inputFormat = format;
            return format;
        }
    }

    public Format setOutputFormat(Format format)
    {
        if(BasicPlugIn.matches(format, getSupportedOutputFormats(super.inputFormat)) == null)
            return null;
        if(!(format instanceof AudioFormat))
        {
            return null;
        } else
        {
            super.outputFormat = (AudioFormat)format;
            return format;
        }
    }

    public boolean checkFormat(Format format)
    {
        if(super.inputFormat == null || super.outputFormat == null || format != super.inputFormat || !format.equals(super.inputFormat))
        {
            super.inputFormat = format;
            Format fs[] = getSupportedOutputFormats(format);
            super.outputFormat = (AudioFormat)fs[0];
        }
        return super.outputFormat != null;
    }
}
