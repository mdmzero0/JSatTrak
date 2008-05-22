// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MergingPullDataSource.java

package com.ibm.media.protocol;

import java.io.IOException;
import javax.media.Time;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;

// Referenced classes of package com.ibm.media.protocol:
//            MergingDataSource

public class MergingPullDataSource extends PullDataSource
{

    public MergingPullDataSource(PullDataSource sources[])
    {
        superClass = new MergingDataSource(sources);
    }

    public PullSourceStream[] getStreams()
    {
        if(streams == null)
        {
            int totalNoOfStreams = 0;
            for(int i = 0; i < superClass.sources.length; i++)
                totalNoOfStreams += ((PullDataSource)superClass.sources[i]).getStreams().length;

            streams = new PullSourceStream[totalNoOfStreams];
            int totalIndex = 0;
            for(int sourceIndex = 0; sourceIndex < superClass.sources.length; sourceIndex++)
            {
                PullSourceStream s[] = ((PullDataSource)superClass.sources[sourceIndex]).getStreams();
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
    PullSourceStream streams[];
}
