// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StreamMappedEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            ReceiveStreamEvent

public class StreamMappedEvent extends ReceiveStreamEvent
{

    public StreamMappedEvent(SessionManager from, ReceiveStream recvStream, Participant participant)
    {
        super(from, recvStream, participant);
    }
}
