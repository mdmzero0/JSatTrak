// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TransportHeader.java

package com.sun.media.rtsp.protocol;


public class TransportHeader
{

    public TransportHeader(String str)
    {
        int end = str.indexOf('/');
        transportProtocol = str.substring(0, end);
        int start = str.indexOf("client_port");
        if(start <= 0);
        start = str.indexOf("server_port");
        if(start > 0)
        {
            start = str.indexOf("=", start) + 1;
            end = str.indexOf("-", start);
            String data_str = str.substring(start, end);
            server_data_port = (new Integer(data_str)).intValue();
            start = end + 1;
            end = str.indexOf(";", start);
            String control_str;
            if(end > 0)
                control_str = str.substring(start, end);
            else
                control_str = str.substring(start);
            server_control_port = (new Integer(control_str)).intValue();
        }
    }

    public String getTransportProtocol()
    {
        return transportProtocol;
    }

    public int getServerDataPort()
    {
        return server_data_port;
    }

    public int getServerControlPort()
    {
        return server_control_port;
    }

    private String transportProtocol;
    private String profile;
    private String lowerTransport;
    private int server_data_port;
    private int server_control_port;
}
