// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PauseMessage.java

package com.sun.media.rtsp.protocol;


// Referenced classes of package com.sun.media.rtsp.protocol:
//            RequestMessage

public class PauseMessage extends RequestMessage
{

    public PauseMessage(byte data[])
    {
        super(data);
    }

    public PauseMessage(String url, int sequenceNumber, int sessionId)
    {
        String msg = "PAUSE " + url + "RTSP/1.0" + "\r\n" + "CSeq: " + sequenceNumber + "\r\n" + "Session: " + sessionId + "\r\n";
    }
}
