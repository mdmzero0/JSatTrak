// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicCodec.java

package com.sun.media;

import java.awt.Dimension;
import java.io.PrintStream;
import javax.media.*;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media:
//            BasicPlugIn

public abstract class BasicCodec extends BasicPlugIn
    implements Codec
{

    public BasicCodec()
    {
        opened = false;
        inputFormats = new Format[0];
        outputFormats = new Format[0];
        pendingEOM = false;
    }

    public Format setInputFormat(Format input)
    {
        inputFormat = input;
        return input;
    }

    public Format setOutputFormat(Format output)
    {
        outputFormat = output;
        return output;
    }

    protected Format getInputFormat()
    {
        return inputFormat;
    }

    protected Format getOutputFormat()
    {
        return outputFormat;
    }

    public void reset()
    {
    }

    public void open()
        throws ResourceUnavailableException
    {
        opened = true;
    }

    public void close()
    {
        opened = false;
    }

    public Format[] getSupportedInputFormats()
    {
        return inputFormats;
    }

    protected RGBFormat updateRGBFormat(VideoFormat newFormat, RGBFormat outputFormat)
    {
        Dimension size = newFormat.getSize();
        RGBFormat oldFormat = outputFormat;
        int lineStride = size.width * oldFormat.getPixelStride();
        RGBFormat newRGB = new RGBFormat(size, lineStride * size.height, oldFormat.getDataType(), newFormat.getFrameRate(), oldFormat.getBitsPerPixel(), oldFormat.getRedMask(), oldFormat.getGreenMask(), oldFormat.getBlueMask(), oldFormat.getPixelStride(), lineStride, oldFormat.getFlipped(), oldFormat.getEndian());
        return newRGB;
    }

    protected boolean isEOM(Buffer inputBuffer)
    {
        return inputBuffer.isEOM();
    }

    protected void propagateEOM(Buffer outputBuffer)
    {
        updateOutput(outputBuffer, getOutputFormat(), 0, 0);
        outputBuffer.setEOM(true);
    }

    protected void updateOutput(Buffer outputBuffer, Format format, int length, int offset)
    {
        outputBuffer.setFormat(format);
        outputBuffer.setLength(length);
        outputBuffer.setOffset(offset);
    }

    protected boolean checkInputBuffer(Buffer inputBuffer)
    {
        boolean fError = !isEOM(inputBuffer) && (inputBuffer == null || inputBuffer.getFormat() == null || !checkFormat(inputBuffer.getFormat()));
        if(fError)
            System.out.println(getClass().getName() + " : [error] checkInputBuffer");
        return !fError;
    }

    protected boolean checkFormat(Format format)
    {
        return true;
    }

    protected int checkEOM(Buffer inputBuffer, Buffer outputBuffer)
    {
        processAtEOM(inputBuffer, outputBuffer);
        if(outputBuffer.getLength() > 0)
        {
            pendingEOM = true;
            return 2;
        } else
        {
            propagateEOM(outputBuffer);
            return 0;
        }
    }

    protected int processAtEOM(Buffer inputBuffer, Buffer outputBuffer)
    {
        return 0;
    }

    protected int getArrayElementSize(Class type)
    {
        if(type == Format.intArray)
            return 4;
        if(type == Format.shortArray)
            return 2;
        return type != Format.byteArray ? 0 : 1;
    }

    public abstract int process(Buffer buffer, Buffer buffer1);

    public abstract Format[] getSupportedOutputFormats(Format format);

    private static final boolean DEBUG = true;
    protected Format inputFormat;
    protected Format outputFormat;
    protected boolean opened;
    protected Format inputFormats[];
    protected Format outputFormats[];
    protected boolean pendingEOM;
}
