// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RtspPort.java

package com.sun.media.rtsp;


public abstract class RtspPort
{

    public RtspPort()
    {
    }

    public static void setPort(int current_port)
    {
        port = current_port;
    }

    public static int getPort()
    {
        return port;
    }

    public static final int RTSP_DEFAULT_PORT = 1554;
    public static int port = 1554;

}
