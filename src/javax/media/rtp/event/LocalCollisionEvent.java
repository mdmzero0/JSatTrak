// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LocalCollisionEvent.java

package javax.media.rtp.event;

import javax.media.rtp.ReceiveStream;
import javax.media.rtp.SessionManager;

// Referenced classes of package javax.media.rtp.event:
//            SessionEvent

public class LocalCollisionEvent extends SessionEvent
{

    public LocalCollisionEvent(SessionManager from, ReceiveStream recvStream, long newSSRC)
    {
        super(from);
        this.recvStream = recvStream;
        this.newSSRC = newSSRC;
    }

    public ReceiveStream getReceiveStream()
    {
        return recvStream;
    }

    public long getNewSSRC()
    {
        return newSSRC;
    }

    private ReceiveStream recvStream;
    private long newSSRC;
}
