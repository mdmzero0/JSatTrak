// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Connection.java

package com.sun.media.rtsp;

import java.io.*;
import java.net.*;

// Referenced classes of package com.sun.media.rtsp:
//            MessageProcessor, Debug, RtspManager

public class Connection extends Thread
    implements Runnable
{

    public Connection(RtspManager rtspManager, int connectionId, byte dstAddress[], int port)
        throws UnknownHostException, ConnectException
    {
        this.rtspManager = rtspManager;
        this.connectionId = connectionId;
        String domain = new String(dstAddress);
        InetAddress dst = InetAddress.getByName(domain);
        try
        {
            socket = new Socket(dst, port);
            start();
        }
        catch(IOException e)
        {
            throw new ConnectException();
        }
    }

    public Connection(RtspManager rtspManager, int connectionId, Socket socket)
    {
        this.rtspManager = rtspManager;
        this.connectionId = connectionId;
        this.socket = socket;
        start();
    }

    public boolean sendData(byte message[])
    {
        boolean success = false;
        try
        {
            OutputStream out = socket.getOutputStream();
            out.write(message);
            out.flush();
            success = true;
        }
        catch(IOException e) { }
        return success;
    }

    public void run()
    {
        connectionIsAlive = true;
        while(connectionIsAlive) 
            try
            {
                java.io.InputStream in = socket.getInputStream();
                DataInputStream din = new DataInputStream(in);
                byte ch = din.readByte();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(ch);
                for(; !eomReached(baos.toByteArray()); baos.write(din.readByte()));
                int length = getContentLength(new String(baos.toByteArray()));
                for(int i = 0; i < length; i++)
                    baos.write(din.readByte());

                if(mp == null)
                    mp = new MessageProcessor(connectionId, rtspManager);
                mp.processMessage(baos.toByteArray());
            }
            catch(Exception e)
            {
                connectionIsAlive = false;
            }
    }

    private boolean eomReached(byte buffer[])
    {
        boolean endReached = false;
        int size = buffer.length;
        if(size >= 4 && buffer[size - 4] == 13 && buffer[size - 3] == 10 && buffer[size - 2] == 13 && buffer[size - 1] == 10)
            endReached = true;
        return endReached;
    }

    private int getContentLength(String msg_header)
    {
        int start = msg_header.indexOf("Content-length");
        if(start == -1)
            start = msg_header.indexOf("Content-Length");
        int length;
        if(start == -1)
        {
            length = 0;
        } else
        {
            start = msg_header.indexOf(':', start) + 2;
            int end = msg_header.indexOf('\r', start);
            String length_str = msg_header.substring(start, end);
            length = (new Integer(length_str)).intValue();
        }
        return length;
    }

    public void cleanup()
    {
        Debug.println("RTSP::Connection:cleanup, id=" + connectionId);
        close();
        rtspManager.removeConnection(connectionId);
    }

    public void close()
    {
        connectionIsAlive = false;
        try
        {
            if(socket != null)
            {
                socket.close();
                socket = null;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getIpAddress()
    {
        return socket.getInetAddress().getHostAddress();
    }

    public int connectionId;
    private Socket socket;
    private RtspManager rtspManager;
    private MessageProcessor mp;
    private boolean connectionIsAlive;
}
