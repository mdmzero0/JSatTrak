// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   WavParser.java

package com.sun.media.parser.audio;

import com.sun.media.format.WavAudioFormat;
import com.sun.media.parser.BasicPullParser;
import com.sun.media.parser.BasicTrack;
import com.sun.media.util.SettableTime;
import java.io.IOException;
import java.util.Hashtable;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.protocol.*;

public class WavParser extends BasicPullParser
{
    class WavTrack extends BasicTrack
    {

        private double sampleRate;
        private float timePerFrame;
        private SettableTime frameToTime;

        WavTrack(WavAudioFormat format, boolean enabled, Time startTime, int numBuffers, int bufferSize, long minLocation, long maxLocation)
        {
            super(WavParser.this, format, enabled, duration, startTime, numBuffers, bufferSize, stream, minLocation, maxLocation);
            frameToTime = new SettableTime();
            double sampleRate = format.getSampleRate();
            int channels = format.getChannels();
            int sampleSizeInBits = format.getSampleSizeInBits();
            int blockSize = format.getFrameSizeInBits() / 8;
            if(encoding == 1 || encoding == 7 || encoding == 6 || encoding == 257 || encoding == 258)
            {
                float bytesPerSecond = (float)sampleRate * (float)blockSize;
                float bytesPerFrame = bufferSize;
                timePerFrame = (float)bufferSize / bytesPerSecond;
            } else
            if(encoding == 2 || encoding == 17 || encoding == 49)
            {
                float bytesPerFrame = bufferSize;
                float blocksPerFrame = (float)bufferSize / (float)blockSize;
                float samplesPerFrame = blocksPerFrame * (float)samplesPerBlock;
                timePerFrame = (float)((double)samplesPerFrame / sampleRate);
            } else
            {
                float bytesPerSecond = (float)sampleRate * (float)blockSize;
                float bytesPerFrame = bufferSize;
                timePerFrame = (float)bufferSize / bytesPerSecond;
            }
        }

        WavTrack(WavAudioFormat format, boolean enabled, Time startTime, int numBuffers, int bufferSize)
        {
            this(format, enabled, startTime, numBuffers, bufferSize, 0L, 0x7fffffffffffffffL);
        }
    }


    public WavParser()
    {
        duration = Duration.DURATION_UNKNOWN;
        format = null;
        tracks = new Track[1];
        numBuffers = 4;
        mediaTime = new SettableTime(0L);
        locationToMediaTime = -1D;
        timePerBlockNano = -1D;
        stream = null;
    }

    public ContentDescriptor[] getSupportedInputContentDescriptors()
    {
        return supportedFormat;
    }

    public Track[] getTracks()
        throws IOException, BadHeaderException
    {
        if(tracks[0] != null)
            return tracks;
        stream = (PullSourceStream)super.streams[0];
        if(super.cacheStream != null)
            super.cacheStream.setEnabledBuffering(false);
        readHeader();
        if(super.cacheStream != null)
            super.cacheStream.setEnabledBuffering(true);
        minLocation = getLocation(stream);
        maxLocation = minLocation + (long)dataSize;
        tracks[0] = new WavTrack(format, true, new Time(0L), numBuffers, bufferSize, minLocation, maxLocation);
        return tracks;
    }

