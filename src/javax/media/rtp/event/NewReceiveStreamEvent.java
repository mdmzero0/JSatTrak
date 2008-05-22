// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NewReceiveStreamEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            ReceiveStreamEvent

public class NewReceiveStreamEvent extends ReceiveStreamEvent
{

    public NewReceiveStreamEvent(SessionManager from, ReceiveStream recvStream)
    {
        super(from, recvStream, recvStream.getParticipant());
    }
}
