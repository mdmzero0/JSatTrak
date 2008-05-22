// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RawPullBufferParser.java

package com.sun.media.parser;

import java.io.IOException;
import javax.media.*;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media.parser:
//            RawPullStreamParser, RawParser

public class RawPullBufferParser extends RawPullStreamParser
{
    class FrameTrack
        implements Track
    {

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
            return parser.getDuration();
        }

        public Time getStartTime()
        {
            return new Time(0L);
        }

        public void setTrackListener(TrackListener l)
        {
            listener = l;
        }

        public void readFrame(Buffer buffer)
        {
            if(buffer.getData() == null)
                buffer.setData(new byte[500]);
            try
            {
                pbs.read(buffer);
            }
            catch(IOException e)
            {
                buffer.setDiscard(true);
            }
        }

        public int mapTimeToFrame(Time t)
        {
            return -1;
        }

        public Time mapFrameToTime(int frameNumber)
        {
            return new Time(0L);
        }

        Demultiplexer parser;
        PullBufferStream pbs;
        boolean enabled;
        Format format;
        TrackListener listener;
        Integer stateReq;

        public FrameTrack(Demultiplexer parser, PullBufferStream pbs)
        {
            enabled = true;
            format = null;
            stateReq = new Integer(0);
            this.pbs = pbs;
            format = pbs.getFormat();
        }
    }


    public RawPullBufferParser()
    {
    }

    public String getName()
    {
        return "Raw pull stream parser";
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        if(!(source instanceof PullBufferDataSource))
            throw new IncompatibleSourceException("DataSource not supported: " + source);
        super.streams = ((PullBufferDataSource)source).getStreams();
        if(super.streams == null)
            throw new IOException("Got a null stream from the DataSource");
        if(super.streams.length == 0)
            throw new IOException("Got a empty stream array from the DataSource");
        if(!supports(super.streams))
        {
            throw new IncompatibleSourceException("DataSource not supported: " + source);
        } else
        {
            super.source = source;
            super.streams = super.streams;
            return;
        }
    }

    protected boolean supports(SourceStream streams[])
    {
        return streams[0] != null && (streams[0] instanceof PullBufferStream);
    }

    public void open()
    {
        if(super.tracks != null)
            return;
        super.tracks = new Track[super.streams.length];
        for(int i = 0; i < super.streams.length; i++)
            super.tracks[i] = new FrameTrack(this, (PullBufferStream)super.streams[i]);

    }

    static final String NAME = "Raw pull stream parser";
}
