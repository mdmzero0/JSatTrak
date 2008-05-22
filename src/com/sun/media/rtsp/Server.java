// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Server.java

package com.sun.media.rtsp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;

// Referenced classes of package com.sun.media.rtsp:
//            RtspPort, Debug, RtspManager

public class Server extends Thread
{

    public Server(RtspManager rtspManager)
    {
        this.rtspManager = rtspManager;
        try
        {
            serverSocket = new ServerSocket(RtspPort.getPort());
            System.err.println("Server Socket: " + serverSocket.toString());
            System.err.println("Socket is connected to: " + serverSocket.getInetAddress().getLocalHost());
            System.err.println("Local port: " + serverSocket.getLocalPort());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        Debug.println("Server running...");
        if(serverSocket == null)
            return;
        do
            try
            {
                Debug.println("accepting...");
                java.net.Socket socket = serverSocket.accept();
                rtspManager.addConnection(socket);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        while(true);
    }

    public void shutdown()
    {
        try
        {
            Debug.println("...closing server socket");
            serverSocket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private RtspManager rtspManager;
    private ServerSocket serverSocket;
}
