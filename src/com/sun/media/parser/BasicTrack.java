// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicTrack.java

package com.sun.media.parser;

import com.sun.media.Log;
import java.io.IOException;
import javax.media.*;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media.parser:
//            BasicPullParser

public class BasicTrack
    implements Track
{

    public BasicTrack(BasicPullParser parser, Format format, boolean enabled, Time duration, Time startTime, int numBuffers, int dataSize, 
            PullSourceStream stream)
    {
        this(parser, format, enabled, duration, startTime, numBuffers, dataSize, stream, 0L, 0x7fffffffffffffffL);
    }

    public BasicTrack(BasicPullParser parser, Format format, boolean enabled, Time duration, Time startTime, int numBuffers, int dataSize, 
            PullSourceStream stream, long minLocation, long maxLocation)
    {
        this.enabled = true;
        sequenceNumber = 0L;
        seekLocation = -1L;
        mediaSizeAtEOM = -1L;
        warnedUserOfReadPastEOM = false;
        this.parser = parser;
        this.format = format;
        this.enabled = enabled;
        this.duration = duration;
        this.startTime = startTime;
        this.numBuffers = numBuffers;
        this.dataSize = dataSize;
        this.stream = stream;
        this.minLocation = minLocation;
        this.maxLocation = maxLocation;
        maxStartLocation = maxLocation - (long)dataSize;
    }

    public Format getFormat()
    {
        return format;
    }

    public void setEnabled(boolean t)
    {
        enabled = t;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public Time getDuration()
    {
        return duration;
    }

    public Time getStartTime()
    {
        return startTime;
    }

    public void setTrackListener(TrackListener l)
    {
        listener = l;
    }

    public synchronized void setSeekLocation(long location)
    {
        seekLocation = location;
    }

    public synchronized long getSeekLocation()
    {
        return seekLocation;
    }

    public void readFrame(Buffer buffer)
    {
        if(buffer == null)
            return;
        if(!enabled)
        {
            buffer.setDiscard(true);
            return;
        }
        buffer.setFormat(format);
        Object obj = buffer.getData();
        long location;
        boolean needToSeek;
        synchronized(this)
        {
            if(seekLocation != -1L)
            {
                location = seekLocation;
                if(seekLocation < maxLocation)
                    seekLocation = -1L;
                needToSeek = true;
            } else
            {
                location = parser.getLocation(stream);
                needToSeek = false;
            }
        }
        if(location < minLocation)
        {
            buffer.setDiscard(true);
            return;
        }
        if(location >= maxLocation)
        {
            buffer.setLength(0);
            buffer.setEOM(true);
            return;
        }
        int needDataSize;
        if(location > maxStartLocation)
            needDataSize = dataSize - (int)(location - maxStartLocation);
        else
            needDataSize = dataSize;
        byte data[];
        if(obj == null || !(obj instanceof byte[]) || ((byte[])obj).length < needDataSize)
        {
            data = new byte[needDataSize];
            buffer.setData(data);
        } else
        {
            data = (byte[])obj;
        }
        try
        {
            if(parser.cacheStream != null && listener != null && parser.cacheStream.willReadBytesBlock(location, needDataSize))
                listener.readHasBlocked(this);
            if(needToSeek)
            {
                long pos = ((Seekable)stream).seek(location);
                if(pos == -2L)
                {
                    buffer.setDiscard(true);
                    return;
                }
            }
            if(parser.getMediaTime() != null)
                buffer.setTimeStamp(parser.getMediaTime().getNanoseconds());
            else
                buffer.setTimeStamp(-1L);
            buffer.setDuration(-1L);
            int actualBytesRead = parser.readBytes(stream, data, needDataSize);
            buffer.setOffset(0);
            buffer.setLength(actualBytesRead);
            buffer.setSequenceNumber(++sequenceNumber);
        }
        catch(IOException e)
        {
            if(maxLocation != 0x7fffffffffffffffL)
            {
                if(!warnedUserOfReadPastEOM)
                {
                    Log.warning("Warning: Attempt to read past End of Media");
                    Log.warning("This typically happens if the duration is not known or");
                    Log.warning("if the media file has incorrect header info");
                    warnedUserOfReadPastEOM = true;
                }
                buffer.setLength(0);
                buffer.setEOM(true);
            } else
            {
                long length = parser.streams[0].getContentLength();
                if(length != -1L)
                {
                    maxLocation = length;
                    maxStartLocation = maxLocation - (long)dataSize;
                    mediaSizeAtEOM = maxLocation - minLocation;
                    buffer.setLength(0);
                    buffer.setDiscard(true);
                } else
                {
                    maxLocation = parser.getLocation(stream);
                    maxStartLocation = maxLocation - (long)dataSize;
                    mediaSizeAtEOM = maxLocation - minLocation;
                    buffer.setLength(0);
                    buffer.setEOM(true);
                }
            }
        }
    }

    public int mapTimeToFrame(Time t)
    {
        return 0x7fffffff;
    }

    public Time mapFrameToTime(int frameNumber)
    {
        return Track.TIME_UNKNOWN;
    }

    public long getMediaSizeAtEOM()
    {
        return mediaSizeAtEOM;
    }

    private Format format;
    private boolean enabled;
    protected Time duration;
    private Time startTime;
    private int numBuffers;
    private int dataSize;
    private PullSourceStream stream;
    private long minLocation;
    private long maxLocation;
    private long maxStartLocation;
    private BasicPullParser parser;
    private long sequenceNumber;
    private TrackListener listener;
    private long seekLocation;
    private long mediaSizeAtEOM;
    private boolean warnedUserOfReadPastEOM;
}
