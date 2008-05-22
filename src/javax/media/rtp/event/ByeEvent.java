// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ByeEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            TimeoutEvent

public class ByeEvent extends TimeoutEvent
{

    public ByeEvent(SessionManager from, Participant participant, ReceiveStream recvStream, String reason, boolean participantBye)
    {
        super(from, participant, recvStream, participantBye);
        this.reason = reason;
    }

    public String getReason()
    {
        return reason;
    }

    private String reason;
}
