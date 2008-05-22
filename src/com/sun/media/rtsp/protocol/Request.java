// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Request.java

package com.sun.media.rtsp.protocol;

import java.io.ByteArrayInputStream;
import java.util.Vector;

// Referenced classes of package com.sun.media.rtsp.protocol:
//            Parser, RequestLine, Header

public class Request extends Parser
{

    public Request(ByteArrayInputStream bin)
    {
        String line = getLine(bin);
        requestLine = new RequestLine(line);
        headers = new Vector();
        for(line = getLine(bin); line.length() > 0;)
            if(line.length() > 0)
            {
                Header header = new Header(line);
                headers.addElement(header);
                line = getLine(bin);
            }

    }

    public RequestLine getRequestLine()
    {
        return requestLine;
    }

    public Header getHeader(int type)
    {
        Header header = null;
        for(int i = 0; i < headers.size(); i++)
        {
            Header tmpHeader = (Header)headers.elementAt(i);
            if(tmpHeader.type != type)
                continue;
            header = tmpHeader;
            break;
        }

        return header;
    }

    public RequestLine requestLine;
    public Vector headers;
}
