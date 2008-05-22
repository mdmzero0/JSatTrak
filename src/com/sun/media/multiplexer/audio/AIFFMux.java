// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AIFFMux.java

package com.sun.media.multiplexer.audio;

import com.sun.media.multiplexer.BasicMux;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

public class AIFFMux extends BasicMux
{

    public AIFFMux()
    {
        blockAlign = 1;
        maxFrames = 0;
        maxFramesOffset = 0;
        headerSize = 0;
        dataSize = 0;
        bigEndian = new AudioFormat(null, -1D, -1, -1, 1, 1);
        super.supportedInputs = new Format[1];
        super.supportedInputs[0] = new AudioFormat(null);
        super.supportedOutputs = new ContentDescriptor[1];
        super.supportedOutputs[0] = new FileTypeDescriptor("audio.x_aiff");
    }

    public String getName()
    {
        return "AIFF Audio Multiplexer";
    }

    public int setNumTracks(int nTracks)
    {
        if(nTracks != 1)
            return 1;
        else
            return super.setNumTracks(nTracks);
    }

    protected void writeHeader()
    {
        bufClear();
        bufWriteBytes("FORM");
        bufWriteInt(0);
        bufWriteBytes(formType);
        if(formType.equals("AIFC"))
        {
            bufWriteBytes("FVER");
            bufWriteInt(4);
            bufWriteInt(AIFCVersion1);
        }
        bufWriteBytes("COMM");
        int commonIDSize = 18;
        if(formType.equals("AIFC"))
            commonIDSize += 8;
        bufWriteInt(commonIDSize);
        bufWriteShort((short)channels);
        maxFramesOffset = super.filePointer;
        bufWriteInt(maxFrames);
        bufWriteShort((short)sampleSizeInBits);
        int exponent = 16398;
        double highMantissa;
        for(highMantissa = sampleRate; highMantissa < 44000D;)
        {
            highMantissa *= 2D;
            exponent--;
        }

        bufWriteShort((short)exponent);
        bufWriteInt((int)highMantissa << 16);
        bufWriteInt(0);
        if(formType.equals("AIFC"))
        {
            bufWriteBytes(aiffEncoding);
            bufWriteBytes(aiffEncoding);
        }
        bufWriteBytes("SSND");
        dataSizeOffset = super.filePointer;
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0);
        bufFlush();
        headerSize = super.filePointer;
    }

    public Format setInputFormat(Format format, int trackID)
    {
        String reason = null;
        if(!(format instanceof AudioFormat))
            return null;
        audioFormat = (AudioFormat)format;
        String encodingString = audioFormat.getEncoding();
        sampleSizeInBits = audioFormat.getSampleSizeInBits();
        sampleRate = audioFormat.getSampleRate();
        channels = audioFormat.getChannels();
        blockAlign = (channels * sampleSizeInBits) / 8;
        if(encodingString.equalsIgnoreCase("LINEAR"))
        {
            if(sampleSizeInBits > 8 && audioFormat.getEndian() == 0)
                return null;
            if(audioFormat.getSigned() == 0)
                return null;
            if(audioFormat.getEndian() == -1 || audioFormat.getSigned() == -1)
                format = audioFormat.intersects(bigEndian);
            formType = "AIFF";
            aiffEncoding = "NONE";
        } else
        {
            formType = "AIFC";
            if(encodingString.equalsIgnoreCase("ULAW"))
                aiffEncoding = "ulaw";
            else
            if(encodingString.equalsIgnoreCase("alaw"))
                aiffEncoding = "alaw";
            else
            if(encodingString.equalsIgnoreCase("ima4"))
            {
                aiffEncoding = "ima4";
                blockAlign = 34 * channels;
            } else
            if(encodingString.equalsIgnoreCase("MAC3"))
            {
                aiffEncoding = encodingString;
                blockAlign = 2;
            } else
            if(encodingString.equalsIgnoreCase("MAC6"))
            {
                aiffEncoding = encodingString;
                blockAlign = 1;
            } else
            {
                reason = "Cannot handle encoding " + encodingString;
            }
        }
        if(reason == null)
        {
            super.inputs[0] = format;
            return format;
        } else
        {
            return null;
        }
    }

    protected void writeFooter()
    {
        byte dummy[] = {
            0
        };
        dataSize = super.filePointer - headerSize;
        if((super.filePointer & 1) != 0)
            write(dummy, 0, 1);
        bufClear();
        seek(4);
        bufWriteInt(super.fileSize);
        bufFlush();
        bufClear();
        seek(maxFramesOffset);
        maxFrames = dataSize / blockAlign;
        bufWriteInt(maxFrames);
        bufFlush();
        bufClear();
        seek(dataSizeOffset);
        bufWriteInt(dataSize + 8);
        bufFlush();
    }

    private Format format;
    private AudioFormat audioFormat;
    private int sampleSizeInBits;
    private double sampleRate;
    private int channels;
    private int blockAlign;
    private int dataSizeOffset;
    private int maxFrames;
    private int maxFramesOffset;
    private String formType;
    private String aiffEncoding;
    private int headerSize;
    private int dataSize;
    private static int AIFCVersion1 = 0xa2805140;
    private static final String FormID = "FORM";
    private static final String FormatVersionID = "FVER";
    private static final String CommonID = "COMM";
    private static final String SoundDataID = "SSND";
    private static final int CommonIDSize = 18;
    Format bigEndian;

}
