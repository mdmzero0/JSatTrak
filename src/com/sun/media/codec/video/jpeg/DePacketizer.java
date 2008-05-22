// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DePacketizer.java

package com.sun.media.codec.video.jpeg;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import java.awt.Dimension;
import javax.media.*;
import javax.media.format.JPEGFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.codec.video.jpeg:
//            RTPDePacketizer

public class DePacketizer extends BasicCodec
{

    public DePacketizer()
    {
        inputFormat = null;
        outputFormat = null;
        decimation = -1;
        quality = -1;
        rtpdp = null;
        DEFAULT_WIDTH = 320;
        DEFAULT_HEIGHT = 240;
        super.inputFormats = (new Format[] {
            new VideoFormat("jpeg/rtp")
        });
        super.outputFormats = (new Format[] {
            new VideoFormat("jpeg")
        });
    }

    protected Format getInputFormat()
    {
        return inputFormat;
    }

    protected Format getOutputFormat()
    {
        return outputFormat;
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        if(in == null)
            return super.outputFormats;
        if(BasicPlugIn.matches(in, super.inputFormats) == null)
        {
            return new Format[0];
        } else
        {
            Format out[] = new Format[1];
            out[0] = makeJPEGFormat(in);
            return out;
        }
    }

    public Format setInputFormat(Format input)
    {
        inputFormat = (VideoFormat)input;
        if(super.opened)
            outputFormat = makeJPEGFormat(inputFormat);
        return input;
    }

    public Format setOutputFormat(Format output)
    {
        if(!(output instanceof VideoFormat))
        {
            return null;
        } else
        {
            outputFormat = makeJPEGFormat(output);
            return outputFormat;
        }
    }

    private final JPEGFormat makeJPEGFormat(Format in)
    {
        VideoFormat vf = (VideoFormat)in;
        return new JPEGFormat(vf.getSize() == null ? new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT) : vf.getSize(), -1, Format.byteArray, vf.getFrameRate(), quality, decimation);
    }

    public void open()
        throws ResourceUnavailableException
    {
        if(inputFormat == null || outputFormat == null)
        {
            throw new ResourceUnavailableException("Incorrect formats set on JPEG converter");
        } else
        {
            rtpdp = new RTPDePacketizer();
            super.open();
            return;
        }
    }

    public synchronized void close()
    {
        rtpdp = null;
        super.close();
    }

    public void reset()
    {
    }

    public synchronized int process(Buffer inBuffer, Buffer outBuffer)
    {
        if(isEOM(inBuffer))
        {
            propagateEOM(outBuffer);
            return 0;
        }
        if(inBuffer.isDiscard())
        {
            updateOutput(outBuffer, outputFormat, 0, 0);
            outBuffer.setDiscard(true);
            return 4;
        }
        int retVal = rtpdp.process(inBuffer, outBuffer);
        if(retVal != 0)
            return retVal;
        int type = rtpdp.getType();
        int q = rtpdp.getQuality();
        if(type != decimation || q != quality)
        {
            decimation = type;
            quality = q;
            outputFormat = makeJPEGFormat(inBuffer.getFormat());
        }
        outBuffer.setFormat(outputFormat);
        outBuffer.setOffset(0);
        outBuffer.setTimeStamp(inBuffer.getTimeStamp());
        inBuffer.setLength(0);
        outBuffer.setFlags(outBuffer.getFlags() | 0x10);
        return 0;
    }

    public void finalize()
    {
        close();
    }

    public String getName()
    {
        return "JPEG DePacketizer";
    }

    private VideoFormat inputFormat;
    private JPEGFormat outputFormat;
    private int decimation;
    private int quality;
    private RTPDePacketizer rtpdp;
    int DEFAULT_WIDTH;
    int DEFAULT_HEIGHT;
}
