// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Handler.java

package com.sun.media.datasink.rtp;

import com.sun.media.datasink.BasicDataSink;
import com.sun.media.rtp.RTPMediaLocator;
import java.awt.Component;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import javax.media.IncompatibleSourceException;
import javax.media.MediaLocator;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.rtp.*;

public class Handler extends BasicDataSink
{

    public Handler()
    {
        rtpmrl = null;
        rtpmanager = null;
        source = null;
        rtpsendstream = null;
    }

    public Object getControl(String controlType)
    {
        return null;
    }

    public Object[] getControls()
    {
        return new Object[0];
    }

    public Component getControlComponent()
    {
        return null;
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        if(!(source instanceof PushBufferDataSource))
            throw new IncompatibleSourceException("Only supports PushBufferDataSource");
        this.source = (PushBufferDataSource)source;
        javax.media.protocol.PushBufferStream streams[] = this.source.getStreams();
        int numStreams = streams.length;
        System.out.println("streams is " + streams + " : " + numStreams);
        if(streams == null || numStreams <= 0)
            throw new IOException("source " + source + " doesn't have any streams");
        else
            return;
    }

    public void setOutputLocator(MediaLocator output)
    {
        if(rtpmrl == null)
        {
            System.out.println("sink: setOutputLocator " + output);
            try
            {
                rtpmrl = new RTPMediaLocator(output.toString());
            }
            catch(MalformedURLException e)
            {
                rtpmrl = null;
            }
        } else
        {
            throw new Error("setOutputLocator cannot be called more than once");
        }
    }

    public MediaLocator getOutputLocator()
    {
        return rtpmrl;
    }

    public void start()
        throws IOException
    {
        rtpsendstream.start();
    }

    public void stop()
        throws IOException
    {
        rtpsendstream.stop();
    }

    public void open()
        throws IOException, SecurityException
    {
        if(rtpmrl == null)
            throw new IOException("No Valid RTP MediaLocator");
        try
        {
            String address = rtpmrl.getSessionAddress();
            int port = rtpmrl.getSessionPort();
            int ttl = rtpmrl.getTTL();
            rtpmanager = RTPManager.newInstance();
            SessionAddress localaddr = new SessionAddress();
            InetAddress destaddr = InetAddress.getByName(address);
            SessionAddress sessaddr = new SessionAddress(destaddr, port, ttl);
            rtpmanager.initialize(localaddr);
            rtpmanager.addTarget(sessaddr);
            rtpsendstream = rtpmanager.createSendStream(source, 0);
        }
        catch(Exception e)
        {
            throw new IOException(e.getMessage());
        }
    }

    public void close()
    {
        if(rtpmanager != null)
        {
            rtpmanager.removeTargets("DataSink closed");
            rtpmanager.dispose();
        }
    }

    public String getContentType()
    {
        return "RTP";
    }

    private RTPMediaLocator rtpmrl;
    RTPManager rtpmanager;
    PushBufferDataSource source;
    SendStream rtpsendstream;
}
