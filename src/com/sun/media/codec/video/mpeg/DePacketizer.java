// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DePacketizer.java

package com.sun.media.codec.video.mpeg;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import javax.media.*;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.codec.video.mpeg:
//            RTPDePacketizer

public class DePacketizer extends BasicCodec
{

    public DePacketizer()
    {
        inputFormat = null;
        outputFormat = null;
        rtpdp = null;
        super.inputFormats = (new Format[] {
            new VideoFormat("mpeg/rtp")
        });
        super.outputFormats = (new Format[] {
            new VideoFormat("mpeg")
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
            out[0] = makeMPEGFormat(in);
            return out;
        }
    }

    public Format setInputFormat(Format input)
    {
        inputFormat = (VideoFormat)input;
        return input;
    }

    public Format setOutputFormat(Format output)
    {
        if(!(output instanceof VideoFormat))
        {
            return null;
        } else
        {
            outputFormat = makeMPEGFormat(output);
            return output;
        }
    }

    private final VideoFormat makeMPEGFormat(Format in)
    {
        VideoFormat vf = (VideoFormat)in;
        return new VideoFormat("mpeg", vf.getSize(), -1, Format.byteArray, vf.getFrameRate());
    }

    public void open()
        throws ResourceUnavailableException
    {
        if(inputFormat == null || outputFormat == null)
        {
            throw new ResourceUnavailableException("Incorrect formats set on MPEG video depacketizer");
        } else
        {
            rtpdp = new RTPDePacketizer();
            return;
        }
    }

    public synchronized void close()
    {
        rtpdp = null;
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
        if(outputFormat == null)
            outputFormat = (VideoFormat)outBuffer.getFormat();
        return 0;
    }

    public void finalize()
    {
        close();
    }

    public String getName()
    {
        return "MPEG Video DePacketizer";
    }

    private VideoFormat inputFormat;
    private VideoFormat outputFormat;
    private RTPDePacketizer rtpdp;
}
