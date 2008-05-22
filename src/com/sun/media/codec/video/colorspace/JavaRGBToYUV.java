// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaRGBToYUV.java

package com.sun.media.codec.video.colorspace;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import java.awt.Component;
import java.awt.Dimension;
import javax.media.*;
import javax.media.control.FrameProcessingControl;
import javax.media.format.*;

public class JavaRGBToYUV extends BasicCodec
{

    public JavaRGBToYUV()
    {
        frameControl = null;
        dropFrame = false;
        int NS = -1;
        super.inputFormats = (new Format[] {
            new RGBFormat(null, NS, Format.byteArray, NS, 24, NS, NS, NS, NS, NS, NS, NS), new RGBFormat(null, NS, Format.intArray, NS, 32, 0xff0000, 65280, 255, 1, NS, NS, NS), new RGBFormat(null, NS, Format.intArray, NS, 32, 255, 65280, 0xff0000, 1, NS, NS, NS)
        });
        super.outputFormats = (new Format[] {
            new YUVFormat(2)
        });
        if(frameControl == null)
        {
            class FPC
                implements FrameProcessingControl
            {

                public void setFramesBehind(float frames)
                {
                    if(frames > 0.0F)
                        dropFrame = true;
                    else
                        dropFrame = false;
                }

                public boolean setMinimalProcessing(boolean minimal)
                {
                    dropFrame = minimal;
                    return dropFrame;
                }

                public Component getControlComponent()
                {
                    return null;
                }

                public int getFramesDropped()
                {
                    return 0;
                }

            FPC()
            {
            }
            }

            frameControl = new FPC();
            super.controls = new Control[1];
            super.controls[0] = frameControl;
        }
    }

    public String getName()
    {
        return "RGB To YUV";
    }

    public Format[] getSupportedOutputFormats(Format input)
    {
        if(input == null)
            return super.outputFormats;
        if((input instanceof RGBFormat) && BasicPlugIn.matches(input, super.inputFormats) != null)
        {
            RGBFormat rgb = (RGBFormat)input;
            Dimension size = rgb.getSize();
            float frameRate = rgb.getFrameRate();
            int bpp = rgb.getBitsPerPixel();
            int scan = size.width + 1 & -2;
            YUVFormat output = new YUVFormat(size, (scan * size.height * 3) / 2, Format.byteArray, frameRate, 2, scan, scan / 2, 0, scan * size.height, (scan * size.height * 5) / 4);
            Format outputs[] = {
                output
            };
            return outputs;
        } else
        {
            return new Format[0];
        }
    }

    public Format setInputFormat(Format input)
    {
        Format ret = super.setInputFormat(input);
        if(super.opened)
            super.outputFormat = getSupportedOutputFormats(ret)[0];
        return ret;
    }

    public int process(Buffer inBuffer, Buffer outBuffer)
    {
        if(isEOM(inBuffer))
        {
            propagateEOM(outBuffer);
            return 0;
        }
        if(dropFrame)
        {
            outBuffer.setFlags(outBuffer.getFlags() | 2);
            return 0;
        }
        Format inF = inBuffer.getFormat();
        if(!(inF instanceof RGBFormat) || inBuffer.getData() == null)
            return 1;
        Object inData = inBuffer.getData();
        validateByteArraySize(outBuffer, ((VideoFormat)super.outputFormat).getMaxDataLength());
        outBuffer.setFormat(super.outputFormat);
        outBuffer.setLength(((VideoFormat)super.outputFormat).getMaxDataLength());
        if(((RGBFormat)inF).getBitsPerPixel() == 24)
            return convert24(inBuffer, outBuffer);
        else
            return convertInt(inBuffer, outBuffer);
    }

