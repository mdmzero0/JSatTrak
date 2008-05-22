// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package com.sun.media.protocol.avi;

import com.sun.media.parser.video.AviParser;
import com.sun.media.protocol.BasicSourceStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.media.BadHeaderException;
import javax.media.Buffer;
import javax.media.Duration;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.Track;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

// Referenced classes of package com.sun.media.protocol.avi:
//            FileDataSource

public class DataSource extends PushBufferDataSource
{
    class AviSourceStream extends BasicSourceStream
        implements PushBufferStream, Runnable
    {

        public Format getFormat()
        {
            return format;
        }

        public void setTransferHandler(BufferTransferHandler th)
        {
            System.out.println("setTransferHandler: " + th);
            transferHandler = th;
        }

        public void connect()
            throws IOException
        {
        }

        void disconnect()
        {
        }

        void start()
            throws IOException
        {
            if(started)
                return;
            if(transferHandler != null)
            {
                (new Thread(this)).start();
                started = true;
            }
        }

        void stop()
            throws IOException
        {
            started = false;
        }

        public synchronized void read(Buffer b)
        {
            if(buffer.getLength() > 0)
            {
                b.setOffset(buffer.getOffset());
                b.setData(buffer.getData());
                b.setLength(buffer.getLength());
                b.setTimeStamp(buffer.getTimeStamp());
                b.setFormat(format);
            } else
            {
                b.setLength(buffer.getLength());
            }
        }

        public void run()
        {
            do
            {
                track.readFrame(buffer);
                synchronized(this)
                {
                    if(transferHandler != null)
                        transferHandler.transferData(this);
                }
                Thread.currentThread();
                Thread.yield();
            } while(true);
        }

        private BufferTransferHandler transferHandler;
        private boolean started;
        private Buffer buffer;
        private Format format;
        private Track track;

        AviSourceStream(Track track)
        {
            transferHandler = null;
            started = false;
            buffer = new Buffer();
            this.track = track;
            buffer.setData(null);
            format = track.getFormat();
        }
    }


    public DataSource()
    {
        contentType = "raw";
        connected = false;
        streams = new PushBufferStream[0];
    }

    public void doConnect(String fileName)
        throws IOException
    {
        try
        {
            this.fileName = fileName;
            inputDataSource = new FileDataSource(fileName);
            inputDataSource.connect();
            aviParser = new AviParser();
            aviParser.setSource(inputDataSource);
            try
            {
                tracks = aviParser.getTracks();
            }
            catch(BadHeaderException e)
            {
                throw new IOException("");
            }
            if(tracks == null || tracks.length <= 0)
                throw new IOException("Unable to get the tracks");
            for(int i = 0; i < tracks.length; i++)
                System.out.println(tracks[i].getFormat().getEncoding());

            numTracks = tracks.length;
            formats = new Format[numTracks];
            for(int i = 0; i < numTracks; i++)
            {
                System.out.println(tracks[i]);
                formats[i] = tracks[i].getFormat();
                tracks[i].setEnabled(true);
            }

        }
        catch(IncompatibleSourceException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    public void connect()
        throws IOException
    {
        if(connected)
            return;
        MediaLocator locator = getLocator();
        if(locator == null)
        {
            System.err.println("medialocator is null");
            throw new IOException(this + ": connect() failed");
        }
        String fileName = locator.getRemainder();
        doConnect(fileName);
        streams = new PushBufferStream[numTracks];
        for(int i = 0; i < numTracks; i++)
            streams[i] = new AviSourceStream(tracks[i]);

        System.out.println("connected");
        connected = true;
    }

    public void disconnect()
    {
    }

    public void start()
        throws IOException
    {
        for(int i = 0; i < numTracks; i++)
            if(streams[i] != null)
                ((AviSourceStream)streams[i]).start();

    }

    public void stop()
        throws IOException
    {
    }

    public String getContentType()
    {
        if(!connected)
        {
            return null;
        } else
        {
            System.out.println("avids: getContentType returns " + contentType);
            return contentType;
        }
    }

    public PushBufferStream[] getStreams()
    {
        if(!connected)
            return null;
        else
            return streams;
    }

    public Object[] getControls()
    {
        return new Object[0];
    }

    public Object getControl(String controlType)
    {
        return null;
    }

    public Time getDuration()
    {
        return Duration.DURATION_UNKNOWN;
    }

    private String fileName;
    private javax.media.protocol.DataSource inputDataSource;
    private AviParser aviParser;
    private Track tracks[];
    private Format formats[];
    private int numTracks;
    private String contentType;
    private boolean connected;
    private PushBufferStream streams[];
}
