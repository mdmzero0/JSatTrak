// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BitMapInfo.java

package com.sun.media.vfw;

import com.sun.media.format.AviVideoFormat;
import java.awt.Dimension;
import javax.media.Format;
import javax.media.format.*;

public class BitMapInfo
{

    public BitMapInfo()
    {
        biWidth = 0;
        biHeight = 0;
        biPlanes = 1;
        biBitCount = 24;
        fourcc = "";
        biSizeImage = 0;
        biXPelsPerMeter = 0;
        biYPelsPerMeter = 0;
        biClrUsed = 0;
        biClrImportant = 0;
        extraSize = 0;
        extraBytes = null;
    }

    public BitMapInfo(String fourcc, int width, int height)
    {
        biWidth = 0;
        biHeight = 0;
        biPlanes = 1;
        biBitCount = 24;
        this.fourcc = "";
        biSizeImage = 0;
        biXPelsPerMeter = 0;
        biYPelsPerMeter = 0;
        biClrUsed = 0;
        biClrImportant = 0;
        extraSize = 0;
        extraBytes = null;
        biWidth = width;
        biHeight = height;
        this.fourcc = fourcc;
        if(fourcc.equals("RGB"))
            biSizeImage = width * height * 3;
        if(fourcc.equals("MSVC"))
            this.fourcc = "CRAM";
    }

    public BitMapInfo(String fourcc, int width, int height, int planes, int bitcount, int sizeImage, int clrused, 
            int clrimportant)
    {
        this(fourcc, width, height);
        biPlanes = planes;
        biBitCount = bitcount;
        biSizeImage = sizeImage;
        biClrUsed = clrused;
        biClrImportant = clrimportant;
    }

    public BitMapInfo(VideoFormat format)
    {
        biWidth = 0;
        biHeight = 0;
        biPlanes = 1;
        biBitCount = 24;
        fourcc = "";
        biSizeImage = 0;
        biXPelsPerMeter = 0;
        biYPelsPerMeter = 0;
        biClrUsed = 0;
        biClrImportant = 0;
        extraSize = 0;
        extraBytes = null;
        Dimension size = format.getSize();
        if(size == null)
            size = new Dimension(320, 240);
        Class arrayType = format.getDataType();
        int elSize = arrayType != Format.byteArray ? ((int) (arrayType != Format.intArray ? 2 : 4)) : 1;
        biWidth = size.width;
        biHeight = size.height;
        biPlanes = 1;
        biSizeImage = format.getMaxDataLength() * elSize;
        fourcc = format.getEncoding();
        if(fourcc.equalsIgnoreCase("msvc"))
            fourcc = "CRAM";
        if(format instanceof AviVideoFormat)
        {
            AviVideoFormat avif = (AviVideoFormat)format;
            biPlanes = avif.getPlanes();
            biBitCount = avif.getBitsPerPixel();
            biXPelsPerMeter = avif.getXPelsPerMeter();
            biYPelsPerMeter = avif.getYPelsPerMeter();
            biClrUsed = avif.getClrUsed();
            biClrImportant = avif.getClrImportant();
            extraBytes = avif.getCodecSpecificHeader();
            if(extraBytes != null)
                extraSize = extraBytes.length;
        } else
        if(format instanceof RGBFormat)
        {
            RGBFormat rgb = (RGBFormat)format;
            fourcc = "RGB";
            biBitCount = rgb.getBitsPerPixel();
            if(rgb.getFlipped() == 0)
                biHeight = -biHeight;
        } else
        if(format instanceof YUVFormat)
        {
            YUVFormat yuv = (YUVFormat)format;
            switch(yuv.getYuvType())
            {
            case 2: // '\002'
                if(yuv.getOffsetU() < yuv.getOffsetV())
                    fourcc = "I420";
                else
                    fourcc = "YV12";
                // fall through

            case 32: // ' '
                if(yuv.getOffsetY() == 0 && yuv.getOffsetU() == 1)
                    fourcc = "YUY2";
                else
                if(yuv.getOffsetY() == 0 && yuv.getOffsetU() == 3)
                    fourcc = "YVYU";
                else
                if(yuv.getOffsetU() == 0)
                    fourcc = "UYVY";
                break;
            }
            if(fourcc.equalsIgnoreCase("yv12") || fourcc.equalsIgnoreCase("i420") || fourcc.equalsIgnoreCase("y411"))
                biBitCount = 12;
            else
            if(fourcc.equalsIgnoreCase("yuy2"))
                biBitCount = 16;
        }
    }

