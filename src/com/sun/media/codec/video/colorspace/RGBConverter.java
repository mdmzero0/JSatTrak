// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RGBConverter.java

package com.sun.media.codec.video.colorspace;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import java.awt.Component;
import java.awt.Dimension;
import javax.media.*;
import javax.media.control.FrameProcessingControl;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

public abstract class RGBConverter extends BasicCodec
{

    public RGBConverter()
    {
        frameControl = null;
        super.inputFormats = (new Format[] {
            new RGBFormat()
        });
        super.outputFormats = (new Format[] {
            new RGBFormat()
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
        return "RGB To RGB Converter";
    }

    public Format[] getSupportedOutputFormats(Format input)
    {
        if(input == null)
            return super.outputFormats;
        if(input instanceof RGBFormat)
        {
            RGBFormat rgb = (RGBFormat)input;
            Dimension size = rgb.getSize();
            float frameRate = rgb.getFrameRate();
            int bpp = rgb.getBitsPerPixel();
            RGBFormat bits_16_p = new RGBFormat(size, size.width * size.height, Format.shortArray, frameRate, 16, -1, -1, -1, 1, size.width, -1, -1);
            RGBFormat bits_16_up = new RGBFormat(size, size.width * size.height * 2, Format.byteArray, frameRate, 16, -1, -1, -1, 2, size.width * 2, -1, -1);
            RGBFormat masks_565 = new RGBFormat(null, -1, null, -1F, -1, 63488, 2016, 31, -1, -1, -1, -1);
            RGBFormat masks_555 = new RGBFormat(null, -1, null, -1F, -1, 31744, 992, 31, -1, -1, -1, -1);
            RGBFormat bits_24_up = new RGBFormat(size, size.width * size.height * 3, Format.byteArray, frameRate, 24, -1, -1, -1, 3, size.width * 3, -1, -1);
            RGBFormat masks_RGB = new RGBFormat(null, -1, null, -1F, -1, 1, 2, 3, -1, -1, -1, -1);
            RGBFormat masks_BGR = new RGBFormat(null, -1, null, -1F, -1, 3, 2, 1, -1, -1, -1, -1);
            RGBFormat bits_32_p = new RGBFormat(size, size.width * size.height, Format.intArray, frameRate, 32, -1, -1, -1, 1, size.width, -1, -1);
            RGBFormat bits_32_up = new RGBFormat(size, size.width * size.height * 4, Format.byteArray, frameRate, 32, -1, -1, -1, 4, size.width * 4, -1, -1);
            RGBFormat masks_234 = new RGBFormat(null, -1, null, -1F, -1, 2, 3, 4, -1, -1, -1, -1);
            RGBFormat masks_432 = new RGBFormat(null, -1, null, -1F, -1, 4, 3, 2, -1, -1, -1, -1);
            RGBFormat masks_123 = new RGBFormat(null, -1, null, -1F, -1, 1, 2, 3, -1, -1, -1, -1);
            RGBFormat flipped = new RGBFormat(null, -1, null, -1F, -1, -1, -1, -1, -1, -1, 1, -1);
            RGBFormat straight = new RGBFormat(null, -1, null, -1F, -1, -1, -1, -1, -1, -1, 0, -1);
            RGBFormat big = new RGBFormat(null, -1, null, -1F, -1, -1, -1, -1, -1, -1, -1, 0);
            RGBFormat little = new RGBFormat(null, -1, null, -1F, -1, -1, -1, -1, -1, -1, -1, 1);
            RGBFormat masks_321 = new RGBFormat(null, -1, null, -1F, -1, 3, 2, 1, -1, -1, -1, -1);
            RGBFormat masks_XRGB = new RGBFormat(null, -1, null, -1F, -1, 0xff0000, 65280, 255, -1, -1, -1, -1);
            RGBFormat masks_XBGR = new RGBFormat(null, -1, null, -1F, -1, 255, 65280, 0xff0000, -1, -1, -1, -1);
            Format out[] = {
                bits_16_p.intersects(masks_565).intersects(flipped), bits_16_p.intersects(masks_565).intersects(straight), bits_16_up.intersects(masks_565).intersects(flipped).intersects(little), bits_16_up.intersects(masks_565).intersects(flipped).intersects(big), bits_16_up.intersects(masks_565).intersects(straight).intersects(little), bits_16_up.intersects(masks_565).intersects(straight).intersects(big), bits_16_p.intersects(masks_555).intersects(flipped), bits_16_p.intersects(masks_555).intersects(straight), bits_16_up.intersects(masks_555).intersects(flipped).intersects(little), bits_16_up.intersects(masks_555).intersects(flipped).intersects(big), 
                bits_16_up.intersects(masks_555).intersects(straight).intersects(little), bits_16_up.intersects(masks_555).intersects(straight).intersects(big), bits_24_up.intersects(masks_RGB).intersects(flipped), bits_24_up.intersects(masks_RGB).intersects(straight), bits_24_up.intersects(masks_BGR).intersects(flipped), bits_24_up.intersects(masks_BGR).intersects(straight), bits_32_p.intersects(masks_XRGB).intersects(flipped), bits_32_p.intersects(masks_XRGB).intersects(straight), bits_32_p.intersects(masks_XBGR).intersects(flipped), bits_32_p.intersects(masks_XBGR).intersects(straight), 
                bits_32_up.intersects(masks_123).intersects(flipped), bits_32_up.intersects(masks_123).intersects(straight), bits_32_up.intersects(masks_321).intersects(flipped), bits_32_up.intersects(masks_321).intersects(straight), bits_32_up.intersects(masks_234).intersects(flipped), bits_32_up.intersects(masks_234).intersects(straight), bits_32_up.intersects(masks_432).intersects(flipped), bits_32_up.intersects(masks_432).intersects(straight)
            };
            return out;
        } else
        {
            return null;
        }
    }

    protected abstract void sixteenToSixteen(Object obj, int i, int j, int k, int l, int i1, int j1, 
            boolean flag, int k1, Object obj1, int l1, int i2, int j2, int k2, 
            int l2, int i3, boolean flag1, int j3, int k3, int l3, boolean flag2);

    protected abstract void sixteenToComponent(Object obj, int i, int j, int k, int l, int i1, int j1, 
            boolean flag, int k1, Object obj1, int l1, int i2, int j2, int k2, 
            int l2, int i3, boolean flag1, int j3, int k3, int l3, boolean flag2);

    protected abstract void componentToSixteen(Object obj, int i, int j, int k, int l, int i1, int j1, 
            boolean flag, int k1, Object obj1, int l1, int i2, int j2, int k2, 
            int l2, int i3, boolean flag1, int j3, int k3, int l3, boolean flag2);

    protected abstract void componentToComponent(Object obj, int i, int j, int k, int l, int i1, int j1, 
            boolean flag, int k1, Object obj1, int l1, int i2, int j2, int k2, 
            int l2, int i3, boolean flag1, int j3, int k3, int l3, boolean flag2);

    public Format setInputFormat(Format in)
    {
        Format returnFormat = super.setInputFormat(in);
        if(returnFormat == null)
            return null;
        if(((RGBFormat)returnFormat).getBitsPerPixel() < 15)
            return null;
        Dimension size = ((VideoFormat)in).getSize();
        if(super.opened)
            super.outputFormat = updateRGBFormat((VideoFormat)in, (RGBFormat)super.outputFormat);
        return returnFormat;
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
        RGBFormat inputRGB = (RGBFormat)super.inputFormat;
        RGBFormat outputRGB = (RGBFormat)super.outputFormat;
        Object inObject = inBuffer.getData();
        Object outObject = outBuffer.getData();
        if(inObject.getClass() != super.inputFormat.getDataType())
            return 1;
        int outMaxDataLen = outputRGB.getMaxDataLength();
        int outLength = 0;
        if(outObject != null)
            if(outObject.getClass() == Format.byteArray)
                outLength = ((byte[])outObject).length;
            else
            if(outObject.getClass() == Format.shortArray)
                outLength = ((short[])outObject).length;
            else
            if(outObject.getClass() == Format.intArray)
                outLength = ((int[])outObject).length;
        if(outObject == null || outLength < outMaxDataLen || super.outputFormat != outBuffer.getFormat() || !super.outputFormat.equals(outBuffer.getFormat()))
        {
            Class outputDataType = super.outputFormat.getDataType();
            if(outputDataType == Format.byteArray)
                outObject = new byte[outputRGB.getMaxDataLength()];
            else
            if(outputDataType == Format.shortArray)
                outObject = new short[outputRGB.getMaxDataLength()];
            else
            if(outputDataType == Format.intArray)
                outObject = new int[outputRGB.getMaxDataLength()];
            else
                return 1;
            outBuffer.setData(outObject);
        }
        if(outObject.getClass() != super.outputFormat.getDataType())
            return 1;
        int inBPP = inputRGB.getBitsPerPixel();
        int outBPP = outputRGB.getBitsPerPixel();
        boolean inPacked = inputRGB.getDataType() != Format.byteArray;
        boolean outPacked = outputRGB.getDataType() != Format.byteArray;
        int inPS = inputRGB.getPixelStride();
        int outPS = outputRGB.getPixelStride();
        int inEndian = inputRGB.getEndian();
        int outEndian = outputRGB.getEndian();
        int inRed = inputRGB.getRedMask();
        int inGreen = inputRGB.getGreenMask();
        int inBlue = inputRGB.getBlueMask();
        int outRed = outputRGB.getRedMask();
        int outGreen = outputRGB.getGreenMask();
        int outBlue = outputRGB.getBlueMask();
        int inLS = inputRGB.getLineStride();
        int outLS = outputRGB.getLineStride();
        boolean flip = inputRGB.getFlipped() != outputRGB.getFlipped();
        Dimension size = inputRGB.getSize();
        int width = size.width;
        int height = size.height;
        if(inBPP == 16 && outBPP == 16)
            sixteenToSixteen(inObject, inPS, inLS, inBPP, inRed, inGreen, inBlue, inPacked, inEndian, outObject, outPS, outLS, outBPP, outRed, outGreen, outBlue, outPacked, outEndian, width, height, flip);
        else
        if(inBPP == 16 && outBPP >= 24)
            sixteenToComponent(inObject, inPS, inLS, inBPP, inRed, inGreen, inBlue, inPacked, inEndian, outObject, outPS, outLS, outBPP, outRed, outGreen, outBlue, outPacked, outEndian, width, height, flip);
        else
        if(inBPP >= 24 && outBPP == 16)
            componentToSixteen(inObject, inPS, inLS, inBPP, inRed, inGreen, inBlue, inPacked, inEndian, outObject, outPS, outLS, outBPP, outRed, outGreen, outBlue, outPacked, outEndian, width, height, flip);
        else
        if(inBPP >= 24 && outBPP >= 24)
            componentToComponent(inObject, inPS, inLS, inBPP, inRed, inGreen, inBlue, inPacked, inEndian, outObject, outPS, outLS, outBPP, outRed, outGreen, outBlue, outPacked, outEndian, width, height, flip);
        outBuffer.setFormat(super.outputFormat);
        outBuffer.setLength(outputRGB.getMaxDataLength());
        return 0;
    }

    public void open()
        throws ResourceUnavailableException
    {
        super.open();
    }

    public void close()
    {
        super.close();
    }

    public void reset()
    {
    }

    protected int getShift(int mask)
    {
        int shift;
        for(shift = 0; (mask & 1) == 0; shift++)
            mask >>= 1;

        return shift;
    }

    private static final String PLUGIN_NAME = "RGB To RGB Converter";
    private FrameProcessingControl frameControl;
    private boolean dropFrame;


}
