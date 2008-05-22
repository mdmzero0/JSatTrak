// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RemotePayloadChangeEvent.java

package javax.media.rtp.event;

import javax.media.rtp.ReceiveStream;
import javax.media.rtp.SessionManager;

// Referenced classes of package javax.media.rtp.event:
//            ReceiveStreamEvent

public class RemotePayloadChangeEvent extends ReceiveStreamEvent
{

    public RemotePayloadChangeEvent(SessionManager from, ReceiveStream recvStream, int oldpayload, int newpayload)
    {
        super(from, recvStream, null);
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
