// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MergingPushDataSource.java

package com.ibm.media.protocol;

import java.io.IOException;
import javax.media.Time;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;

// Referenced classes of package com.ibm.media.protocol:
//            MergingDataSource

public class MergingPushDataSource extends PushDataSource
{

    public MergingPushDataSource(PushDataSource sources[])
    {
        superClass = new MergingDataSource(sources);
    }

    public PushSourceStream[] getStreams()
    {
        if(streams == null)
        {
            int totalNoOfStreams = 0;
            for(int i = 0; i < superClass.sources.length; i++)
                totalNoOfStreams += ((PushDataSource)superClass.sources[i]).getStreams().length;

            streams = new PushSourceStream[totalNoOfStreams];
            int totalIndex = 0;
            for(int sourceIndex = 0; sourceIndex < superClass.sources.length; sourceIndex++)
            {
                PushSourceStream s[] = ((PushDataSource)superClass.sources[sourceIndex]).getStreams();
                for(int streamIndex = 0; streamIndex < s.length; streamIndex++)
                    streams[totalIndex++] = s[streamIndex];

            }

        }
        return streams;
    }

    public String getContentType()
    {
        return superClass.getContentType();
    }

    public void connect()
        throws IOException
    {
        superClass.connect();
    }

    public void disconnect()
    {
        superClass.disconnect();
    }

    public void start()
        throws IOException
    {
        superClass.start();
    }

    public void stop()
        throws IOException
    {
        superClass.stop();
    }

    public Time getDuration()
    {
        return superClass.getDuration();
    }

    public Object[] getControls()
    {
        return superClass.getControls();
    }

    public Object getControl(String controlType)
    {
        return superClass.getControl(controlType);
    }

    MergingDataSource superClass;
    PushSourceStream streams[];
}
