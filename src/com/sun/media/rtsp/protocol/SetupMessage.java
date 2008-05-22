// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SetupMessage.java

package com.sun.media.rtsp.protocol;


// Referenced classes of package com.sun.media.rtsp.protocol:
//            RequestMessage

public class SetupMessage extends RequestMessage
{

    public SetupMessage(byte data[])
    {
        super(data);
    }

    public SetupMessage(String url, int sequenceNumber, int port_lo, int port_hi)
    {
        String msg = "SETUP " + url + "RTSP/1.0" + "\r\n" + "CSeq: " + sequenceNumber + "\r\n" + "Transport: RTP/AVP;unicast;client_port=" + port_lo + "-" + port_hi;
    }
}
