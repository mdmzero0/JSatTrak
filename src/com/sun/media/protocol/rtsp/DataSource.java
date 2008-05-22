// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package com.sun.media.protocol.rtsp;

import com.sun.media.protocol.BasicPushBufferDataSource;
import java.io.IOException;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.protocol.PushBufferStream;

public class DataSource extends BasicPushBufferDataSource
{

    public DataSource()
    {
        srcStreams = null;
        stopped = true;
        streamplayer = null;
        srcStreams = new PushBufferStream[1];
    }

    public PushBufferStream[] getStreams()
    {
        if(!super.connected)
            return null;
        else
            return srcStreams;
    }

    public void setPlayer(Player player)
    {
        streamplayer = player;
    }

    public Player getPlayer()
    {
        return streamplayer;
    }

    public void setSourceStream(PushBufferStream stream)
    {
        if(srcStreams != null)
            srcStreams[0] = stream;
    }

    public void setLocator(MediaLocator mrl)
    {
        super.setLocator(mrl);
    }

    public void start()
        throws IOException
    {
        super.start();
    }

    public void stop()
        throws IOException
    {
        super.stop();
    }

    public String getContentType()
    {
        return "rtsp";
    }

    public boolean isStarted()
    {
        return super.started;
    }

    public void connect()
        throws IOException
    {
        super.connected = true;
    }

    public void disconnect()
    {
        super.connected = false;
    }

    public Object[] getControls()
    {
        return null;
    }

    public void setControl(Object obj)
    {
    }

    public Object getControl(String controlName)
    {
        return null;
    }

    private PushBufferStream srcStreams[];
    private boolean stopped;
    Player streamplayer;
}
