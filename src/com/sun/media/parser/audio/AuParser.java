// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AuParser.java

package com.sun.media.parser.audio;

import com.sun.media.parser.BasicPullParser;
import com.sun.media.parser.BasicTrack;
import com.sun.media.util.SettableTime;
import java.io.IOException;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.protocol.*;

public class AuParser extends BasicPullParser
{
    class AuTrack extends BasicTrack
    {

        private double sampleRate;
        private float timePerFrame;
        private SettableTime frameToTime;

        AuTrack(AudioFormat format, boolean enabled, Time startTime, int numBuffers, int bufferSize, long minLocation, long maxLocation)
        {
            super(AuParser.this, format, enabled, duration, startTime, numBuffers, bufferSize, stream, minLocation, maxLocation);
            frameToTime = new SettableTime();
            double sampleRate = format.getSampleRate();
            int channels = format.getChannels();
            int sampleSizeInBits = format.getSampleSizeInBits();
            long durationNano = super.duration.getNanoseconds();
        }

        AuTrack(AudioFormat format, boolean enabled, Time startTime, int numBuffers, int bufferSize)
        {
            this(format, enabled, startTime, numBuffers, bufferSize, 0L, 0x7fffffffffffffffL);
        }
    }


    public AuParser()
    {
        duration = Duration.DURATION_UNKNOWN;
        format = null;
        tracks = new Track[1];
        numBuffers = 4;
        mediaTime = new SettableTime(0L);
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
        if(dataSize == -1)
            maxLocation = 0x7fffffffffffffffL;
        else
            maxLocation = minLocation + (long)dataSize;
        tracks[0] = new AuTrack((AudioFormat)format, true, new Time(0L), numBuffers, bufferSize, minLocation, maxLocation);
        return tracks;
    }

    private void readHeader()
        throws IOException, BadHeaderException
    {
        int magic = readInt(stream, true);
        boolean bigEndian;
        if(magic == 0x2e736e64 || magic == 0x2e736400)
            bigEndian = true;
        else
        if(magic == 0x646e732e || magic == 0x64732e)
            bigEndian = false;
        else
            throw new BadHeaderException("Invalid magic number " + Integer.toHexString(magic));
        int headerSize = readInt(stream);
        if(headerSize < 24)
            throw new BadHeaderException("AU Parser: header size should be atleast 24 but is " + headerSize);
        dataSize = readInt(stream);
        if(dataSize == -1)
        {
            long contentLength = stream.getContentLength();
            if(contentLength != -1L)
            {
                dataSize = (int)(contentLength - (long)headerSize);
                if(dataSize < 0)
                    dataSize = -1;
            }
        }
        int encoding = readInt(stream);
        blockSize = -1;
        int sampleSizeInBits;
        switch(encoding)
        {
        case 1: // '\001'
            encodingString = "ULAW";
            sampleSizeInBits = 8;
            blockSize = 1;
            break;

        case 27: // '\033'
            encodingString = "alaw";
            sampleSizeInBits = 8;
            blockSize = 1;
            break;

        case 2: // '\002'
            encodingString = "LINEAR";
            sampleSizeInBits = 8;
            blockSize = 1;
            break;

        case 3: // '\003'
            encodingString = "LINEAR";
            sampleSizeInBits = 16;
            blockSize = 2;
            break;

        case 4: // '\004'
            encodingString = "LINEAR";
            sampleSizeInBits = 24;
            blockSize = 3;
            break;

        case 5: // '\005'
            encodingString = "LINEAR";
            sampleSizeInBits = 32;
            blockSize = 4;
            break;

        case 6: // '\006'
            encodingString = "float";
            sampleSizeInBits = 32;
            blockSize = 4;
            break;

        case 7: // '\007'
            encodingString = "double";
            sampleSizeInBits = 64;
            blockSize = 8;
            break;

        case 23: // '\027'
            encodingString = "??? what adpcm";
            sampleSizeInBits = 4;
            break;

        case 25: // '\031'
            encodingString = "G723_3";
            sampleSizeInBits = 3;
            break;

        case 26: // '\032'
            encodingString = "G723_5";
            sampleSizeInBits = 5;
            break;

        case 8: // '\b'
        case 9: // '\t'
        case 10: // '\n'
        case 11: // '\013'
        case 12: // '\f'
        case 13: // '\r'
        case 14: // '\016'
        case 15: // '\017'
        case 16: // '\020'
        case 17: // '\021'
        case 18: // '\022'
        case 19: // '\023'
        case 20: // '\024'
        case 21: // '\025'
        case 22: // '\026'
        case 24: // '\030'
        default:
            throw new BadHeaderException("Unsupported encoding: " + Integer.toHexString(encoding));
        }
        int sampleRate = readInt(stream);
        if(sampleRate < 0)
            throw new BadHeaderException("Negative Sample Rate " + sampleRate);
        int channels = readInt(stream);
        if(channels < 1)
            throw new BadHeaderException("Number of channels is " + channels);
        if(blockSize != -1)
            blockSize *= channels;
        skip(stream, headerSize - 24);
        bytesPerSecond = (channels * sampleSizeInBits * sampleRate) / 8;
        int frameSizeInBytes = (channels * sampleSizeInBits) / 8;
        bufferSize = bytesPerSecond;
        if(dataSize != -1)
        {
            double durationSeconds = (double)dataSize / (double)bytesPerSecond;
            duration = new Time(durationSeconds);
        }
        boolean signed = true;
        format = new AudioFormat(encodingString, sampleRate, sampleSizeInBits, channels, bigEndian ? 1 : 0, signed ? 1 : 0, frameSizeInBytes * 8, -1D, Format.byteArray);
    }

