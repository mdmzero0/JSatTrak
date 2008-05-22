// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GsmParser.java

package com.sun.media.parser.audio;

import com.sun.media.parser.BasicPullParser;
import com.sun.media.parser.BasicTrack;
import com.sun.media.util.SettableTime;
import java.io.IOException;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.protocol.*;

public class GsmParser extends BasicPullParser
{
    class GsmTrack extends BasicTrack
    {

        private double sampleRate;
        private float timePerFrame;
        private SettableTime frameToTime;

        GsmTrack(AudioFormat format, boolean enabled, Time startTime, int numBuffers, int bufferSize, long minLocation, long maxLocation)
        {
            super(GsmParser.this, format, enabled, duration, startTime, numBuffers, bufferSize, stream, minLocation, maxLocation);
            timePerFrame = 0.02F;
            frameToTime = new SettableTime();
            double sampleRate = format.getSampleRate();
            int channels = format.getChannels();
            int sampleSizeInBits = format.getSampleSizeInBits();
            long durationNano = super.duration.getNanoseconds();
        }

        GsmTrack(AudioFormat format, boolean enabled, Time startTime, int numBuffers, int bufferSize)
        {
            this(format, enabled, startTime, numBuffers, bufferSize, 0L, 0x7fffffffffffffffL);
        }
    }


    public GsmParser()
    {
        duration = Duration.DURATION_UNKNOWN;
        format = null;
        tracks = new Track[1];
        numBuffers = 4;
        mediaTime = new SettableTime(0L);
        bytesPerSecond = 1650;
        blockSize = 33;
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
        {
            return tracks;
        } else
        {
            stream = (PullSourceStream)super.streams[0];
            readHeader();
            bufferSize = bytesPerSecond;
            tracks[0] = new GsmTrack((AudioFormat)format, true, new Time(0L), numBuffers, bufferSize, minLocation, maxLocation);
            return tracks;
        }
    }

    private void readHeader()
        throws IOException, BadHeaderException
    {
        minLocation = getLocation(stream);
        long contentLength = stream.getContentLength();
        if(contentLength != -1L)
        {
            double durationSeconds = contentLength / (long)bytesPerSecond;
            duration = new Time(durationSeconds);
            maxLocation = contentLength;
        } else
        {
            maxLocation = 0x7fffffffffffffffL;
        }
        boolean signed = true;
        boolean bigEndian = false;
        format = new AudioFormat("gsm", 8000D, 16, 1, bigEndian ? 1 : 0, signed ? 1 : 0, blockSize * 8, -1D, Format.byteArray);
    }

    public Time setPosition(Time where, int rounding)
    {
        if(!super.seekable)
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
        if(duration.equals(Duration.DURATION_UNKNOWN) && tracks[0] != null)
        {
            long mediaSizeAtEOM = ((BasicTrack)tracks[0]).getMediaSizeAtEOM();
            if(mediaSizeAtEOM > 0L)
            {
                double durationSeconds = mediaSizeAtEOM / (long)bytesPerSecond;
                duration = new Time(durationSeconds);
            }
        }
        return duration;
    }

    public String getName()
    {
        return "Parser for raw GSM";
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
    private static ContentDescriptor supportedFormat[] = {
        new ContentDescriptor("audio.x_gsm")
    };



}
