// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   InactiveReceiveStreamEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            ReceiveStreamEvent

public class InactiveReceiveStreamEvent extends ReceiveStreamEvent
{

    public InactiveReceiveStreamEvent(SessionManager from, Participant participant, ReceiveStream recvStream, boolean laststream)
    {
        super(from, recvStream, participant);
        this.laststream = laststream;
    }

    public boolean isLastStream()
    {
        return laststream;
    }

    private boolean laststream;
}
