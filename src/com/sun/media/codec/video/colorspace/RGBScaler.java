// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RGBScaler.java

package com.sun.media.codec.video.colorspace;

import com.sun.media.*;
import java.awt.Dimension;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

public class RGBScaler extends BasicCodec
{

    public RGBScaler()
    {
        this(null);
    }

    public RGBScaler(Dimension sizeOut)
    {
        quality = 0.5F;
        nativeData = 0;
        super.inputFormats = (new Format[] {
            new RGBFormat(null, -1, Format.byteArray, -1F, 24, 3, 2, 1, 3, -1, 0, -1)
        });
        if(sizeOut != null)
            setOutputSize(sizeOut);
    }

    public void setOutputSize(Dimension sizeOut)
    {
        super.outputFormats = (new Format[] {
            new RGBFormat(sizeOut, sizeOut.width * sizeOut.height * 3, Format.byteArray, -1F, 24, 3, 2, 1, 3, sizeOut.width * 3, 0, -1)
        });
    }

    public String getName()
    {
        return "RGB Scaler";
    }

    public Format[] getSupportedOutputFormats(Format input)
    {
        if(input == null)
            return super.outputFormats;
        if(BasicPlugIn.matches(input, super.inputFormats) != null)
        {
            float frameRate = ((VideoFormat)input).getFrameRate();
            VideoFormat frameRateFormat = new VideoFormat(null, null, -1, null, frameRate);
            return (new Format[] {
                super.outputFormats[0].intersects(frameRateFormat)
            });
        } else
        {
            return new Format[0];
        }
    }

    public Format setInputFormat(Format input)
    {
        if(BasicPlugIn.matches(input, super.inputFormats) == null)
            return null;
        else
            return input;
    }

    public Format setOutputFormat(Format output)
    {
        if(output == null || BasicPlugIn.matches(output, super.outputFormats) == null)
            return null;
        RGBFormat incoming = (RGBFormat)output;
        Dimension size = incoming.getSize();
        int maxDataLength = incoming.getMaxDataLength();
        int lineStride = incoming.getLineStride();
        float frameRate = incoming.getFrameRate();
        int flipped = incoming.getFlipped();
        int endian = incoming.getEndian();
        if(size == null)
            return null;
        if(maxDataLength < size.width * size.height * 3)
            maxDataLength = size.width * size.height * 3;
        if(lineStride < size.width * 3)
            lineStride = size.width * 3;
        if(flipped != 0)
            flipped = 0;
        super.outputFormat = super.outputFormats[0].intersects(new RGBFormat(size, maxDataLength, null, frameRate, -1, -1, -1, -1, -1, lineStride, -1, -1));
        return super.outputFormat;
    }

    public int process(Buffer inBuffer, Buffer outBuffer)
    {
        int outputDataLength = ((VideoFormat)super.outputFormat).getMaxDataLength();
        outBuffer.setLength(outputDataLength);
        outBuffer.setFormat(super.outputFormat);
        if(quality <= 0.5F)
            nearestNeighbour(inBuffer, outBuffer);
        return 0;
    }

    public void close()
    {
        super.close();
        if(nativeAvailable && nativeData != 0)
            try
            {
                nativeClose();
            }
            catch(Throwable t) { }
    }

    protected void nearestNeighbour(Buffer inBuffer, Buffer outBuffer)
    {
        RGBFormat vfIn = (RGBFormat)inBuffer.getFormat();
        Dimension sizeIn = vfIn.getSize();
        RGBFormat vfOut = (RGBFormat)outBuffer.getFormat();
        Dimension sizeOut = vfOut.getSize();
        int pixStrideIn = vfIn.getPixelStride();
        int pixStrideOut = vfOut.getPixelStride();
        int lineStrideIn = vfIn.getLineStride();
        int lineStrideOut = vfOut.getLineStride();
        float horRatio = (float)sizeIn.width / (float)sizeOut.width;
        float verRatio = (float)sizeIn.height / (float)sizeOut.height;
        long inBytes = 0L;
        long outBytes = 0L;
        Object inObj;
        Object outObj;
        if(nativeAvailable)
        {
            inObj = getInputData(inBuffer);
            outObj = validateData(outBuffer, 0, true);
            inBytes = getNativeData(inObj);
            outBytes = getNativeData(outObj);
        } else
        {
            inObj = inBuffer.getData();
            outObj = outBuffer.getData();
        }
        if(nativeAvailable)
            try
            {
                nativeScale(inObj, inBytes, outObj, outBytes, pixStrideIn, lineStrideIn, sizeIn.width, sizeIn.height, pixStrideOut, lineStrideOut, sizeOut.width, sizeOut.height);
            }
            catch(Throwable t)
            {
                nativeAvailable = false;
            }
        if(!nativeAvailable)
        {
            byte inData[] = (byte[])inObj;
            byte outData[] = (byte[])outObj;
            for(int y = 0; y < sizeOut.height; y++)
            {
                int ptrOut = y * lineStrideOut;
                int ptrIn = (int)((float)y * verRatio) * lineStrideIn;
                for(int x = 0; x < sizeOut.width; x++)
                {
                    int ptrIn2 = ptrIn + (int)((float)x * horRatio) * pixStrideIn;
                    outData[ptrOut] = inData[ptrIn2];
                    outData[ptrOut + 1] = inData[ptrIn2 + 1];
                    outData[ptrOut + 2] = inData[ptrIn2 + 2];
                    ptrOut += pixStrideOut;
                }

            }

        }
    }

    private native void nativeScale(Object obj, long l, Object obj1, long l1, int i, 
            int j, int k, int i1, int j1, int k1, int i2, int j2);

    private native void nativeClose();

    protected float quality;
    private int nativeData;
    private static boolean nativeAvailable = true;

    static 
    {
        try
        {
            JMFSecurityManager.loadLibrary("jmutil");
        }
        catch(Throwable t) { }
    }
}
