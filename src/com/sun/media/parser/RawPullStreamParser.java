// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RawPullStreamParser.java

package com.sun.media.parser;

import java.io.IOException;
import javax.media.*;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media.parser:
//            RawParser

public class RawPullStreamParser extends RawParser
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
            byte data[] = (byte[])buffer.getData();
            if(data == null)
            {
                data = new byte[500];
                buffer.setData(data);
            }
            try
            {
                int len = pss.read(data, 0, data.length);
                buffer.setLength(len);
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
        PullSourceStream pss;
        boolean enabled;
        Format format;
        TrackListener listener;
        Integer stateReq;

        public FrameTrack(Demultiplexer parser, PullSourceStream pss)
        {
            enabled = true;
            format = null;
            stateReq = new Integer(0);
            this.pss = pss;
        }
    }


    public String getName()
    {
        return "Raw pull stream parser";
    }

    public RawPullStreamParser()
    {
        tracks = null;
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        if(!(source instanceof PullDataSource))
            throw new IncompatibleSourceException("DataSource not supported: " + source);
        streams = ((PullDataSource)source).getStreams();
        if(streams == null)
            throw new IOException("Got a null stream from the DataSource");
        if(streams.length == 0)
            throw new IOException("Got a empty stream array from the DataSource");
        if(!supports(streams))
        {
            throw new IncompatibleSourceException("DataSource not supported: " + source);
        } else
        {
            super.source = source;
            streams = streams;
            return;
        }
    }

    protected boolean supports(SourceStream streams[])
    {
        return streams[0] != null && (streams[0] instanceof PullSourceStream);
    }

    public Track[] getTracks()
    {
        return tracks;
    }

    public void open()
    {
        if(tracks != null)
            return;
        tracks = new Track[streams.length];
        for(int i = 0; i < streams.length; i++)
            tracks[i] = new FrameTrack(this, (PullSourceStream)streams[i]);

    }

    public void close()
    {
        if(super.source != null)
        {
            try
            {
                super.source.stop();
                super.source.disconnect();
            }
            catch(IOException e) { }
            super.source = null;
        }
    }

    public void start()
        throws IOException
    {
        super.source.start();
    }

    public void stop()
    {
        try
        {
            super.source.stop();
        }
        catch(IOException e) { }
    }

    protected SourceStream streams[];
    protected Track tracks[];
    static final String NAME = "Raw pull stream parser";
}
