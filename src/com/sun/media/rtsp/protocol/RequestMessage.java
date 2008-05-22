// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RequestMessage.java

package com.sun.media.rtsp.protocol;

import java.io.ByteArrayInputStream;

// Referenced classes of package com.sun.media.rtsp.protocol:
//            Request

public class RequestMessage
{

    public RequestMessage()
    {
    }

    public RequestMessage(byte data[])
    {
        this.data = data;
        parseRequest();
    }

    private void parseRequest()
    {
        request = new Request(new ByteArrayInputStream(data));
    }

    public Request getRequest()
    {
        return request;
    }

    private byte data[];
    private Request request;
}
