// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPPushDataSource.java

package javax.media.rtp;

import java.io.IOException;
import java.io.PrintStream;
import javax.media.Time;
import javax.media.protocol.*;

// Referenced classes of package javax.media.rtp:
//            RTPControl, OutputDataStream

/**
 * @deprecated Class RTPPushDataSource is deprecated
 */

public class RTPPushDataSource extends PushDataSource
{

    public RTPPushDataSource()
    {
        contentType = null;
        connected = false;
        started = false;
        childsrc = null;
        rtpcontrol = null;
        Class eClass = null;
        try
        {
            eClass = Class.forName("com.sun.media.rtp.RTPControlImpl");
            rtpcontrol = (RTPControl)eClass.newInstance();
        }
        catch(Exception e)
        {
            rtpcontrol = null;
        }
    }

    public void setChild(DataSource source)
    {
        childsrc = source;
    }

    public PushSourceStream getOutputStream()
    {
        return outputstream;
    }

    public OutputDataStream getInputStream()
    {
        return inputstream;
    }

    public void setOutputStream(PushSourceStream outputstream)
    {
        this.outputstream = outputstream;
    }

    public void setInputStream(OutputDataStream inputstream)
    {
        this.inputstream = inputstream;
    }

    public String getContentType()
    {
        if(!connected)
        {
            System.err.println("Error: DataSource not connected");
            return null;
        } else
        {
            return ContentDescriptor.mimeTypeToPackageName(contentType);
        }
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public void connect()
        throws IOException
    {
        connected = true;
        if(childsrc != null)
            childsrc.connect();
    }

    public void disconnect()
    {
        connected = false;
        if(childsrc != null)
            childsrc.disconnect();
    }

    protected void initCheck()
    {
    }

    public void start()
        throws IOException
    {
        if(!connected)
            return;
        started = true;
        if(childsrc != null)
            childsrc.start();
    }

    public void stop()
        throws IOException
    {
        if(!connected && !started)
            return;
        started = false;
        if(childsrc != null)
            childsrc.stop();
    }

    public boolean isStarted()
    {
        return started;
    }

    public Object[] getControls()
    {
        RTPControl controls[] = new RTPControl[1];
        controls[0] = rtpcontrol;
        return controls;
    }

    public Object getControl(String controlName)
    {
        if(controlName.equals("javax.media.rtp.RTPControl"))
            return rtpcontrol;
        else
            return null;
    }

    public Time getDuration()
    {
        return null;
    }

    public PushSourceStream[] getStreams()
    {
        PushSourceStream outstream[] = new PushSourceStream[1];
        outstream[0] = outputstream;
        return outstream;
    }

    PushSourceStream outputstream;
    OutputDataStream inputstream;
    String contentType;
    private boolean connected;
    private boolean started;
    DataSource childsrc;
    private RTPControl rtpcontrol;
}
