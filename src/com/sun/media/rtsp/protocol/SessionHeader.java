// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SessionHeader.java

package com.sun.media.rtsp.protocol;


public class SessionHeader
{

    public SessionHeader(String str)
    {
        int index = str.indexOf(';');
        if(index > 0)
        {
            sessionId = str.substring(0, index);
            str = str.substring(index);
            index = str.indexOf('=');
            String seconds = str.substring(index + 1);
            try
            {
                timeout = (new Long(seconds)).longValue();
            }
            catch(NumberFormatException e)
            {
                timeout = 60L;
            }
        } else
        {
            sessionId = str;
        }
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public long getTimeoutValue()
    {
        return timeout;
    }

    private String sessionId;
    private long timeout;
}
