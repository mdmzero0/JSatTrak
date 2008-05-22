// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VideoCodec.java

package com.ibm.media.codec.video;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import java.awt.Dimension;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;

public abstract class VideoCodec extends BasicCodec
{

    public VideoCodec()
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
        if(!(format instanceof VideoFormat) || null == BasicPlugIn.matches(format, supportedInputFormats))
        {
            return null;
        } else
        {
            inputFormat = (VideoFormat)format;
            return format;
        }
    }

    public Format setOutputFormat(Format format)
    {
        if(!(format instanceof VideoFormat) || null == BasicPlugIn.matches(format, getMatchingOutputFormats(inputFormat)))
        {
            return null;
        } else
        {
            outputFormat = (VideoFormat)format;
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
        if(!(in instanceof VideoFormat) || BasicPlugIn.matches(in, supportedInputFormats) == null)
            return new Format[0];
        else
            return getMatchingOutputFormats(in);
    }

    public boolean checkFormat(Format format)
    {
        Dimension inSize = ((VideoFormat)format).getSize();
        if(!inSize.equals(outputFormat.getSize()))
            videoResized();
        return true;
    }

    protected void videoResized()
    {
    }

    protected void updateOutput(Buffer outputBuffer, Format format, int length, int offset)
    {
        outputBuffer.setFormat(format);
        outputBuffer.setLength(length);
        outputBuffer.setOffset(offset);
    }

    protected String PLUGIN_NAME;
    protected VideoFormat defaultOutputFormats[];
    protected VideoFormat supportedInputFormats[];
    protected VideoFormat supportedOutputFormats[];
    protected VideoFormat inputFormat;
    protected VideoFormat outputFormat;
    protected final boolean DEBUG = true;
}