    protected int convert24(Buffer inBuffer, Buffer outBuffer)
    {
        RGBFormat rgb = (RGBFormat)inBuffer.getFormat();
        YUVFormat yuv = (YUVFormat)outBuffer.getFormat();
        Dimension size = rgb.getSize();
        byte outData[] = (byte[])outBuffer.getData();
        byte inData[] = (byte[])inBuffer.getData();
        boolean flipped = rgb.getFlipped() == 1;
        int increment = flipped ? -1 : 1;
        int ystride = yuv.getStrideY();
        int uvstride = yuv.getStrideUV();
        int pointerY = yuv.getOffsetY() + ystride * (flipped ? size.height - 1 : 0);
        int pointerU = yuv.getOffsetU() + uvstride * (flipped ? size.height / 2 - 1 : 0);
        int pointerV = yuv.getOffsetV() + uvstride * (flipped ? size.height / 2 - 1 : 0);
        int pRGB = 0;
        int rgbscan = rgb.getLineStride();
        int pixstride = rgb.getPixelStride();
        int rOffset = rgb.getRedMask() - 1;
        int gOffset = rgb.getGreenMask() - 1;
        int bOffset = rgb.getBlueMask() - 1;
        for(int y = 0; y < size.height; y += 2)
        {
            for(int x = 0; x < size.width; x += 2)
            {
                int rval = inData[pRGB + rOffset] & 0xff;
                int gval = inData[pRGB + gOffset] & 0xff;
                int bval = inData[pRGB + bOffset] & 0xff;
                int yval = (rval * 257 + gval * 504 + bval * 98) / 1000 + 16;
                int uval = ((-rval * 148 - gval * 291) + bval * 439) / 1000 + 128;
                int vval = (rval * 439 - gval * 368 - bval * 71) / 1000 + 128;
                outData[pointerY] = (byte)(yval & 0xff);
                rval = inData[pRGB + rOffset + pixstride] & 0xff;
                gval = inData[pRGB + gOffset + pixstride] & 0xff;
                bval = inData[pRGB + bOffset + pixstride] & 0xff;
                yval = (rval * 257 + gval * 504 + bval * 98) / 1000 + 16;
                uval += ((-rval * 148 - gval * 291) + bval * 439) / 1000 + 128;
                vval += (rval * 439 - gval * 368 - bval * 71) / 1000 + 128;
                outData[pointerY + 1] = (byte)(yval & 0xff);
                rval = inData[pRGB + rOffset + rgbscan] & 0xff;
                gval = inData[pRGB + gOffset + rgbscan] & 0xff;
                bval = inData[pRGB + bOffset + rgbscan] & 0xff;
                yval = (rval * 257 + gval * 504 + bval * 98) / 1000 + 16;
                uval += ((-rval * 148 - gval * 291) + bval * 439) / 1000 + 128;
                vval += (rval * 439 - gval * 368 - bval * 71) / 1000 + 128;
                outData[pointerY + increment * ystride] = (byte)(yval & 0xff);
                rval = inData[pRGB + rOffset + rgbscan + pixstride] & 0xff;
                gval = inData[pRGB + gOffset + rgbscan + pixstride] & 0xff;
                bval = inData[pRGB + bOffset + rgbscan + pixstride] & 0xff;
                yval = (rval * 257 + gval * 504 + bval * 98) / 1000 + 16;
                uval += ((-rval * 148 - gval * 291) + bval * 439) / 1000 + 128;
                vval += (rval * 439 - gval * 368 - bval * 71) / 1000 + 128;
                outData[pointerY + increment * ystride + 1] = (byte)(yval & 0xff);
                outData[pointerU] = (byte)(uval >> 2 & 0xff);
                outData[pointerV] = (byte)(vval >> 2 & 0xff);
                pointerY += 2;
                pointerU++;
                pointerV++;
                pRGB += pixstride * 2;
            }

            pRGB += rgbscan * 2 - size.width * pixstride;
            pointerY += increment * ystride * 2 - size.width;
            pointerU += increment * uvstride - size.width / 2;
            pointerV += increment * uvstride - size.width / 2;
        }

        return 0;
    }

