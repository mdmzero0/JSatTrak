// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TimeoutEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            ReceiveStreamEvent

public class TimeoutEvent extends ReceiveStreamEvent
{

    public TimeoutEvent(SessionManager from, Participant participant, ReceiveStream recvStream, boolean participantBye)
    {
        super(from, recvStream, participant);
        this.participantBye = participantBye;
    }

    public boolean participantLeaving()
    {
        return participantBye;
    }

    private boolean participantBye;
}
