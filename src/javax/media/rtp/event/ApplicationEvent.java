// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ApplicationEvent.java

package javax.media.rtp.event;

import javax.media.rtp.*;

// Referenced classes of package javax.media.rtp.event:
//            ReceiveStreamEvent

public class ApplicationEvent extends ReceiveStreamEvent
{

    public ApplicationEvent(SessionManager from, Participant participant, ReceiveStream recvStream, int appSubtype, String appString, byte appData[])
    {
        super(from, recvStream, participant);
        this.appSubtype = appSubtype;
        this.appString = appString;
        this.appData = appData;
    }

    public int getAppSubType()
    {
        return appSubtype;
    }

    public String getAppString()
    {
        return appString;
    }

    public byte[] getAppData()
    {
        return appData;
    }

    private int appSubtype;
    private String appString;
    private byte appData[];
}
