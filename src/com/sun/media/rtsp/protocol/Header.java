// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Header.java

package com.sun.media.rtsp.protocol;

import java.io.ByteArrayInputStream;

// Referenced classes of package com.sun.media.rtsp.protocol:
//            Parser, CSeqHeader, TransportHeader, SessionHeader, 
//            DurationHeader, RangeHeader, ContentBaseHeader, Debug

public class Header extends Parser
{

    public Header(String input)
    {
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes());
        String id = getToken(bin);
        String date;
        String rtpInfo;
        if(id.equalsIgnoreCase("CSeq:"))
        {
            type = 2;
            String number = getStringToken(bin).trim();
            parameter = new CSeqHeader(number);
        } else
        if(id.equalsIgnoreCase("Transport:"))
        {
            type = 1;
            String tx = getToken(bin);
            parameter = new TransportHeader(tx);
        } else
        if(id.equalsIgnoreCase("Session:"))
        {
            type = 3;
            String tx = getToken(bin);
            parameter = new SessionHeader(tx);
        } else
        if(id.equalsIgnoreCase("Duration:"))
        {
            type = 4;
            String tx = getToken(bin);
            Debug.println("Duration : " + tx);
            parameter = new DurationHeader(tx);
        } else
        if(id.equalsIgnoreCase("Range:"))
        {
            type = 5;
            String tx = getToken(bin);
            parameter = new RangeHeader(tx);
        } else
        if(id.equalsIgnoreCase("Date:"))
        {
            type = 6;
            date = getStringToken(bin);
        } else
        if(id.equalsIgnoreCase("Allow:"))
        {
            type = 6;
            String entries = getStringToken(bin);
        } else
        if(id.equalsIgnoreCase("Server:"))
        {
            type = 7;
            String server = getStringToken(bin);
        } else
        if(id.equalsIgnoreCase("Content-Type:"))
        {
            type = 8;
            String content_type = getStringToken(bin);
        } else
        if(id.equalsIgnoreCase("Content-Base:"))
        {
            type = 9;
            String content_base = getStringToken(bin);
            parameter = new ContentBaseHeader(content_base);
        } else
        if(id.equalsIgnoreCase("Content-Length:"))
        {
            type = 10;
            String content_length = getStringToken(bin);
            contentLength = (new Integer(content_length)).intValue();
        } else
        if(id.equalsIgnoreCase("Last-Modified:"))
            content_length = getStringToken(bin);
        else
        if(id.equalsIgnoreCase("RTP-Info:"))
            rtpInfo = getStringToken(bin);
        else
        if(id.length() > 0)
        {
            Debug.println("unknown id : <" + id + ">");
            String tmp = getStringToken(bin);
        }
    }

    public int type;
    public Object parameter;
    public int contentLength;
    public static final int TRANSPORT = 1;
    public static final int CSEQ = 2;
    public static final int SESSION = 3;
    public static final int DURATION = 4;
    public static final int RANGE = 5;
    public static final int DATE = 6;
    public static final int SERVER = 7;
    public static final int CONTENT_TYPE = 8;
    public static final int CONTENT_BASE = 9;
    public static final int CONTENT_LENGTH = 10;
}
