// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DelegateDataSource.java

package com.sun.media.protocol;

import com.sun.media.Log;
import java.io.IOException;
import java.io.PrintStream;
import javax.media.*;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media.protocol:
//            Streamable

public class DelegateDataSource extends PushBufferDataSource
    implements Streamable
{
    class DelegateStream
        implements PushBufferStream, BufferTransferHandler
    {

        public void setMaster(PushBufferStream master)
        {
            this.master = master;
            master.setTransferHandler(this);
        }

        public PushBufferStream getMaster()
        {
            return master;
        }

        public Format getFormat()
        {
            if(master != null)
                return master.getFormat();
            else
                return format;
        }

        public ContentDescriptor getContentDescriptor()
        {
            if(master != null)
                return master.getContentDescriptor();
            else
                return new ContentDescriptor("raw");
        }

        public long getContentLength()
        {
            if(master != null)
                return master.getContentLength();
            else
                return -1L;
        }

        public boolean endOfStream()
        {
            if(master != null)
                return master.endOfStream();
            else
                return false;
        }

        public void read(Buffer buffer)
            throws IOException
        {
            if(master != null)
                master.read(buffer);
            throw new IOException("No data available");
        }

        public void setTransferHandler(BufferTransferHandler transferHandler)
        {
            th = transferHandler;
        }

        public void transferData(PushBufferStream stream)
        {
            if(th != null)
                th.transferData(stream);
        }

        public Object[] getControls()
        {
            if(master != null)
                return master.getControls();
            else
                return new Object[0];
        }

        public Object getControl(String controlType)
        {
            if(master != null)
                return master.getControl(controlType);
            else
                return null;
        }

        Format format;
        PushBufferStream master;
        BufferTransferHandler th;

        public DelegateStream(Format format)
        {
            this.format = format;
        }
    }


    public DelegateDataSource(Format format[])
    {
        contentType = "raw";
        started = false;
        connected = false;
        streams = new DelegateStream[format.length];
        for(int i = 0; i < format.length; i++)
            streams[i] = new DelegateStream(format[i]);

        try
        {
            connect();
        }
        catch(IOException e) { }
    }

    public void setMaster(PushBufferDataSource ds)
        throws IOException
    {
        master = ds;
        PushBufferStream mstrms[] = ds.getStreams();
        for(int i = 0; i < mstrms.length; i++)
        {
            for(int j = 0; j < streams.length; j++)
                if(streams[j].getFormat().matches(mstrms[i].getFormat()))
                    streams[j].setMaster(mstrms[i]);

        }

        for(int i = 0; i < mstrms.length; i++)
            if(streams[i].getMaster() == null)
                Log.error("DelegateDataSource: cannot not find a matching track from the master with this format: " + streams[i].getFormat());

        if(connected)
            master.connect();
        if(started)
            master.start();
    }

    public DataSource getMaster()
    {
        return master;
    }

    public PushBufferStream[] getStreams()
    {
        return streams;
    }

    public MediaLocator getLocator()
    {
        if(master != null)
            return master.getLocator();
        else
            return null;
    }

    public String getContentType()
    {
        if(!connected)
        {
            System.err.println("Error: DataSource not connected");
            return null;
        } else
        {
            return contentType;
        }
    }

    public void connect()
        throws IOException
    {
        if(connected)
            return;
        if(master != null)
            master.connect();
        connected = true;
    }

    public void disconnect()
    {
        try
        {
            if(started)
                stop();
        }
        catch(IOException e) { }
        if(master != null)
            master.disconnect();
        connected = false;
    }

    public void start()
        throws IOException
    {
        if(!connected)
            throw new Error("DataSource must be connected before it can be started");
        if(started)
            return;
        if(master != null)
            master.start();
        started = true;
    }

    public void stop()
        throws IOException
    {
        if(!connected || !started)
            return;
        if(master != null)
            master.stop();
        started = false;
    }

    public Object[] getControls()
    {
        if(master != null)
            return master.getControls();
        else
            return new Object[0];
    }

    public Object getControl(String controlType)
    {
        if(master != null)
            return master.getControl(controlType);
        else
            return null;
    }

    public Time getDuration()
    {
        if(master != null)
            return master.getDuration();
        else
            return Duration.DURATION_UNKNOWN;
    }

    public boolean isPrefetchable()
    {
        return false;
    }

    protected String contentType;
    protected PushBufferDataSource master;
    protected DelegateStream streams[];
    protected boolean started;
    protected boolean connected;
}
