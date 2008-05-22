// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SdpParser.java

package com.sun.media.sdp;

import java.io.ByteArrayInputStream;
import java.util.Vector;

// Referenced classes of package com.sun.media.sdp:
//            Parser, SessionDescription, MediaDescription, MediaAttribute

public class SdpParser extends Parser
{

    public SdpParser(byte data[])
    {
        init();
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        parseData(bin);
    }

    public void parseData(ByteArrayInputStream bin)
    {
        if(getToken(bin, "v=", true))
        {
            sessionDescription = new SessionDescription(bin);
            mediaDescriptions = new Vector();
            for(boolean found = getToken(bin, "m=", false); found; found = getToken(bin, "m=", false))
            {
                MediaDescription mediaDescription = new MediaDescription(bin, sessionDescription.connectionIncluded);
                mediaDescriptions.addElement(mediaDescription);
            }

        }
    }

    public MediaAttribute getSessionAttribute(String name)
    {
        MediaAttribute attribute = null;
        if(sessionDescription != null)
            attribute = sessionDescription.getSessionAttribute(name);
        return attribute;
    }

    public MediaDescription getMediaDescription(String name)
    {
        MediaDescription description = null;
        if(mediaDescriptions != null)
        {
            for(int i = 0; i < mediaDescriptions.size(); i++)
            {
                MediaDescription entry = (MediaDescription)mediaDescriptions.elementAt(i);
                if(!entry.name.equals(name))
                    continue;
                description = entry;
                break;
            }

        }
        return description;
    }

    public Vector getMediaDescriptions()
    {
        return mediaDescriptions;
    }

    public static void main(String args[])
    {
        new SdpParser(input.getBytes());
    }

    public SessionDescription sessionDescription;
    public Vector mediaDescriptions;
    static String input = "v=0\r\no=mhandley 2890844526 2890842807 IN IP4 126.16.64.4\r\ns=SDP Seminar\r\ni=A Seminar on the session description protocol\r\nu=http://www.cs.ucl.ac.uk/staff/M.Handley/sdp.03.ps\r\ne=mjb@isi.edu (Mark Handley)\r\nc=IN IP4 224.2.17.12/127\r\nt=2873397496 2873404696\r\na=recvonly\r\nm=audio 49170 RTP/AVP 0\r\nm=video 51372 RTP/AVP 31\r\nm=application 32416 udp wbr\na=orient:portrait\r\n";

}
