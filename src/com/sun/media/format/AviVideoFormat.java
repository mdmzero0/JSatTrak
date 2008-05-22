// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AviVideoFormat.java

package com.sun.media.format;

import java.awt.Dimension;
import javax.media.Format;
import javax.media.format.VideoFormat;

public class AviVideoFormat extends VideoFormat
{

    public AviVideoFormat(String encoding)
    {
        super(encoding);
        planes = -1;
        bitsPerPixel = -1;
        imageSize = -1;
        xPelsPerMeter = -1;
        yPelsPerMeter = -1;
        clrUsed = -1;
        clrImportant = -1;
        codecSpecificHeader = null;
    }

    public AviVideoFormat(String encoding, Dimension size, int maxDataLength, Class dataType, float frameRate, int planes, int bitsPerPixel, 
            int imageSize, int xPelsPerMeter, int yPelsPerMeter, int clrUsed, int clrImportant, byte codecHeader[])
    {
        super(encoding, size, maxDataLength, dataType, frameRate);
        this.planes = -1;
        this.bitsPerPixel = -1;
        this.imageSize = -1;
        this.xPelsPerMeter = -1;
        this.yPelsPerMeter = -1;
        this.clrUsed = -1;
        this.clrImportant = -1;
        codecSpecificHeader = null;
        this.planes = planes;
        this.bitsPerPixel = bitsPerPixel;
        this.imageSize = imageSize;
        this.xPelsPerMeter = xPelsPerMeter;
        this.yPelsPerMeter = yPelsPerMeter;
        this.clrUsed = clrUsed;
        this.clrImportant = clrImportant;
        codecSpecificHeader = codecHeader;
    }

    public int getPlanes()
    {
        return planes;
    }

    public int getBitsPerPixel()
    {
        return bitsPerPixel;
    }

    public int getImageSize()
    {
        return imageSize;
    }

    public int getXPelsPerMeter()
    {
        return xPelsPerMeter;
    }

    public int getYPelsPerMeter()
    {
        return yPelsPerMeter;
    }

    public int getClrUsed()
    {
        return clrUsed;
    }

    public int getClrImportant()
    {
        return clrImportant;
    }

    public byte[] getCodecSpecificHeader()
    {
        return codecSpecificHeader;
    }

    public Object clone()
    {
        AviVideoFormat f = new AviVideoFormat(super.encoding);
        f.copy(this);
        return f;
    }

    protected void copy(Format f)
    {
        super.copy(f);
        if(f instanceof AviVideoFormat)
        {
            AviVideoFormat other = (AviVideoFormat)f;
            planes = other.planes;
            bitsPerPixel = other.bitsPerPixel;
            imageSize = other.imageSize;
            xPelsPerMeter = other.xPelsPerMeter;
            yPelsPerMeter = other.yPelsPerMeter;
            clrUsed = other.clrUsed;
            clrImportant = other.clrImportant;
            codecSpecificHeader = other.codecSpecificHeader;
        }
    }

    public boolean equals(Object format)
    {
        if(format instanceof AviVideoFormat)
        {
            AviVideoFormat other = (AviVideoFormat)format;
            boolean result = super.equals(format) && planes == other.planes && bitsPerPixel == other.bitsPerPixel && imageSize == other.imageSize && xPelsPerMeter == other.xPelsPerMeter && yPelsPerMeter == other.yPelsPerMeter && clrUsed == other.clrUsed && clrImportant == other.clrImportant;
            if(!result)
                return false;
            if(codecSpecificHeader == other.codecSpecificHeader)
                return true;
            if(codecSpecificHeader == null || other.codecSpecificHeader == null)
                return false;
            if(codecSpecificHeader.length != other.codecSpecificHeader.length)
                return false;
            for(int i = 0; i < codecSpecificHeader.length; i++)
                if(codecSpecificHeader[i] != other.codecSpecificHeader[i])
                    return false;

            return true;
        } else
        {
            return false;
        }
    }

    public boolean matches(Format format)
    {
        if(!super.matches(format))
            return false;
        if(!(format instanceof AviVideoFormat))
        {
            return true;
        } else
        {
            AviVideoFormat other = (AviVideoFormat)format;
            boolean returnVal = (planes == -1 || other.planes == -1 || planes == other.planes) && (bitsPerPixel == -1 || other.bitsPerPixel == -1 || bitsPerPixel == other.bitsPerPixel) && (imageSize == -1 || other.imageSize == -1 || imageSize == other.imageSize) && (xPelsPerMeter == -1 || other.xPelsPerMeter == -1 || xPelsPerMeter == other.xPelsPerMeter) && (yPelsPerMeter == -1 || other.yPelsPerMeter == -1 || yPelsPerMeter == other.yPelsPerMeter) && (clrUsed == -1 || other.clrUsed == -1 || clrUsed == other.clrUsed) && (clrImportant == -1 || other.clrImportant == -1 || clrImportant == other.clrImportant) && (codecSpecificHeader == null || other.codecSpecificHeader == null || codecSpecificHeader == other.codecSpecificHeader || codecSpecificHeader.equals(codecSpecificHeader));
            return returnVal;
        }
    }

    public Format intersects(Format format)
    {
        Format fmt;
        if((fmt = super.intersects(format)) == null)
            return null;
        if(!(format instanceof AviVideoFormat))
        {
            return fmt;
        } else
        {
            AviVideoFormat other = (AviVideoFormat)format;
            AviVideoFormat res = (AviVideoFormat)fmt;
            res.planes = planes == -1 ? other.planes : planes;
            res.bitsPerPixel = bitsPerPixel == -1 ? other.bitsPerPixel : bitsPerPixel;
            res.imageSize = imageSize == -1 ? other.imageSize : imageSize;
            res.xPelsPerMeter = xPelsPerMeter == -1 ? other.xPelsPerMeter : xPelsPerMeter;
            res.yPelsPerMeter = yPelsPerMeter == -1 ? other.yPelsPerMeter : yPelsPerMeter;
            res.clrUsed = clrUsed == -1 ? other.clrUsed : clrUsed;
            res.clrImportant = clrImportant == -1 ? other.clrImportant : clrImportant;
            res.codecSpecificHeader = codecSpecificHeader == null ? other.codecSpecificHeader : codecSpecificHeader;
            return res;
        }
    }

    public Format relax()
    {
        AviVideoFormat fmt;
        if((fmt = (AviVideoFormat)super.relax()) == null)
        {
            return null;
        } else
        {
            fmt.imageSize = -1;
            return fmt;
        }
    }

    public String toString()
    {
        String s = super.toString() + " " + (codecSpecificHeader == null ? 0 : codecSpecificHeader.length) + " extra bytes";
        return s;
    }

    protected int planes;
    protected int bitsPerPixel;
    protected int imageSize;
    protected int xPelsPerMeter;
    protected int yPelsPerMeter;
    protected int clrUsed;
    protected int clrImportant;
    protected byte codecSpecificHeader[];
}
