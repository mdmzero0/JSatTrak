// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RawStreamParser.java

package com.sun.media.parser;

import com.sun.media.CircularBuffer;
import java.io.IOException;
import javax.media.*;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media.parser:
//            RawParser

public class RawStreamParser extends RawParser
{
    class FrameTrack
        implements Track, SourceTransferHandler
    {

        public Format getFormat()
        {
            return format;
        }

        public void setEnabled(boolean t)
        {
            if(t)
                pss.setTransferHandler(this);
            else
                pss.setTransferHandler(null);
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
            synchronized(stateReq)
            {
                if(stopped)
                {
                    buffer.setDiscard(true);
                    buffer.setFormat(format);
                    return;
                }
            }
            Buffer filled;
            synchronized(bufferQ)
            {
                while(!bufferQ.canRead()) 
                    try
                    {
                        bufferQ.wait();
                        synchronized(stateReq)
                        {
                            if(stopped)
                            {
                                buffer.setDiscard(true);
                                buffer.setFormat(format);
                                return;
                            }
                        }
                    }
                    catch(Exception e) { }
                filled = bufferQ.read();
                bufferQ.notifyAll();
            }
            byte data[] = (byte[])filled.getData();
            filled.setData(buffer.getData());
            buffer.setData(data);
            buffer.setLength(filled.getLength());
            buffer.setFormat(format);
            buffer.setTimeStamp(-1L);
            synchronized(bufferQ)
            {
                bufferQ.readReport();
                bufferQ.notifyAll();
            }
        }

        public void stop()
        {
            synchronized(stateReq)
            {
                stopped = true;
            }
            synchronized(bufferQ)
            {
                bufferQ.notifyAll();
            }
        }

        public void start()
        {
            synchronized(stateReq)
            {
                stopped = false;
            }
            synchronized(bufferQ)
            {
                bufferQ.notifyAll();
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

        public void transferData(PushSourceStream pss)
        {
            Buffer buffer;
            synchronized(bufferQ)
            {
                while(!bufferQ.canWrite()) 
                    try
                    {
                        bufferQ.wait();
                    }
                    catch(Exception e) { }
                buffer = bufferQ.getEmptyBuffer();
                bufferQ.notifyAll();
            }
            int size = pss.getMinimumTransferSize();
            byte data[];
            if((data = (byte[])buffer.getData()) == null || data.length < size)
            {
                data = new byte[size];
                buffer.setData(data);
            }
            try
            {
                int len = pss.read(data, 0, size);
                buffer.setLength(len);
            }
            catch(IOException e)
            {
                buffer.setDiscard(true);
            }
            synchronized(bufferQ)
            {
                bufferQ.writeReport();
                bufferQ.notifyAll();
            }
        }

        Demultiplexer parser;
        PushSourceStream pss;
        boolean enabled;
        CircularBuffer bufferQ;
        Format format;
        TrackListener listener;
        Integer stateReq;
        boolean stopped;

        public FrameTrack(Demultiplexer parser, PushSourceStream pss, int numOfBufs)
        {
            enabled = true;
            format = null;
            stateReq = new Integer(0);
            stopped = true;
            this.pss = pss;
            pss.setTransferHandler(this);
            bufferQ = new CircularBuffer(numOfBufs);
        }
    }


    public String getName()
    {
        return "Raw stream parser";
    }

    public RawStreamParser()
    {
        tracks = null;
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        if(!(source instanceof PushDataSource))
            throw new IncompatibleSourceException("DataSource not supported: " + source);
        streams = ((PushDataSource)source).getStreams();
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
        return streams[0] != null && (streams[0] instanceof PushSourceStream);
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
            tracks[i] = new FrameTrack(this, (PushSourceStream)streams[i], 5);

    }

    public void close()
    {
        if(super.source != null)
        {
            try
            {
                super.source.stop();
                for(int i = 0; i < tracks.length; i++)
                    ((FrameTrack)tracks[i]).stop();

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
        for(int i = 0; i < tracks.length; i++)
            ((FrameTrack)tracks[i]).start();

    }

    public void stop()
    {
        try
        {
            super.source.stop();
            for(int i = 0; i < tracks.length; i++)
                ((FrameTrack)tracks[i]).stop();

        }
        catch(IOException e) { }
    }

    protected SourceStream streams[];
    protected Track tracks[];
    static final String NAME = "Raw stream parser";
}
