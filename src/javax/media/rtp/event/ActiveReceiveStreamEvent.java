// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ActiveReceiveStreamEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            ReceiveStreamEvent

public class ActiveReceiveStreamEvent extends ReceiveStreamEvent
{

    public ActiveReceiveStreamEvent(SessionManager from, Participant participant, ReceiveStream recvStream)
    {
        super(from, recvStream, participant);
    }
}