    public Time setPosition(Time where, int rounding)
    {
        if(!super.seekable)
            return getMediaTime();
        if(blockSize < 0)
            return getMediaTime();
        long time = where.getNanoseconds();
        if(time < 0L)
            time = 0L;
        double newPosd = (double)(time * (long)bytesPerSecond) / 1000000000D;
        double remainder = newPosd % (double)blockSize;
        long newPos = (long)(newPosd - remainder);
        if(remainder > 0.0D)
            switch(rounding)
            {
            default:
                break;

            case 1: // '\001'
                newPos += blockSize;
                break;

            case 3: // '\003'
                if(remainder > (double)blockSize / 2D)
                    newPos += blockSize;
                break;
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
            mediaTime.set((double)location / (double)bytesPerSecond);
        }
        return mediaTime;
    }

    public Time getDuration()
    {
        return duration;
    }

    public String getName()
    {
        return "Parser for AU file format";
    }

    private Time duration;
    private Format format;
    private Track tracks[];
    private int numBuffers;
    private int bufferSize;
    private int dataSize;
    private SettableTime mediaTime;
    private int encoding;
    private String encodingString;
    private int sampleRate;
    private int samplesPerBlock;
    private int bytesPerSecond;
    private int blockSize;
    private long minLocation;
    private long maxLocation;
    private PullSourceStream stream;
    public static final int AU_SUN_MAGIC = 0x2e736e64;
    public static final int AU_SUN_INV_MAGIC = 0x646e732e;
    public static final int AU_DEC_MAGIC = 0x2e736400;
    public static final int AU_DEC_INV_MAGIC = 0x64732e;
    public static final int AU_ULAW_8 = 1;
    public static final int AU_LINEAR_8 = 2;
    public static final int AU_LINEAR_16 = 3;
    public static final int AU_LINEAR_24 = 4;
    public static final int AU_LINEAR_32 = 5;
    public static final int AU_FLOAT = 6;
    public static final int AU_DOUBLE = 7;
    public static final int AU_ADPCM_G721 = 23;
    public static final int AU_ADPCM_G722 = 24;
    public static final int AU_ADPCM_G723_3 = 25;
    public static final int AU_ADPCM_G723_5 = 26;
    public static final int AU_ALAW_8 = 27;
    private static ContentDescriptor supportedFormat[] = {
        new ContentDescriptor("audio.basic")
    };



}
