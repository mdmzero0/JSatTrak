// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AiffParser.java

package com.sun.media.parser.audio;

import com.sun.media.parser.BasicPullParser;
import com.sun.media.parser.BasicTrack;
import com.sun.media.util.SettableTime;
import java.io.IOException;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.protocol.*;

public class AiffParser extends BasicPullParser
{
    class AiffTrack extends BasicTrack
    {

        private double sampleRate;
        private float timePerFrame;
        private SettableTime frameToTime;

        AiffTrack(AudioFormat format, boolean enabled, Time startTime, int numBuffers, int bufferSize, long minLocation, long maxLocation)
        {
            super(AiffParser.this, format, enabled, duration, startTime, numBuffers, bufferSize, stream, minLocation, maxLocation);
            frameToTime = new SettableTime();
            double sampleRate = format.getSampleRate();
            int channels = format.getChannels();
            int sampleSizeInBits = format.getSampleSizeInBits();
            if(timePerBlockNano == -1D)
            {
                float bytesPerSecond = (float)(sampleRate * (double)blockSize);
                timePerFrame = (float)bufferSize / bytesPerSecond;
            } else
            {
                float blocksPerFrame = (float)bufferSize / (float)blockSize;
                float samplesPerFrame = blocksPerFrame * (float)samplesPerBlock;
                timePerFrame = (float)((double)samplesPerFrame / sampleRate);
            }
        }

        AiffTrack(AudioFormat format, boolean enabled, Time startTime, int numBuffers, int bufferSize)
        {
            this(format, enabled, startTime, numBuffers, bufferSize, 0L, 0x7fffffffffffffffL);
        }
    }


    public AiffParser()
    {
        duration = Duration.DURATION_UNKNOWN;
        format = null;
        tracks = new Track[1];
        numBuffers = 4;
        bufferSize = -1;
        mediaTime = new SettableTime(0L);
        stream = null;
        blockSize = 0;
        sampleRate = -1D;
        encodingString = null;
        samplesPerBlock = 1;
        timePerBlockNano = -1D;
        locationToMediaTime = -1D;
        isAIFC = false;
        commonChunkSeen = false;
        soundDataChunkSeen = false;
        formatVersionChunkSeen = false;
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
        tracks[0] = new AiffTrack((AudioFormat)format, true, new Time(0L), numBuffers, bufferSize, minLocation, maxLocation);
        return tracks;
    }

    private void readHeader()
        throws IOException, BadHeaderException
    {
        boolean signed = true;
        String magic = readString(stream);
        if(!magic.equals("FORM"))
            throw new BadHeaderException("AIFF Parser: expected string FORM, got " + magic);
        int fileLength = readInt(stream) + 8;
        String formType = readString(stream);
        if(formType.equals("AIFC"))
            isAIFC = true;
        else
            encodingString = "LINEAR";
        int remainingLength = fileLength - 12;
        String compressionType = null;
        int offset = 0;
        int channels = -1;
        int sampleSizeInBits = -1;
        int size;
        for(; remainingLength >= 8; remainingLength -= size)
        {
            String type = readString(stream);
            size = readInt(stream);
            remainingLength -= 8;
            if(type.equals("FVER"))
            {
                if(isAIFC);
                int timestamp = readInt(stream);
                if(size != 4)
                    throw new BadHeaderException("Illegal FormatVersionID: chunk size is not 4 but " + size);
                formatVersionChunkSeen = true;
                continue;
            }
            if(type.equals("COMM"))
            {
                if(size < 18)
                    throw new BadHeaderException("Size of COMM chunk should be atleast 18");
                channels = readShort(stream);
                if(channels < 1)
                    throw new BadHeaderException("Number of channels is " + channels);
                maxFrame = readInt(stream);
                sampleSizeInBits = readShort(stream);
                if(sampleSizeInBits <= 0)
                    throw new BadHeaderException("Illegal sampleSize " + sampleSizeInBits);
                sampleRate = readIeeeExtended(stream);
                if(sampleRate < 0.0D)
                    throw new BadHeaderException("Negative Sample Rate " + sampleRate);
                int remainingCommSize = size - 18;
                if(isAIFC)
                {
                    if(remainingCommSize < 4)
                        throw new BadHeaderException("COMM chunk in AIFC doesn't have compressionType info");
                    compressionType = readString(stream);
                    if(compressionType == null)
                        throw new BadHeaderException("Compression type for AIFC is null");
                    skip(stream, remainingCommSize - 4);
                }
                commonChunkSeen = true;
                continue;
            }
            if(type.equals("SSND"))
            {
                if(soundDataChunkSeen)
                    throw new BadHeaderException("Cannot have more than 1 Sound Data Chunk");
                offset = readInt(stream);
                blockSize = readInt(stream);
                minLocation = getLocation(stream);
                dataSize = size - 8;
                maxLocation = minLocation + (long)dataSize;
                soundDataChunkSeen = true;
                if(commonChunkSeen)
                {
                    remainingLength -= 8;
                    break;
                }
                skip(stream, size - 8);
            } else
            {
                skip(stream, size);
            }
        }

        if(!commonChunkSeen)
            throw new BadHeaderException("Mandatory chunk COMM missing");
        if(!soundDataChunkSeen)
            throw new BadHeaderException("Mandatory chunk SSND missing");
        double durationSeconds = -1D;
        if(isAIFC)
        {
            String c = compressionType;
            if(c.equalsIgnoreCase("NONE"))
                encodingString = "LINEAR";
            else
            if(c.equalsIgnoreCase("twos"))
                encodingString = "LINEAR";
            else
            if(c.equalsIgnoreCase("raw"))
            {
                encodingString = "LINEAR";
                signed = false;
            } else
            if(c.equalsIgnoreCase("ULAW"))
            {
                encodingString = "ULAW";
                sampleSizeInBits = 8;
                signed = false;
            } else
            if(c.equalsIgnoreCase("ALAW"))
            {
                encodingString = "alaw";
                sampleSizeInBits = 8;
                signed = false;
            } else
            if(c.equalsIgnoreCase("G723"))
                encodingString = "g723";
            else
            if(c.equalsIgnoreCase("MAC3"))
            {
                encodingString = "MAC3";
                blockSize = 2;
                samplesPerBlock = 6;
                timePerBlockNano = ((double)samplesPerBlock * 1000000000D) / sampleRate;
            } else
            if(c.equalsIgnoreCase("MAC6"))
            {
                encodingString = "MAC6";
                blockSize = 1;
                samplesPerBlock = 6;
                timePerBlockNano = ((double)samplesPerBlock * 1000000000D) / sampleRate;
            } else
            if(c.equalsIgnoreCase("IMA4"))
            {
                encodingString = "ima4";
                blockSize = 34 * channels;
                samplesPerBlock = 64;
                timePerBlockNano = ((double)samplesPerBlock * 1000000000D) / sampleRate;
            } else
            {
                throw new BadHeaderException("Unsupported encoding" + c);
            }
        }
        if(blockSize == 0)
            blockSize = (channels * sampleSizeInBits) / 8;
        bufferSize = blockSize * (int)(sampleRate / (double)samplesPerBlock);
        durationSeconds = (double)(maxFrame * samplesPerBlock) / sampleRate;
        if(durationSeconds > 0.0D)
            duration = new Time(durationSeconds);
        locationToMediaTime = (double)samplesPerBlock / (sampleRate * (double)blockSize);
        format = new AudioFormat(encodingString, sampleRate, sampleSizeInBits, channels, 1, signed ? 1 : 0, blockSize * 8, -1D, Format.byteArray);
    }

