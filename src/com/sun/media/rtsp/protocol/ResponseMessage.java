// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResponseMessage.java

package com.sun.media.rtsp.protocol;

import java.io.ByteArrayInputStream;

// Referenced classes of package com.sun.media.rtsp.protocol:
//            Response

public class ResponseMessage
{

    public ResponseMessage(byte data[])
    {
        this.data = data;
        parseResponse();
    }

    private void parseResponse()
    {
        response = new Response(new ByteArrayInputStream(data));
    }

    public Response getResponse()
    {
        return response;
    }

    private byte data[];
    private Response response;
}
