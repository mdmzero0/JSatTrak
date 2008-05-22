// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Message.java

package com.sun.media.rtsp.protocol;

import java.util.StringTokenizer;

// Referenced classes of package com.sun.media.rtsp.protocol:
//            MessageType, DescribeMessage, SetupMessage, PlayMessage, 
//            PauseMessage, TeardownMessage, OptionsMessage, ResponseMessage, 
//            SetParameterMessage, Debug

public class Message
{

    public Message(int type, Object parameter)
    {
    }

    public Message(byte data[])
    {
        this.data = data;
        parseData();
    }

    private void parseData()
    {
        StringTokenizer st = new StringTokenizer(new String(data));
        type = (new MessageType(st.nextToken())).getType();
        switch(type)
        {
        case 1: // '\001'
            parameter = new DescribeMessage(data);
            break;

        case 9: // '\t'
            parameter = new SetupMessage(data);
            break;

        case 6: // '\006'
            parameter = new PlayMessage(data);
            break;

        case 5: // '\005'
            parameter = new PauseMessage(data);
            break;

        case 11: // '\013'
            parameter = new TeardownMessage(data);
            break;

        case 4: // '\004'
            parameter = new OptionsMessage(data);
            break;

        case 12: // '\f'
            parameter = new ResponseMessage(data);
            break;

        case 10: // '\n'
            parameter = new SetParameterMessage(data);
            break;

        case 2: // '\002'
        case 3: // '\003'
        case 7: // '\007'
        case 8: // '\b'
        default:
            Debug.println("Unknown msg type: " + type);
            Debug.println("Unknown msg type: " + new String(data));
            break;
        }
    }

    public int getType()
    {
        return type;
    }

    public Object getParameter()
    {
        return parameter;
    }

    private byte data[];
    private int type;
    private Object parameter;
}
