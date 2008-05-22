// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MessageType.java

package com.sun.media.rtsp.protocol;


// Referenced classes of package com.sun.media.rtsp.protocol:
//            Debug

public class MessageType
{

    public MessageType(String msg)
    {
        Debug.println(msg);
        type = 0;
        for(int i = 0; i < messages.length; i++)
        {
            if(!msg.equals(messages[i]))
                continue;
            type = i + 1;
            break;
        }

    }

    public int getType()
    {
        return type;
    }

    public static final int UNKNOWN = 0;
    public static final int DESCRIBE = 1;
    public static final int ANNOUNCE = 2;
    public static final int GET_PARAMETER = 3;
    public static final int OPTIONS = 4;
    public static final int PAUSE = 5;
    public static final int PLAY = 6;
    public static final int RECORD = 7;
    public static final int REDIRECT = 8;
    public static final int SETUP = 9;
    public static final int SET_PARAMETER = 10;
    public static final int TEARDOWN = 11;
    public static final int RESPONSE = 12;
    private int type;
    public String messages[] = {
        "DESCRIBE", "ANNOUNCE", "GET_PARAMETER", "OPTIONS", "PAUSE", "PLAY", "RECORD", "REDIRECT", "SETUP", "SET_PARAMETER", 
        "TEARDOWN", "RTSP/1.0"
    };
}