    private void readHeader()
        throws IOException, BadHeaderException
    {
        String magicRIFF = readString(stream);
        if(!magicRIFF.equals("RIFF"))
            throw new BadHeaderException("WAVE Parser: expected magic string RIFF, got " + magicRIFF);
        int length = readInt(stream, false);
        String magicWAVE = readString(stream);
        if(!magicWAVE.equals("WAVE"))
            throw new BadHeaderException("WAVE Parser: expected magic string WAVE, got " + magicWAVE);
        length += 8;
        int size;
        for(; !readString(stream).equals("fmt "); skip(stream, size))
            size = readInt(stream, false);

        int formatSize = readInt(stream, false);
        int remainingFormatSize = formatSize;
        if(formatSize >= 16);
        encoding = readShort(stream, false);
        encodingString = (String)WavAudioFormat.formatMapper.get(new Integer(encoding));
        if(encodingString == null)
            encodingString = "unknown";
        channels = readShort(stream, false);
        sampleRate = readInt(stream, false);
        int bytesPerSecond = readInt(stream, false);
        blockAlign = readShort(stream, false);
        sampleSizeInBits = readShort(stream, false);
        if(encoding == 85)
            sampleSizeInBits = 16;
        samplesPerBlock = -1;
        if((remainingFormatSize -= 16) > 0 && encoding == 1 || remainingFormatSize <= 2)
        {
            skip(stream, remainingFormatSize);
            remainingFormatSize = 0;
        }
        byte codecSpecificHeader[] = null;
        int extraFieldsSize = 0;
        if(remainingFormatSize >= 2)
        {
            extraFieldsSize = readShort(stream, false);
            remainingFormatSize -= 2;
            if(extraFieldsSize > 0)
            {
                codecSpecificHeader = new byte[extraFieldsSize];
                readBytes(stream, codecSpecificHeader, codecSpecificHeader.length);
                remainingFormatSize -= extraFieldsSize;
            }
        }
        switch(encoding)
        {
        case 2: // '\002'
        case 17: // '\021'
        case 49: // '1'
            if(extraFieldsSize < 2)
                throw new BadHeaderException("msadpcm: samplesPerBlock field not available");
            samplesPerBlock = BasicPullParser.parseShortFromArray(codecSpecificHeader, false);
            locationToMediaTime = (double)samplesPerBlock / (double)(sampleRate * blockAlign);
            break;

        default:
            locationToMediaTime = 1.0D / (double)(sampleRate * blockAlign);
            break;
        }
        if(remainingFormatSize < 0)
            throw new BadHeaderException("WAVE Parser: incorrect chunkSize in the fmt chunk");
        if(remainingFormatSize > 0)
            skip(stream, remainingFormatSize);
        int size;
        for(; !readString(stream).equals("data"); skip(stream, size))
            size = readInt(stream, false);

        dataSize = readInt(stream, false);
        if(blockAlign != 0)
        {
            if(bytesPerSecond < dataSize)
                bufferSize = bytesPerSecond - bytesPerSecond % blockAlign;
            else
                bufferSize = dataSize - dataSize % blockAlign;
        } else
        if(bytesPerSecond < dataSize)
            bufferSize = bytesPerSecond;
        else
            bufferSize = dataSize;
        double durationSeconds = -1D;
        if((channels * sampleSizeInBits) / 8 == blockAlign)
            durationSeconds = (float)dataSize / (float)bytesPerSecond;
        else
        if(samplesPerBlock > 0)
        {
            durationSeconds = (((float)dataSize / (float)blockAlign) * (float)samplesPerBlock) / (float)sampleRate;
            timePerBlockNano = ((double)samplesPerBlock * 1000000000D) / (double)sampleRate;
        } else
        {
            timePerBlockNano = ((double)blockAlign * 1000000000D) / (double)bytesPerSecond;
            durationSeconds = (float)dataSize / (float)bytesPerSecond;
        }
        duration = new Time(durationSeconds);
        boolean signed;
        if(sampleSizeInBits > 8)
            signed = true;
        else
            signed = false;
        format = new WavAudioFormat(encodingString, sampleRate, sampleSizeInBits, channels, blockAlign * 8, bytesPerSecond, 0, signed ? 1 : 0, -1F, Format.byteArray, codecSpecificHeader);
    }

    public Time setPosition(Time where, int rounding)
    {
        if(!super.seekable)
            return getMediaTime();
        long time = where.getNanoseconds();
        if(time < 0L)
            time = 0L;
        long newPos;
        if(timePerBlockNano <= 0.0D)
        {
            int bytesPerSecond = sampleRate * blockAlign;
            double newPosd = (double)(time * (long)sampleRate * (long)blockAlign) / 1000000000D;
            double remainder = newPosd % (double)blockAlign;
            newPos = (long)(newPosd - remainder);
            if(remainder > 0.0D)
                switch(rounding)
                {
                case 1: // '\001'
                    newPos += blockAlign;
                    break;

                case 3: // '\003'
                    if(remainder > (double)blockAlign / 2D)
                        newPos += blockAlign;
                    break;
                }
        } else
        {
            double blockNum = (double)time / timePerBlockNano;
            int blockNumInt = (int)blockNum;
            double remainder = blockNum - (double)blockNumInt;
            if(remainder > 0.0D)
                switch(rounding)
                {
                default:
                    break;

                case 1: // '\001'
                    blockNumInt++;
                    break;

                case 3: // '\003'
                    if(remainder > 0.5D)
                        blockNumInt++;
                    break;
                }
            newPos = blockNumInt * blockAlign;
        }
        newPos += minLocation;
        ((BasicTrack)tracks[0]).setSeekLocation(newPos);
        if(super.cacheStream != null)
            synchronized(this)
            {
                super.cacheStream.abortRead();
            }
        return where;
    }

    public Time getMediaTime()
    {
        long seekLocation = ((BasicTrack)tracks[0]).getSeekLocation();
        long location;
        if(seekLocation != -1L)
            location = seekLocation - minLocation;
        else
            location = getLocation(stream) - minLocation;
        synchronized(mediaTime)
        {
            mediaTime.set((double)location * locationToMediaTime);
        }
        return mediaTime;
    }

    public Time getDuration()
    {
        return duration;
    }

    public String getName()
    {
        return "Parser for WAV file format";
    }

    private Time duration;
    private WavAudioFormat format;
    private Track tracks[];
    private int numBuffers;
    private int bufferSize;
    private int dataSize;
    private SettableTime mediaTime;
    private int encoding;
    private String encodingString;
    private int sampleRate;
    private int channels;
    private int sampleSizeInBits;
    private int blockAlign;
    private int samplesPerBlock;
    private long minLocation;
    private long maxLocation;
    private double locationToMediaTime;
    private double timePerBlockNano;
    private PullSourceStream stream;
    private static ContentDescriptor supportedFormat[] = {
        new ContentDescriptor("audio.x_wav")
    };





}
