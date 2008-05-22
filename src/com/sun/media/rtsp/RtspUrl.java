// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RtspUrl.java

package com.sun.media.rtsp;

import java.net.MalformedURLException;

public class RtspUrl
{

    public RtspUrl(String url)
        throws MalformedURLException
    {
        this.url = url;
        if(url.length() < 7)
            throw new MalformedURLException();
        if(!url.startsWith("rtsp://"))
            throw new MalformedURLException();
        else
            return;
    }

    public String getFile()
    {
        String str = url.substring(7);
        int start = str.indexOf('/');
        String file = "";
        if(start != -1)
            file = str.substring(start + 1);
        return file;
    }

    public String getHost()
    {
        String host = null;
        String str = url.substring(7);
        int end = str.indexOf(':');
        if(end == -1)
        {
            end = str.indexOf('/');
            if(end == -1)
                host = str;
            else
                host = str.substring(0, end);
        } else
        {
            host = str.substring(0, end);
        }
        return host;
    }

    public int getPort()
    {
        int port = 554;
        String str = url.substring(7);
        int start = str.indexOf(':');
        if(start != -1)
        {
            int end = str.indexOf('/');
            port = (new Integer(str.substring(start + 1, end))).intValue();
        }
        return port;
    }

    private String url;
}
