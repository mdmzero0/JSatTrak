// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NewParticipantEvent.java

package javax.media.rtp.event;

import javax.media.rtp.Participant;
import javax.media.rtp.SessionManager;

// Referenced classes of package javax.media.rtp.event:
//            SessionEvent

public class NewParticipantEvent extends SessionEvent
{

    public NewParticipantEvent(SessionManager from, Participant participant)
    {
        super(from);
        this.participant = participant;
    }

    public Participant getParticipant()
    {
        return participant;
    }

    private Participant participant;
}
