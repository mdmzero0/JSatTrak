// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package com.sun.media.protocol.javasound;

import com.sun.media.JMFSecurityManager;
import com.sun.media.protocol.BasicPushBufferDataSource;
import com.sun.media.protocol.BasicSourceStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.media.CaptureDeviceInfo;
import javax.media.Duration;
import javax.media.Time;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.RateConfiguration;
import javax.media.protocol.RateConfigureable;
import javax.media.protocol.RateRange;
import javax.media.protocol.SourceStream;

// Referenced classes of package com.sun.media.protocol.javasound:
//            JavaSoundSourceStream

public class DataSource extends BasicPushBufferDataSource
    implements CaptureDevice, RateConfigureable
{
    class OneRateConfig
        implements RateConfiguration
    {

        public RateRange getRate()
        {
            return new RateRange(1.0F, 1.0F, 1.0F, true);
        }

        public SourceStream[] getStreams()
        {
            SourceStream ss[] = {
                sourceStream
            };
            return ss;
        }

        OneRateConfig()
        {
        }
    }


    public DataSource()
    {
        streams = new PushBufferStream[0];
        sourceStream = null;
        started = false;
        JMFSecurityManager.checkCapture();
        contentType = ContentType;
        duration = Duration.DURATION_UNBOUNDED;
        sourceStream = new JavaSoundSourceStream(this);
        streams = new PushBufferStream[1];
        streams[0] = sourceStream;
    }

    public static CaptureDeviceInfo[] listCaptureDeviceInfo()
    {
        return JavaSoundSourceStream.listCaptureDeviceInfo();
    }

    public CaptureDeviceInfo getCaptureDeviceInfo()
    {
        return JavaSoundSourceStream.listCaptureDeviceInfo()[0];
    }

    public FormatControl[] getFormatControls()
    {
        FormatControl fc[] = new FormatControl[1];
        fc[0] = (FormatControl)sourceStream.getControl("javax.media.control.FormatControl");
        return fc;
    }

    public PushBufferStream[] getStreams()
    {
        if(streams == null)
            System.err.println("DataSource needs to be connected before calling getStreams");
        return streams;
    }

    public void connect()
        throws IOException
    {
        if(sourceStream.isConnected())
            return;
        if(getLocator() != null)
            sourceStream.setFormat(JavaSoundSourceStream.parseLocator(getLocator()));
        sourceStream.connect();
    }

    public void disconnect()
    {
        sourceStream.disconnect();
    }

    public void start()
        throws IOException
    {
        sourceStream.start();
    }

    public void stop()
        throws IOException
    {
        sourceStream.stop();
    }

    public String getContentType()
    {
        return contentType;
    }

    public Time getDuration()
    {
        return duration;
    }

    boolean getStarted()
    {
        return started;
    }

    public Object[] getControls()
    {
        Object o[] = sourceStream.getControls();
        return o;
    }

    public Object getControl(String name)
    {
        return sourceStream.getControl(name);
    }

    public RateConfiguration[] getRateConfigurations()
    {
        RateConfiguration config[] = {
            new OneRateConfig()
        };
        return config;
    }

    public RateConfiguration setRateConfiguration(RateConfiguration config)
    {
        return config;
    }

    PushBufferStream streams[];
    JavaSoundSourceStream sourceStream;
    String contentType;
    Time duration;
    boolean started;
    static String ContentType = "raw";

}
