// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NewSendStreamEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            SendStreamEvent

public class NewSendStreamEvent extends SendStreamEvent
{

    public NewSendStreamEvent(SessionManager from, SendStream sendStream)
    {
        super(from, sendStream, sendStream.getParticipant());
    }
}