    protected int convertInt(Buffer inBuffer, Buffer outBuffer)
    {
        RGBFormat rgb = (RGBFormat)inBuffer.getFormat();
        YUVFormat yuv = (YUVFormat)outBuffer.getFormat();
        Dimension size = rgb.getSize();
        byte outData[] = (byte[])outBuffer.getData();
        int inData[] = (int[])inBuffer.getData();
        boolean flipped = rgb.getFlipped() == 1;
        int increment = flipped ? -1 : 1;
        int ystride = yuv.getStrideY();
        int uvstride = yuv.getStrideUV();
        int pointerY = yuv.getOffsetY() + ystride * (flipped ? size.height - 1 : 0);
        int pointerU = yuv.getOffsetU() + uvstride * (flipped ? size.height / 2 - 1 : 0);
        int pointerV = yuv.getOffsetV() + uvstride * (flipped ? size.height / 2 - 1 : 0);
        int pRGB = 0;
        int rgbscan = rgb.getLineStride();
        int rOffset = 16;
        int gOffset = 8;
        int bOffset = 0;
        if(rgb.getRedMask() == 255)
        {
            rOffset = 0;
            bOffset = 16;
        }
        for(int y = 0; y < size.height; y += 2)
        {
            for(int x = 0; x < size.width; x += 2)
            {
                int rval = inData[pRGB] >> rOffset & 0xff;
                int gval = inData[pRGB] >> gOffset & 0xff;
                int bval = inData[pRGB] >> bOffset & 0xff;
                int yval = (rval * 257 + gval * 504 + bval * 98) / 1000 + 16;
                int uval = ((-rval * 148 - gval * 291) + bval * 439) / 1000 + 128;
                int vval = (rval * 439 - gval * 368 - bval * 71) / 1000 + 128;
                outData[pointerY] = (byte)(yval & 0xff);
                rval = inData[pRGB + 1] >> rOffset & 0xff;
                gval = inData[pRGB + 1] >> gOffset & 0xff;
                bval = inData[pRGB + 1] >> bOffset & 0xff;
                yval = (rval * 257 + gval * 504 + bval * 98) / 1000 + 16;
                uval += ((-rval * 148 - gval * 291) + bval * 439) / 1000 + 128;
                vval += (rval * 439 - gval * 368 - bval * 71) / 1000 + 128;
                outData[pointerY + 1] = (byte)(yval & 0xff);
                rval = inData[pRGB + rgbscan] >> rOffset & 0xff;
                gval = inData[pRGB + rgbscan] >> gOffset & 0xff;
                bval = inData[pRGB + rgbscan] >> bOffset & 0xff;
                yval = (rval * 257 + gval * 504 + bval * 98) / 1000 + 16;
                uval += ((-rval * 148 - gval * 291) + bval * 439) / 1000 + 128;
                vval += (rval * 439 - gval * 368 - bval * 71) / 1000 + 128;
                outData[pointerY + increment * ystride] = (byte)(yval & 0xff);
                rval = inData[pRGB + rgbscan + 1] >> rOffset & 0xff;
                gval = inData[pRGB + rgbscan + 1] >> gOffset & 0xff;
                bval = inData[pRGB + rgbscan + 1] >> bOffset & 0xff;
                yval = (rval * 257 + gval * 504 + bval * 98) / 1000 + 16;
                uval += ((-rval * 148 - gval * 291) + bval * 439) / 1000 + 128;
                vval += (rval * 439 - gval * 368 - bval * 71) / 1000 + 128;
                outData[pointerY + increment * ystride + 1] = (byte)(yval & 0xff);
                outData[pointerU] = (byte)(uval >> 2 & 0xff);
                outData[pointerV] = (byte)(vval >> 2 & 0xff);
                pointerY += 2;
                pointerU++;
                pointerV++;
                pRGB += 2;
            }

            pRGB += rgbscan * 2 - size.width;
            pointerY += increment * ystride * 2 - size.width;
            pointerU += increment * uvstride - size.width / 2;
            pointerV += increment * uvstride - size.width / 2;
        }

        return 0;
    }

    public void reset()
    {
    }

    private static final String PLUGIN_NAME = "RGB To YUV";
    private FrameProcessingControl frameControl;
    private boolean dropFrame;


}
