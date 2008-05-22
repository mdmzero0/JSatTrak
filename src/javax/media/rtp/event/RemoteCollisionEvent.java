// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RemoteCollisionEvent.java

package javax.media.rtp.event;

import javax.media.rtp.SessionManager;

// Referenced classes of package javax.media.rtp.event:
//            RemoteEvent

public class RemoteCollisionEvent extends RemoteEvent
{

    public RemoteCollisionEvent(SessionManager from, long ssrc)
    {
        super(from);
        collidingSSRC = ssrc;
    }

    public long getSSRC()
    {
        return collidingSSRC;
    }

    private long collidingSSRC;
}