    public VideoFormat createVideoFormat(Class arrayType)
    {
        return createVideoFormat(arrayType, -1F);
    }

    public VideoFormat createVideoFormat(Class arrayType, float frameRate)
    {
        VideoFormat format;
        if(fourcc.equalsIgnoreCase("rgb"))
        {
            int elSize = arrayType != Format.byteArray ? ((int) (arrayType != Format.intArray ? 2 : 4)) : 1;
            int maxDataLength = biSizeImage / elSize;
            int rm = -1;
            int gm = -1;
            int bm = -1;
            if(biBitCount == 16)
            {
                rm = 31744;
                gm = 992;
                bm = 31;
            } else
            if(biBitCount == 32)
            {
                if(elSize == 4)
                {
                    rm = 0xff0000;
                    gm = 65280;
                    bm = 255;
                } else
                {
                    rm = 3;
                    gm = 2;
                    bm = 1;
                }
            } else
            if(biBitCount == 24)
            {
                rm = 3;
                gm = 2;
                bm = 1;
            }
            int bytesPerLine = (biWidth * biBitCount) / 8;
            int lineStride = bytesPerLine / elSize;
            int pixelStride = lineStride / biWidth;
            int actualHeight = biHeight;
            int flipped = 1;
            if(biHeight < 0)
            {
                actualHeight = -actualHeight;
                flipped = 0;
            }
            format = new RGBFormat(new Dimension(biWidth, actualHeight), maxDataLength, arrayType, frameRate, biBitCount, rm, gm, bm, pixelStride, lineStride, flipped, 1);
        } else
        if(fourcc.equalsIgnoreCase("yuy2"))
        {
            int ySize = biWidth * biHeight;
            format = new YUVFormat(new Dimension(biWidth, biHeight), biSizeImage, byte[].class, frameRate, 32, biWidth * 2, biWidth * 2, 0, 1, 3);
        } else
        if(fourcc.equalsIgnoreCase("i420"))
        {
            int ySize = biWidth * biHeight;
            format = new YUVFormat(new Dimension(biWidth, biHeight), biSizeImage, byte[].class, frameRate, 2, biWidth, biWidth / 2, 0, ySize, ySize + ySize / 4);
        } else
        if(fourcc.equalsIgnoreCase("yv12"))
        {
            int ySize = biWidth * biHeight;
            format = new YUVFormat(new Dimension(biWidth, biHeight), biSizeImage, byte[].class, frameRate, 2, biWidth, biWidth / 2, 0, ySize + ySize / 4, ySize);
        } else
        {
            format = new AviVideoFormat(fourcc, new Dimension(biWidth, biHeight), biSizeImage, arrayType, frameRate, biPlanes, biBitCount, biSizeImage, biXPelsPerMeter, biYPelsPerMeter, biClrUsed, biClrImportant, extraBytes);
        }
        return format;
    }

    public String toString()
    {
        String s = "Size = " + biWidth + " x " + biHeight + "\t" + "Planes = " + biPlanes + "\t" + "BitCount = " + biBitCount + "\t" + "FourCC = " + fourcc + "\t" + "SizeImage = " + biSizeImage + "\n" + "ClrUsed = " + biClrUsed + "\n" + "ClrImportant = " + biClrImportant + "\n" + "ExtraSize = " + extraSize + "\n";
        if(extraSize > 0)
        {
            for(int i = 0; i < extraSize; i++)
                s = s + "\t" + i + " = " + extraBytes[i] + "\n";

        }
        return s;
    }

    public int biWidth;
    public int biHeight;
    public int biPlanes;
    public int biBitCount;
    public String fourcc;
    public int biSizeImage;
    public int biXPelsPerMeter;
    public int biYPelsPerMeter;
    public int biClrUsed;
    public int biClrImportant;
    public int extraSize;
    public byte extraBytes[];
}
