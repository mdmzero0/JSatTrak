// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RemoteEvent.java

package javax.media.rtp.event;

import javax.media.rtp.SessionManager;

// Referenced classes of package javax.media.rtp.event:
//            RTPEvent

public class RemoteEvent extends RTPEvent
{

    public RemoteEvent(SessionManager from)
    {
        super(from);
    }
}
