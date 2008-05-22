// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MessageProcessor.java

package com.sun.media.rtsp;

import com.sun.media.Log;
import com.sun.media.rtsp.protocol.Message;

// Referenced classes of package com.sun.media.rtsp:
//            RtspManager

public class MessageProcessor
{

    public MessageProcessor(int connectionId, RtspManager rtspManager)
    {
        this.connectionId = connectionId;
        this.rtspManager = rtspManager;
        buffer = new byte[0];
    }

    public void processMessage(byte data[])
    {
        Log.comment("incoming msg:");
        Log.comment(new String(data));
        Message message = new Message(data);
        rtspManager.dataIndication(connectionId, message);
    }

    private int connectionId;
    private RtspManager rtspManager;
    private byte buffer[];
}
