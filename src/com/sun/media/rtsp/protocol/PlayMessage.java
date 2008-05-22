// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PlayMessage.java

package com.sun.media.rtsp.protocol;


// Referenced classes of package com.sun.media.rtsp.protocol:
//            RequestMessage

public class PlayMessage extends RequestMessage
{

    public PlayMessage(byte data[])
    {
        super(data);
    }

    public PlayMessage(String url, int sequenceNumber, int sessionId, int range_lo, int range_hi)
    {
        String msg = "PLAY " + url + "RTSP/1.0" + "\r\n" + "CSeq: " + sequenceNumber + "\r\n" + "Session: " + sessionId + "\r\n" + "Range: npt=" + range_lo + "-" + range_hi;
    }
}
