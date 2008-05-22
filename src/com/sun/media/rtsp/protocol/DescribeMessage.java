// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DescribeMessage.java

package com.sun.media.rtsp.protocol;


// Referenced classes of package com.sun.media.rtsp.protocol:
//            RequestMessage

public class DescribeMessage extends RequestMessage
{

    public DescribeMessage(byte data[])
    {
        super(data);
    }

    public DescribeMessage(String url, int sequenceNumber)
    {
        String msg = "DESCRIBE " + url + "RTSP/1.0" + "\r\n" + "CSeq: " + sequenceNumber + "\r\n\r\n";
    }
}
