// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RequestLine.java

package com.sun.media.rtsp.protocol;

import java.io.ByteArrayInputStream;

// Referenced classes of package com.sun.media.rtsp.protocol:
//            Parser, Debug

public class RequestLine extends Parser
{

    public RequestLine(String input)
    {
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes());
        String method = getToken(bin);
        Debug.println("method  : " + method);
        url = getToken(bin);
        Debug.println("url     : " + url);
        version = getToken(bin);
        Debug.println("version : " + version);
    }

    public String getUrl()
    {
        return url;
    }

    public String getVersion()
    {
        return version;
    }

    private String url;
    private String version;
}
