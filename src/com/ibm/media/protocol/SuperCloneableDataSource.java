// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SuperCloneableDataSource.java

package com.ibm.media.protocol;

import java.io.IOException;
import java.util.Vector;
import javax.media.Time;
import javax.media.protocol.*;

// Referenced classes of package com.ibm.media.protocol:
//            CloneableSourceStreamAdapter, SourceStreamSlave

class SuperCloneableDataSource extends DataSource
{
    class PushBufferDataSourceSlave extends PushBufferDataSource
    {

        public String getContentType()
        {
            return input.getContentType();
        }

        public void connect()
            throws IOException
        {
            for(int i = 0; i < streams.length; i++)
                ((SourceStreamSlave)streams[i]).connect();

        }

        public void disconnect()
        {
            for(int i = 0; i < streams.length; i++)
                ((SourceStreamSlave)streams[i]).disconnect();

        }

        public void start()
            throws IOException
        {
        }

        public void stop()
            throws IOException
        {
        }

        public PushBufferStream[] getStreams()
        {
            return streams;
        }

        public Object[] getControls()
        {
            return input.getControls();
        }

        public Object getControl(String controlType)
        {
            return input.getControl(controlType);
        }

        public Time getDuration()
        {
            return input.getDuration();
        }

        PushBufferStream streams[];

        public PushBufferDataSourceSlave()
        {
            streams = null;
            streams = new PushBufferStream[streamsAdapters.length];
            for(int i = 0; i < streams.length; i++)
                streams[i] = (PushBufferStream)streamsAdapters[i].createSlave();

        }
    }

    class PushDataSourceSlave extends PushDataSource
    {

        public String getContentType()
        {
            return input.getContentType();
        }

        public void connect()
            throws IOException
        {
            for(int i = 0; i < streams.length; i++)
                ((SourceStreamSlave)streams[i]).connect();

        }

        public void disconnect()
        {
            for(int i = 0; i < streams.length; i++)
                ((SourceStreamSlave)streams[i]).disconnect();

        }

        public void start()
            throws IOException
        {
        }

        public void stop()
            throws IOException
        {
        }

        public PushSourceStream[] getStreams()
        {
            return streams;
        }

        public Object[] getControls()
        {
            return input.getControls();
        }

        public Object getControl(String controlType)
        {
            return input.getControl(controlType);
        }

        public Time getDuration()
        {
            return input.getDuration();
        }

        PushSourceStream streams[];

        public PushDataSourceSlave()
        {
            streams = null;
            streams = new PushSourceStream[streamsAdapters.length];
            for(int i = 0; i < streams.length; i++)
                streams[i] = (PushSourceStream)streamsAdapters[i].createSlave();

        }
    }


    SuperCloneableDataSource(DataSource input)
    {
        streams = null;
        clones = new Vector();
        this.input = input;
        SourceStream originalStreams[] = null;
        if(input instanceof PullDataSource)
            originalStreams = ((PullDataSource)input).getStreams();
        if(input instanceof PushDataSource)
            originalStreams = ((PushDataSource)input).getStreams();
        if(input instanceof PullBufferDataSource)
            originalStreams = ((PullBufferDataSource)input).getStreams();
        if(input instanceof PushBufferDataSource)
            originalStreams = ((PushBufferDataSource)input).getStreams();
        streamsAdapters = new CloneableSourceStreamAdapter[originalStreams.length];
        for(int i = 0; i < originalStreams.length; i++)
            streamsAdapters[i] = new CloneableSourceStreamAdapter(originalStreams[i]);

    }

    DataSource createClone()
    {
        DataSource newSlave;
        if((input instanceof PullDataSource) || (input instanceof PushDataSource))
            newSlave = new PushDataSourceSlave();
        else
            newSlave = new PushBufferDataSourceSlave();
        clones.addElement(newSlave);
        try
        {
            newSlave.connect();
        }
        catch(IOException e)
        {
            return null;
        }
        return newSlave;
    }

    public String getContentType()
    {
        return input.getContentType();
    }

    public void connect()
        throws IOException
    {
        input.connect();
    }

    public void disconnect()
    {
        input.disconnect();
    }

    public void start()
        throws IOException
    {
        input.start();
    }

    public void stop()
        throws IOException
    {
        input.stop();
    }

    public Object[] getControls()
    {
        return input.getControls();
    }

    public Object getControl(String controlType)
    {
        return input.getControl(controlType);
    }

    public Time getDuration()
    {
        return input.getDuration();
    }

    protected DataSource input;
    public CloneableSourceStreamAdapter streamsAdapters[];
    public SourceStream streams[];
    private Vector clones;
}
