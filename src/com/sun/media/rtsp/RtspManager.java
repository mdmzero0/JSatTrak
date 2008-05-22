// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RtspManager.java

package com.sun.media.rtsp;

import com.sun.media.Log;
import com.sun.media.rtsp.protocol.Message;
import java.io.PrintStream;
import java.net.*;
import java.util.Vector;

// Referenced classes of package com.sun.media.rtsp:
//            Server, RtspListener, Connection

public class RtspManager
{

    public RtspManager()
    {
        listeners = new Vector();
        connections = new Vector();
        Server server = new Server(this);
        server.start();
    }

    public RtspManager(boolean server_socket)
    {
        listeners = new Vector();
        connections = new Vector();
        if(server_socket)
        {
            Server server = new Server(this);
            server.start();
        }
    }

    public boolean sendMessage(int connectionId, String message)
    {
        Log.comment("outgoing msg:");
        Log.comment(message);
        Connection connection = getConnection(connectionId);
        boolean success;
        if(connection == null)
            success = false;
        else
            success = connection.sendData(message.getBytes());
        return success;
    }

    public void dataIndication(int connectionId, Message message)
    {
        for(int i = 0; i < listeners.size(); i++)
        {
            RtspListener listener = (RtspListener)listeners.elementAt(i);
            listener.rtspMessageIndication(connectionId, message);
        }

    }

    public void addListener(RtspListener listener)
    {
        listeners.addElement(listener);
    }

    public void removeListener(RtspListener listener)
    {
        listeners.removeElement(listener);
    }

    public int createConnection(String address, int port)
    {
        this.address = address;
        this.port = port;
        int connectionId = -1;
        try
        {
            Connection connection = new Connection(this, connectionCounter + 1, address.getBytes(), port);
            connections.addElement(connection);
            connectionId = connection.connectionId;
            connectionCounter++;
        }
        catch(UnknownHostException e)
        {
            Log.error("[EXCEPTION]: Unknown host.");
            connectionId = -2;
        }
        catch(ConnectException e)
        {
            Log.error("[EXCEPTION]: Can't connect to server.");
            connectionId = -3;
        }
        return connectionId;
    }

    public void addConnection(Socket socket)
    {
        connectionCounter++;
        Connection connection = new Connection(this, connectionCounter, socket);
        connections.addElement(connection);
    }

    public void removeConnection(int connectionId)
    {
        Connection connection = getConnection(connectionId);
        connections.removeElement(connection);
        for(int i = 0; i < listeners.size(); i++)
        {
            RtspListener listener = (RtspListener)listeners.elementAt(i);
            listener.rtspConnectionTerminated(connectionId);
        }

    }

    public void closeConnection(int connectionId)
    {
        Connection connection = getConnection(connectionId);
        if(connection != null)
        {
            connection.close();
            connections.removeElement(connection);
        } else
        {
            System.out.println("connection not found!");
        }
    }

    public Connection getConnection(int connectionId)
    {
        Connection connection = null;
        for(int i = 0; i < connections.size(); i++)
        {
            Connection tmpConnection = (Connection)connections.elementAt(i);
            if(tmpConnection.connectionId != connectionId)
                continue;
            connection = tmpConnection;
            break;
        }

        return connection;
    }

    private Vector listeners;
    private int connectionCounter;
    private Vector connections;
    private String address;
    private int port;
}
