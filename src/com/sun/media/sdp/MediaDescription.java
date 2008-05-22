// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaDescription.java

package com.sun.media.sdp;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.Vector;

// Referenced classes of package com.sun.media.sdp:
//            Parser, MediaAttribute

public class MediaDescription extends Parser
{

    public MediaDescription(ByteArrayInputStream bin, boolean connectionIncluded)
    {
        String line = getLine(bin);
        int end = line.indexOf(' ');
        this.name = line.substring(0, end);
        int start = end + 1;
        end = line.indexOf(' ', start);
        port = line.substring(start, end);
        start = end + 1;
        end = line.indexOf(' ', start);
        protocol = line.substring(start, end);
        start = end + 1;
        payload = line.substring(start);
        try
        {
            payload_type = (new Integer(payload)).intValue();
        }
        catch(Exception e)
        {
            payload_type = -1;
        }
        if(getToken(bin, "i=", false))
        {
            mediaTitle = getLine(bin);
            System.out.println("media title: " + mediaTitle);
        }
        boolean mandatory = true;
        if(connectionIncluded)
            mandatory = false;
        if(getToken(bin, "c=", mandatory))
        {
            connectionInfo = getLine(bin);
            System.out.println("connection info: " + connectionInfo);
        }
        if(getToken(bin, "b=", false))
        {
            bandwidthInfo = getLine(bin);
            System.out.println("bandwidth info: " + bandwidthInfo);
        }
        if(getToken(bin, "k=", false))
        {
            encryptionKey = getLine(bin);
            System.out.println("encryption key: " + encryptionKey);
        }
        mediaAttributes = new Vector();
        for(boolean found = getToken(bin, "a=", false); found; found = getToken(bin, "a=", false))
        {
            String mediaAttribute = getLine(bin);
            int index = mediaAttribute.indexOf(':');
            if(index > 0)
            {
                String name = mediaAttribute.substring(0, index);
                String value = mediaAttribute.substring(index + 1);
                MediaAttribute attribute = new MediaAttribute(name, value);
                mediaAttributes.addElement(attribute);
            }
        }

    }

    public MediaAttribute getMediaAttribute(String name)
    {
        MediaAttribute attribute = null;
        if(mediaAttributes != null)
        {
            for(int i = 0; i < mediaAttributes.size(); i++)
            {
                MediaAttribute entry = (MediaAttribute)mediaAttributes.elementAt(i);
                if(!entry.getName().equals(name))
                    continue;
                attribute = entry;
                break;
            }

        }
        return attribute;
    }

    public String name;
    public String port;
    public String protocol;
    public int payload_type;
    public String payload;
    public String mediaTitle;
    public String connectionInfo;
    public String bandwidthInfo;
    public String encryptionKey;
    public Vector mediaAttributes;
}
