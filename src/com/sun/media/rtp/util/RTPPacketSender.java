// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPPacketSender.java

package com.sun.media.rtp.util;

import java.io.IOException;
import javax.media.rtp.*;

// Referenced classes of package com.sun.media.rtp.util:
//            PacketConsumer, Packet

public class RTPPacketSender
    implements PacketConsumer
{

    public RTPPacketSender(RTPPushDataSource dest)
    {
        this.dest = null;
        connector = null;
        outstream = null;
        this.dest = dest;
        outstream = dest.getInputStream();
    }

    public RTPPacketSender(RTPConnector connector)
        throws IOException
    {
        dest = null;
        this.connector = null;
        outstream = null;
        this.connector = connector;
        outstream = connector.getDataOutputStream();
    }

    public RTPPacketSender(OutputDataStream os)
    {
        dest = null;
        connector = null;
        outstream = null;
        outstream = os;
    }

    public void sendTo(Packet p)
        throws IOException
    {
        if(outstream == null)
        {
            throw new IOException();
        } else
        {
            outstream.write(p.data, 0, p.length);
            return;
        }
    }

    public void closeConsumer()
    {
    }

    public RTPConnector getConnector()
    {
        return connector;
    }

    public String consumerString()
    {
        String s = "RTPPacketSender for " + dest;
        return s;
    }

    RTPPushDataSource dest;
    RTPConnector connector;
    OutputDataStream outstream;
}
