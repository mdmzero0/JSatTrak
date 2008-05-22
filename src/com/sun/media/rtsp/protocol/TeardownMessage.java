// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TeardownMessage.java

package com.sun.media.rtsp.protocol;


// Referenced classes of package com.sun.media.rtsp.protocol:
//            RequestMessage

public class TeardownMessage extends RequestMessage
{

    public TeardownMessage(byte data[])
    {
        super(data);
    }

    public TeardownMessage(String url, int sequenceNumber, int sessionId)
    {
        String msg = "TEARDOWN " + url + "RTSP/1.0" + "\r\n" + "CSeq: " + sequenceNumber + "\r\n" + "Session: " + sessionId + "\r\n";
    }
}
