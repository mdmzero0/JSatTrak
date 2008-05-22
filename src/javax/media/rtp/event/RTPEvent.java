// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPEvent.java

package javax.media.rtp.event;

import javax.media.MediaEvent;
import javax.media.rtp.SessionManager;

public class RTPEvent extends MediaEvent
{

    public RTPEvent(SessionManager from)
    {
        super(from);
        eventSrc = from;
    }

    public Object getSource()
    {
        return eventSrc;
    }

    public SessionManager getSessionManager()
    {
        return eventSrc;
    }

    public String toString()
    {
        return getClass().getName() + "[source = " + eventSrc + "]";
    }

    private SessionManager eventSrc;
}
