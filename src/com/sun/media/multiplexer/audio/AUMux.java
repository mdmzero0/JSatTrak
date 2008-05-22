// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AUMux.java

package com.sun.media.multiplexer.audio;

import com.sun.media.multiplexer.BasicMux;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

public class AUMux extends BasicMux
{

    public AUMux()
    {
        bigEndian = new AudioFormat(null, -1D, -1, -1, 1, 1);
        super.supportedInputs = new Format[1];
        super.supportedInputs[0] = new AudioFormat(null);
        super.supportedOutputs = new ContentDescriptor[1];
        super.supportedOutputs[0] = new FileTypeDescriptor("audio.basic");
    }

    public String getName()
    {
        return "Basic Audio Multiplexer";
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
        bufWriteInt(0x2e736e64);
        bufWriteInt(24);
        bufWriteInt(-1);
        bufWriteInt(encoding);
        bufWriteInt((int)sampleRate);
        bufWriteInt(channels);
        bufFlush();
    }

    public Format setInputFormat(Format format, int trackID)
    {
        String reason = null;
        if(!(format instanceof AudioFormat))
            return null;
        audioFormat = (AudioFormat)format;
        String encodingString = audioFormat.getEncoding();
        sampleSizeInBits = audioFormat.getSampleSizeInBits();
        if(encodingString.equalsIgnoreCase("LINEAR"))
        {
            if(sampleSizeInBits > 8 && audioFormat.getEndian() == 0)
                return null;
            if(audioFormat.getSigned() == 0)
                return null;
            if(audioFormat.getEndian() == -1 || audioFormat.getSigned() == -1)
                audioFormat = (AudioFormat)audioFormat.intersects(bigEndian);
        }
        encoding = getEncoding(encodingString, sampleSizeInBits);
        if(encoding == -1)
            reason = "No support for encoding " + encodingString;
        sampleRate = audioFormat.getSampleRate();
        channels = audioFormat.getChannels();
        if(reason == null)
            return audioFormat;
        else
            return null;
    }

    protected void writeFooter()
    {
        seek(8);
        bufClear();
        bufWriteInt(super.fileSize - 24);
        bufFlush();
    }

    private int getEncoding(String encodingString, int sampleSizeInBits)
    {
        if(encodingString.equalsIgnoreCase("ULAW"))
            return 1;
        if(encodingString.equalsIgnoreCase("alaw"))
            return 27;
        if(encodingString.equalsIgnoreCase("LINEAR"))
        {
            if(sampleSizeInBits == 8)
                return 2;
            if(sampleSizeInBits == 16)
                return 3;
            if(sampleSizeInBits == 24)
                return 4;
            return sampleSizeInBits != 32 ? -1 : 5;
        }
        if(encodingString.equalsIgnoreCase("float"))
            return 6;
        return !encodingString.equalsIgnoreCase("double") ? -1 : 7;
    }

    private static final int HEADER_SIZE = 24;
    private static final int UNKNOWN_ENCODING = -1;
    private AudioFormat audioFormat;
    private int sampleSizeInBits;
    private int encoding;
    private double sampleRate;
    private int channels;
    private static final int AU_SUN_MAGIC = 0x2e736e64;
    private static final int AU_ULAW_8 = 1;
    private static final int AU_ALAW_8 = 27;
    private static final int AU_LINEAR_8 = 2;
    private static final int AU_LINEAR_16 = 3;
    private static final int AU_LINEAR_24 = 4;
    private static final int AU_LINEAR_32 = 5;
    private static final int AU_FLOAT = 6;
    private static final int AU_DOUBLE = 7;
    Format bigEndian;
}