    public Time setPosition(Time where, int rounding)
    {
        if(!super.seekable)
            return getMediaTime();
        long time = where.getNanoseconds();
        if(time < 0L)
            time = 0L;
        long newPos;
        if(timePerBlockNano == -1D)
        {
            int bytesPerSecond = (int)sampleRate * blockSize;
            double newPosd = ((double)time * sampleRate * (double)blockSize) / 1000000000D;
            double remainder = newPosd % (double)blockSize;
            newPos = (long)(newPosd - remainder);
            if(remainder > 0.0D)
                switch(rounding)
                {
                case 1: // '\001'
                    newPos += blockSize;
                    break;

                case 3: // '\003'
                    if(remainder > (double)blockSize / 2D)
                        newPos += blockSize;
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
            newPos = blockNumInt * blockSize;
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
        if(maxFrame <= 0 && tracks[0] != null)
        {
            long mediaSizeAtEOM = ((BasicTrack)tracks[0]).getMediaSizeAtEOM();
            if(mediaSizeAtEOM > 0L)
            {
                maxFrame = (int)(mediaSizeAtEOM / (long)blockSize);
                double durationSeconds = (double)(maxFrame * samplesPerBlock) / sampleRate;
                if(durationSeconds > 0.0D)
                    duration = new Time(durationSeconds);
            }
        }
        return duration;
    }

    public String getName()
    {
        return "Parser for AIFF file format";
    }

    private double readIeeeExtended(PullSourceStream stream)
        throws IOException
    {
        double f = 0.0D;
        int expon = 0;
        long hiMant = 0L;
        long loMant = 0L;
        double huge = 3.4028234663852886E+038D;
        expon = readShort(stream);
        hiMant = readInt(stream);
        if(hiMant < 0L)
            hiMant += 0x100000000L;
        loMant = readInt(stream);
        if(loMant < 0L)
            loMant += 0x100000000L;
        if(expon == 0 && hiMant == 0L && loMant == 0L)
            f = 0.0D;
        else
        if(expon == 32767)
        {
            f = huge;
        } else
        {
            expon -= 16383;
            expon -= 31;
            f = (double)hiMant * Math.pow(2D, expon);
            expon -= 32;
            f += (double)loMant * Math.pow(2D, expon);
        }
        return f;
    }

    private Time duration;
    private Format format;
    private Track tracks[];
    private int numBuffers;
    private int bufferSize;
    private int dataSize;
    private SettableTime mediaTime;
    private PullSourceStream stream;
    private int maxFrame;
    private int blockSize;
    private double sampleRate;
    private long minLocation;
    private long maxLocation;
    private String encodingString;
    private int samplesPerBlock;
    private double timePerBlockNano;
    private double locationToMediaTime;
    public static final String FormID = "FORM";
    public static final String FormatVersionID = "FVER";
    public static final String CommonID = "COMM";
    public static final String SoundDataID = "SSND";
    private static ContentDescriptor supportedFormat[] = {
        new ContentDescriptor("audio.x_aiff")
    };
    public static final int CommonIDSize = 18;
    private boolean isAIFC;
    private boolean commonChunkSeen;
    private boolean soundDataChunkSeen;
    private boolean formatVersionChunkSeen;






}
