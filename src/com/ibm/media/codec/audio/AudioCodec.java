// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AudioCodec.java

package com.ibm.media.codec.audio;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import javax.media.Format;
import javax.media.format.AudioFormat;

public abstract class AudioCodec extends BasicCodec
{

    public AudioCodec()
    {
    }

    public String getName()
    {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedInputFormats()
    {
        return supportedInputFormats;
    }

    public Format setInputFormat(Format format)
    {
        if(!(format instanceof AudioFormat) || null == BasicPlugIn.matches(format, supportedInputFormats))
        {
            return null;
        } else
        {
            inputFormat = (AudioFormat)format;
            return format;
        }
    }

    public Format setOutputFormat(Format format)
    {
        if(!(format instanceof AudioFormat) || null == BasicPlugIn.matches(format, getMatchingOutputFormats(inputFormat)))
        {
            return null;
        } else
        {
            outputFormat = (AudioFormat)format;
            return format;
        }
    }

    protected Format getInputFormat()
    {
        return inputFormat;
    }

    protected Format getOutputFormat()
    {
        return outputFormat;
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        return new Format[0];
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        if(in == null)
            return defaultOutputFormats;
        if(!(in instanceof AudioFormat) || BasicPlugIn.matches(in, supportedInputFormats) == null)
            return new Format[0];
        else
            return getMatchingOutputFormats(in);
    }

    public boolean checkFormat(Format format)
    {
        return true;
    }

    protected String PLUGIN_NAME;
    protected AudioFormat defaultOutputFormats[];
    protected AudioFormat supportedInputFormats[];
    protected AudioFormat supportedOutputFormats[];
    protected AudioFormat inputFormat;
    protected AudioFormat outputFormat;
    protected final boolean DEBUG = true;
}
