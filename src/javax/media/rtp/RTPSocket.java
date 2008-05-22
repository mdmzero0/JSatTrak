// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPSocket.java

package javax.media.rtp;

import java.io.IOException;

// Referenced classes of package javax.media.rtp:
//            RTPPushDataSource, DataChannel

/**
 * @deprecated Class RTPSocket is deprecated
 */

public class RTPSocket extends RTPPushDataSource
    implements DataChannel
{

    public RTPSocket()
    {
        controlsource = null;
        controlsource = new RTPPushDataSource();
    }

    public RTPPushDataSource getControlChannel()
    {
        return controlsource;
    }

    public void setContentType(String contentType)
    {
        super.setContentType(contentType);
        controlsource.setContentType(contentType);
    }

    public void connect()
        throws IOException
    {
        super.connect();
        controlsource.connect();
    }

    public void disconnect()
    {
        super.disconnect();
        controlsource.disconnect();
    }

    public void start()
        throws IOException
    {
        super.start();
        controlsource.start();
    }

    public void stop()
        throws IOException
    {
        super.stop();
        controlsource.stop();
    }

    RTPPushDataSource controlsource;
}
