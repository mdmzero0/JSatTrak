// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LocalPayloadChangeEvent.java

package javax.media.rtp.event;

import javax.media.rtp.SendStream;
import javax.media.rtp.SessionManager;

// Referenced classes of package javax.media.rtp.event:
//            SendStreamEvent

public class LocalPayloadChangeEvent extends SendStreamEvent
{

    public LocalPayloadChangeEvent(SessionManager from, SendStream sendStream, int oldpayload, int newpayload)
    {
        super(from, sendStream, null);
        this.oldpayload = oldpayload;
        this.newpayload = newpayload;
    }

    public int getNewPayload()
    {
        return newpayload;
    }

    private int oldpayload;
    private int newpayload;
}
