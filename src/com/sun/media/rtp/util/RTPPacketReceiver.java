// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPPacketReceiver.java

package com.sun.media.rtp.util;

import com.sun.media.CircularBuffer;
import java.io.IOException;
import javax.media.Buffer;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import javax.media.rtp.RTPPushDataSource;

// Referenced classes of package com.sun.media.rtp.util:
//            UDPPacket, PacketSource, Packet

public class RTPPacketReceiver
    implements PacketSource, SourceTransferHandler
{

    public RTPPacketReceiver(RTPPushDataSource rtpsource)
    {
        this.rtpsource = null;
        bufQue = new CircularBuffer(2);
        closed = false;
        dataRead = false;
        this.rtpsource = rtpsource;
        PushSourceStream output = rtpsource.getOutputStream();
        output.setTransferHandler(this);
    }

    public RTPPacketReceiver(PushSourceStream pss)
    {
        rtpsource = null;
        bufQue = new CircularBuffer(2);
        closed = false;
        dataRead = false;
        pss.setTransferHandler(this);
    }

    public void transferData(PushSourceStream sourcestream)
    {
        Buffer buf;
        synchronized(bufQue)
        {
            while(!bufQue.canWrite() && !closed) 
                try
                {
                    bufQue.wait(1000L);
                }
                catch(InterruptedException e) { }
            if(closed)
                return;
            buf = bufQue.getEmptyBuffer();
        }
        int size = sourcestream.getMinimumTransferSize();
        byte data[] = (byte[])buf.getData();
        int len = 0;
        if(data == null || data.length < size)
        {
            data = new byte[size];
            buf.setData(data);
        }
        try
        {
            len = sourcestream.read(data, 0, size);
        }
        catch(IOException e) { }
        buf.setLength(len);
        buf.setOffset(0);
        synchronized(bufQue)
        {
            bufQue.writeReport();
            bufQue.notify();
        }
    }

    public Packet receiveFrom()
        throws IOException
    {
        Buffer buf;
        synchronized(bufQue)
        {
            if(dataRead)
            {
                bufQue.readReport();
                bufQue.notify();
            }
            while(!bufQue.canRead() && !closed) 
                try
                {
                    bufQue.wait(1000L);
                }
                catch(InterruptedException e) { }
            if(closed)
            {
                buf = null;
                dataRead = false;
            } else
            {
                buf = bufQue.read();
                dataRead = true;
            }
        }
        byte data[];
        if(buf != null)
            data = (byte[])buf.getData();
        else
            data = new byte[1];
        UDPPacket p = new UDPPacket();
        p.receiptTime = System.currentTimeMillis();
        p.data = data;
        p.offset = 0;
        p.length = buf != null ? buf.getLength() : 0;
        return p;
    }

    public void closeSource()
    {
        synchronized(bufQue)
        {
            closed = true;
            bufQue.notifyAll();
        }
    }

    public String sourceString()
    {
        String s = "RTPPacketReceiver for " + rtpsource;
        return s;
    }

    RTPPushDataSource rtpsource;
    CircularBuffer bufQue;
    boolean closed;
    boolean dataRead;
}
