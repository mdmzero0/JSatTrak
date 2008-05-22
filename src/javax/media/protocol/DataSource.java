// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package javax.media.protocol;

import java.io.IOException;
import javax.media.*;

// Referenced classes of package javax.media.protocol:
//            Controls

public abstract class DataSource
    implements Controls, Duration
{

    public DataSource()
    {
        sourceLocator = null;
    }

    public DataSource(MediaLocator source)
    {
        sourceLocator = null;
        setLocator(source);
    }

    public void setLocator(MediaLocator source)
    {
        if(sourceLocator == null)
            sourceLocator = source;
        else
            throw new Error("Locator already set on DataSource.");
    }

    public MediaLocator getLocator()
    {
        return sourceLocator;
    }

    protected void initCheck()
    {
        if(sourceLocator == null)
            throw new Error("Uninitialized DataSource error.");
        else
            return;
    }

    public abstract String getContentType();

    public abstract void connect()
        throws IOException;

    public abstract void disconnect();

    public abstract void start()
        throws IOException;

    public abstract void stop()
        throws IOException;

    public abstract Object getControl(String s);

    public abstract Object[] getControls();

    public abstract Time getDuration();

    MediaLocator sourceLocator;
}
