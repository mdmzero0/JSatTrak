// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReceiveStreamEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            RTPEvent

public class ReceiveStreamEvent extends RTPEvent
{

    public ReceiveStreamEvent(SessionManager from, ReceiveStream stream, Participant participant)
    {
        super(from);
        recvStream = null;
        this.participant = null;
        recvStream = stream;
        this.participant = participant;
    }

    public ReceiveStream getReceiveStream()
    {
        return recvStream;
    }

    public Participant getParticipant()
    {
        return participant;
    }

    private ReceiveStream recvStream;
    private Participant participant;
}
