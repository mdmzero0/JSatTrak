// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CloneablePullDataSource.java

package com.ibm.media.protocol;

import java.io.IOException;
import javax.media.Time;
import javax.media.protocol.*;

// Referenced classes of package com.ibm.media.protocol:
//            SuperCloneableDataSource, CloneableSourceStreamAdapter

public class CloneablePullDataSource extends PullDataSource
    implements SourceCloneable
{

    public CloneablePullDataSource(PullDataSource source)
    {
        superClass = new SuperCloneableDataSource(source);
    }

    public PullSourceStream[] getStreams()
    {
        if(superClass.streams == null)
        {
            superClass.streams = new PullSourceStream[superClass.streamsAdapters.length];
            for(int i = 0; i < superClass.streamsAdapters.length; i++)
                superClass.streams[i] = (PullSourceStream)superClass.streamsAdapters[i].getAdapter();

        }
        return (PullSourceStream[])superClass.streams;
    }

    public DataSource createClone()
    {
        return superClass.createClone();
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

    public Object[] getControls()
    {
        return superClass.getControls();
    }

    public Object getControl(String controlType)
    {
        return superClass.getControl(controlType);
    }

    public Time getDuration()
    {
        return superClass.getDuration();
    }

    private SuperCloneableDataSource superClass;
}
