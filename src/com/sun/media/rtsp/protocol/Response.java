// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Response.java

package com.sun.media.rtsp.protocol;

import com.sun.media.sdp.SdpParser;
import java.io.*;
import java.util.Vector;

// Referenced classes of package com.sun.media.rtsp.protocol:
//            Parser, StatusLine, Header

public class Response extends Parser
{

    public Response(ByteArrayInputStream bin)
    {
        String line = getLine(bin);
        statusLine = new StatusLine(line);
        headers = new Vector();
        line = getLine(bin);
        int contentLength = 0;
        while(line.length() > 0) 
            if(line.length() > 0)
            {
                Header header = new Header(line);
                if(header.type == 10)
                    contentLength = header.contentLength;
                headers.addElement(header);
                line = getLine(bin);
            }
        if(contentLength > 0)
        {
            byte data[] = new byte[bin.available()];
            try
            {
                bin.read(data);
                sdp = new SdpParser(data);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
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

    public StatusLine getStatusLine()
    {
        return statusLine;
    }

    public StatusLine statusLine;
    public Vector headers;
    public SdpParser sdp;
}
