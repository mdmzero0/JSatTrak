// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaDecoder.java

package com.sun.media.codec.video.cinepak;

import com.ibm.media.codec.video.VideoCodec;
import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import java.awt.Dimension;
import javax.media.*;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.codec.video.cinepak:
//            CineStore

public class JavaDecoder extends VideoCodec
{

    public JavaDecoder()
    {
        refData = null;
        super.supportedInputFormats = (new VideoFormat[] {
            new VideoFormat("cvid")
        });
        super.defaultOutputFormats = (new VideoFormat[] {
            new RGBFormat(null, -1, Format.intArray, -1F, 32, 255, 65280, 0xff0000, 1, -1, 0, -1)
        });
        super.PLUGIN_NAME = "Cinepak Decoder";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        VideoFormat ivf = (VideoFormat)in;
        Dimension inSize = ivf.getSize();
        int lineStride = inSize.width + 3 & -4;
        int rowStride = inSize.height + 3 & -4;
        super.supportedOutputFormats = (new VideoFormat[] {
            new RGBFormat(new Dimension(inSize), lineStride * rowStride, Format.intArray, ivf.getFrameRate(), 32, 255, 65280, 0xff0000)
        });
        return super.supportedOutputFormats;
    }

    public void open()
        throws ResourceUnavailableException
    {
        initDecoder();
    }

    public void close()
    {
        fOurStore = null;
    }

    public void reset()
    {
    }

    protected void videoResized()
    {
        initDecoder();
    }

    protected void initDecoder()
    {
        fOurStore = new CineStore();
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
        VideoFormat ivf = (VideoFormat)inputBuffer.getFormat();
        int inLength = inputBuffer.getLength();
        int inMaxLength = ivf.getMaxDataLength();
        int outMaxLength = super.outputFormat.getMaxDataLength();
        byte inData[] = (byte[])inputBuffer.getData();
        int outData[] = validateIntArraySize(outputBuffer, outMaxLength);
        if(refData == null)
        {
            refData = outData;
            outputBuffer.setData(null);
            outData = validateIntArraySize(outputBuffer, outMaxLength);
        }
        outputBuffer.setData(refData);
        outputBuffer.setFormat(super.outputFormat);
        fOurStore.DoFrame(inputBuffer, outputBuffer, fOurStore);
        System.arraycopy(refData, 0, outData, 0, outMaxLength);
        outputBuffer.setData(outData);
        updateOutput(outputBuffer, super.outputFormat, outMaxLength, 0);
        return 0;
    }

    private static final int rMask = 255;
    private static final int gMask = 65280;
    private static final int bMask = 0xff0000;
    private int refData[];
    private CineStore fOurStore;
}
