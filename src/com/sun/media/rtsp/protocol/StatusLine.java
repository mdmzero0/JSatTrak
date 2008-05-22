// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StatusLine.java

package com.sun.media.rtsp.protocol;

import java.io.ByteArrayInputStream;

// Referenced classes of package com.sun.media.rtsp.protocol:
//            Parser, Debug

public class StatusLine extends Parser
{

    public StatusLine(String input)
    {
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes());
        String protocol = getToken(bin);
        Debug.println("protocol : " + protocol);
        code = (new Integer(getToken(bin))).intValue();
        Debug.println("code     : " + code);
        reason = getStringToken(bin);
        Debug.println("reason   : " + reason);
    }

    public String getReason()
    {
        return reason;
    }

    public int getCode()
    {
        return code;
    }

    private String protocol;
    private int code;
    private String reason;
}
