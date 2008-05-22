// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   WAVMux.java

package com.sun.media.multiplexer.audio;

import com.sun.media.format.WavAudioFormat;
import com.sun.media.multiplexer.BasicMux;
import java.util.Hashtable;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

public class WAVMux extends BasicMux
{

    public WAVMux()
    {
        blockAlign = 0;
        bytesPerSecond = 0;
        factChunkLength = 0;
        signed = new AudioFormat(null, -1D, -1, -1, 0, 1);
        unsigned = new AudioFormat(null, -1D, -1, -1, 0, 0);
        super.supportedInputs = new Format[1];
        super.supportedInputs[0] = new AudioFormat(null);
        super.supportedOutputs = new ContentDescriptor[1];
        super.supportedOutputs[0] = new FileTypeDescriptor("audio.x_wav");
    }

    public String getName()
    {
        return "WAVE Audio Multiplexer";
    }

    public Format setInputFormat(Format input, int trackID)
    {
        if(!(input instanceof AudioFormat))
            return null;
        AudioFormat format = (AudioFormat)input;
        sampleRate = format.getSampleRate();
        String reason = null;
        audioFormat = format;
        if(format instanceof WavAudioFormat)
            wavAudioFormat = (WavAudioFormat)format;
        String encodingString = audioFormat.getEncoding();
        sampleSizeInBits = audioFormat.getSampleSizeInBits();
        if(encodingString.equalsIgnoreCase("LINEAR"))
            if(sampleSizeInBits > 8)
            {
                if(audioFormat.getEndian() == 1)
                    return null;
                if(audioFormat.getSigned() == 0)
                    return null;
                if(audioFormat.getEndian() == -1 || audioFormat.getSigned() == -1)
                    format = (AudioFormat)audioFormat.intersects(signed);
            } else
            {
                if(audioFormat.getSigned() == 1)
                    return null;
                if(audioFormat.getEndian() == -1 || audioFormat.getSigned() == -1)
                    format = (AudioFormat)audioFormat.intersects(unsigned);
            }
        Integer formatTag = (Integer)WavAudioFormat.reverseFormatMapper.get(encodingString.toLowerCase());
        if(formatTag == null)
        {
            reason = "Cannot handle format";
            return null;
        }
        wFormatTag = formatTag.shortValue();
        switch(wFormatTag)
        {
        case 2: // '\002'
        case 17: // '\021'
        case 49: // '1'
            if(wavAudioFormat == null)
                reason = "A WavAudioFormat is required  to provide encoding specific information for this encoding " + wFormatTag;
            break;
        }
        if(wavAudioFormat != null)
        {
            codecSpecificHeader = wavAudioFormat.getCodecSpecificHeader();
            bytesPerSecond = wavAudioFormat.getAverageBytesPerSecond();
        }
        sampleRate = audioFormat.getSampleRate();
        channels = audioFormat.getChannels();
        if(reason != null)
        {
            return null;
        } else
        {
            super.inputs[0] = format;
            return format;
        }
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
        int formatSize = 16;
        bufClear();
        audioFormat = (AudioFormat)super.inputs[0];
        codecSpecificHeader = null;
        if(audioFormat instanceof WavAudioFormat)
            codecSpecificHeader = ((WavAudioFormat)audioFormat).getCodecSpecificHeader();
        if(codecSpecificHeader != null)
            formatSize += 2 + codecSpecificHeader.length;
        else
        if(wFormatTag == 85)
            formatSize += 14;
        bufWriteBytes("RIFF");
        bufWriteInt(0);
        bufWriteBytes("WAVE");
        bufWriteBytes("fmt ");
        bufWriteIntLittleEndian(formatSize);
        int frameSizeInBits = audioFormat.getFrameSizeInBits();
        if(frameSizeInBits > 0)
            blockAlign = frameSizeInBits / 8;
        else
            blockAlign = (sampleSizeInBits / 8) * channels;
        bufWriteShortLittleEndian(wFormatTag);
        bufWriteShortLittleEndian((short)channels);
        bufWriteIntLittleEndian((int)sampleRate);
        int frameRate = -1;
        if(wFormatTag == 85)
        {
            blockAlign = 1;
            frameRate = (int)audioFormat.getFrameRate();
            if(frameRate != -1)
                bytesPerSecond = frameRate;
        }
        if(bytesPerSecond <= 0)
            bytesPerSecond = (channels * sampleSizeInBits * (int)sampleRate) / 8;
        bufWriteIntLittleEndian(bytesPerSecond);
        bufWriteShortLittleEndian((short)blockAlign);
        if(wFormatTag == 85)
            bufWriteShortLittleEndian((short)0);
        else
            bufWriteShortLittleEndian((short)sampleSizeInBits);
        if(codecSpecificHeader != null)
        {
            bufWriteShortLittleEndian((short)codecSpecificHeader.length);
            bufWriteBytes(codecSpecificHeader);
        } else
        if(wFormatTag == 85)
        {
            int blockSize;
            if(frameRate > 0)
            {
                float temp = (72F * (float)frameRate * 8F) / 8000F;
                temp = (float)((double)temp * (8000D / sampleRate));
                blockSize = (int)temp;
            } else
            {
                blockSize = 417;
            }
            bufWriteShortLittleEndian((short)12);
            bufWriteShortLittleEndian((short)1);
            bufWriteIntLittleEndian(2);
            bufWriteShortLittleEndian((short)blockSize);
            bufWriteShortLittleEndian((short)1);
            bufWriteShortLittleEndian((short)1393);
        }
        factChunkLength = 0;
        if(wFormatTag != 1)
        {
            bufWriteBytes("fact");
            bufWriteIntLittleEndian(4);
            numberOfSamplesOffset = super.filePointer;
            bufWriteInt(0);
            factChunkLength = 12;
        }
        bufWriteBytes("data");
        dataSizeOffset = super.filePointer;
        bufWriteInt(0);
        bufFlush();
    }

    protected void writeFooter()
    {
        seek(4);
        bufClear();
        bufWriteIntLittleEndian(super.fileSize - 8);
        bufFlush();
        seek(dataSizeOffset);
        bufClear();
        int dataSize = super.fileSize - (dataSizeOffset + 4);
        bufWriteIntLittleEndian(dataSize);
        bufFlush();
        if(factChunkLength > 0)
        {
            float duration = (float)dataSize / (float)bytesPerSecond;
            int numberOfSamples = (int)((double)duration * sampleRate);
            seek(numberOfSamplesOffset);
            bufClear();
            bufWriteIntLittleEndian(numberOfSamples);
            bufFlush();
        }
    }

    private AudioFormat audioFormat;
    private WavAudioFormat wavAudioFormat;
    private int sampleSizeInBits;
    private double sampleRate;
    private int channels;
    private byte codecSpecificHeader[];
    private short wFormatTag;
    private int blockAlign;
    private int bytesPerSecond;
    private int dataSizeOffset;
    private int numberOfSamplesOffset;
    private int factChunkLength;
    Format signed;
    Format unsigned;
}
