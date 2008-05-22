// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SessionEvent.java

package javax.media.rtp.event;

import javax.media.rtp.SessionManager;

// Referenced classes of package javax.media.rtp.event:
//            RTPEvent

public class SessionEvent extends RTPEvent
{

    public SessionEvent(SessionManager from)
    {
        super(from);
    }
}
