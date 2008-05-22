// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package com.sun.media.protocol.rtp;

import com.sun.media.protocol.BasicPushBufferDataSource;
import com.sun.media.protocol.BufferListener;
import com.sun.media.protocol.RTPSource;
import com.sun.media.protocol.Streamable;
import com.sun.media.rtp.RTPControlImpl;
import com.sun.media.rtp.RTPSessionMgr;
import com.sun.media.rtp.RTPSourceStream;
import com.sun.media.rtp.SSRCInfo;
import java.io.IOException;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPControl;

public class DataSource extends BasicPushBufferDataSource
    implements Streamable, RTPSource
{
    class MyRTPControl extends RTPControlImpl
    {

        public int getSSRC()
        {
            return ssrc;
        }

        public String getCNAME()
        {
            if(mgr == null)
                return null;
            SSRCInfo info = mgr.getSSRCInfo(ssrc);
            if(info != null)
                return info.getCNAME();
            else
                return null;
        }

        MyRTPControl()
        {
        }
    }


    public void setMgr(RTPSessionMgr mgr)
    {
        this.mgr = mgr;
    }

    public RTPSessionMgr getMgr()
    {
        return mgr;
    }

    public void setChild(DataSource source)
    {
        childsrc = source;
    }

    public DataSource()
    {
        srcStreams = null;
        stopped = true;
        streamplayer = null;
        mgr = null;
        rtpcontrol = null;
        childsrc = null;
        ssrc = SSRC_UNDEFINED;
        srcStreams = new RTPSourceStream[1];
        rtpcontrol = new MyRTPControl();
        setContentType("rtp");
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

    public void setSourceStream(RTPSourceStream stream)
    {
        if(srcStreams != null)
            srcStreams[0] = stream;
    }

    public void setBufferListener(BufferListener listener)
    {
        srcStreams[0].setBufferListener(listener);
    }

    public void setLocator(MediaLocator mrl)
    {
        super.setLocator(mrl);
    }

    public void setBufferWhenStopped(boolean flag)
    {
        srcStreams[0].setBufferWhenStopped(flag);
    }

    public void prebuffer()
    {
        super.started = true;
        srcStreams[0].prebuffer();
    }

    public void flush()
    {
        srcStreams[0].reset();
    }

    public void setSSRC(int ssrc)
    {
        this.ssrc = ssrc;
    }

    public int getSSRC()
    {
        return ssrc;
    }

    public String getCNAME()
    {
        if(mgr == null)
            return null;
        SSRCInfo info = mgr.getSSRCInfo(ssrc);
        if(info != null)
            return info.getCNAME();
        else
            return null;
    }

    public void start()
        throws IOException
    {
        super.start();
        if(childsrc != null)
            childsrc.start();
        if(srcStreams != null)
        {
            for(int i = 0; i < srcStreams.length; i++)
                srcStreams[i].start();

        }
    }

    public void stop()
        throws IOException
    {
        super.stop();
        if(childsrc != null)
            childsrc.stop();
        if(srcStreams != null)
        {
            for(int i = 0; i < srcStreams.length; i++)
                srcStreams[i].stop();

        }
    }

    public void setContentType(String contentType)
    {
        super.contentType = contentType;
    }

    public boolean isStarted()
    {
        return super.started;
    }

    public void connect()
        throws IOException
    {
        if(srcStreams != null)
        {
            for(int i = 0; i < srcStreams.length; i++)
                if(srcStreams[i] != null)
                    srcStreams[i].connect();

        }
        super.connected = true;
    }

    public void disconnect()
    {
        if(srcStreams != null)
        {
            for(int i = 0; i < srcStreams.length; i++)
                srcStreams[i].close();

        }
    }

    public Object[] getControls()
    {
        RTPControl controls[] = new RTPControl[1];
        controls[0] = rtpcontrol;
        return controls;
    }

    public void setControl(Object control)
    {
        rtpcontrol = (RTPControl)control;
    }

    public Object getControl(String type)
    {
        Class cls;
        try
        {
            cls = Class.forName(type);
        }
        catch(ClassNotFoundException e)
        {
            return null;
        }
        Object cs[] = getControls();
        for(int i = 0; i < cs.length; i++)
            if(cls.isInstance(cs[i]))
                return cs[i];

        return null;
    }

    public boolean isPrefetchable()
    {
        return false;
    }

    static int SSRC_UNDEFINED = 0;
    private RTPSourceStream srcStreams[];
    private boolean stopped;
    Player streamplayer;
    RTPSessionMgr mgr;
    RTPControl rtpcontrol;
    DataSource childsrc;
    int ssrc;

}
