// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SendStreamEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            RTPEvent

public class SendStreamEvent extends RTPEvent
{

    public SendStreamEvent(SessionManager from, SendStream stream, Participant participant)
    {
        super(from);
        sendStream = null;
        this.participant = null;
        sendStream = stream;
        this.participant = participant;
    }

    public SendStream getSendStream()
    {
        return sendStream;
    }

    public Participant getParticipant()
    {
        return participant;
    }

    private SendStream sendStream;
    private Participant participant;
}
