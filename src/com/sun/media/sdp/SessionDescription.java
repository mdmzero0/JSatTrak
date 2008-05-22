// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SessionDescription.java

package com.sun.media.sdp;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.Vector;

// Referenced classes of package com.sun.media.sdp:
//            Parser, TimeDescription, MediaAttribute

public class SessionDescription extends Parser
{

    public SessionDescription(ByteArrayInputStream bin)
    {
        connectionIncluded = false;
        version = getLine(bin);
        if(getToken(bin, "o=", true))
            origin = getLine(bin);
        if(getToken(bin, "s=", true))
            sessionName = getLine(bin);
        if(getToken(bin, "i=", false))
            sessionInfo = getLine(bin);
        if(getToken(bin, "u=", false))
            uri = getLine(bin);
        if(getToken(bin, "e=", false))
            email = getLine(bin);
        if(getToken(bin, "e=", false))
            email = getLine(bin);
        if(getToken(bin, "p=", false))
            phone = getLine(bin);
        if(getToken(bin, "c=", false))
        {
            connectionIncluded = true;
            connectionInfo = getLine(bin);
        }
        if(getToken(bin, "b=", false))
        {
            bandwidthInfo = getLine(bin);
            System.out.println("bandwidth info: " + bandwidthInfo);
        }
        timeDescriptions = new Vector();
        for(boolean found = getToken(bin, "t=", true); found; found = getToken(bin, "t=", false))
        {
            TimeDescription timeDescription = new TimeDescription(bin);
            timeDescriptions.addElement(timeDescription);
        }

        if(getToken(bin, "z=", false))
            timezoneAdjustment = getLine(bin);
        if(getToken(bin, "k=", false))
            encryptionKey = getLine(bin);
        sessionAttributes = new Vector();
        for(boolean found = getToken(bin, "a=", false); found; found = getToken(bin, "a=", false))
        {
            String sessionAttribute = getLine(bin);
            int index = sessionAttribute.indexOf(':');
            if(index > 0)
            {
                String name = sessionAttribute.substring(0, index);
                String value = sessionAttribute.substring(index + 1);
                MediaAttribute attribute = new MediaAttribute(name, value);
                sessionAttributes.addElement(attribute);
            }
        }

    }

    public MediaAttribute getSessionAttribute(String name)
    {
        MediaAttribute attribute = null;
        if(sessionAttributes != null)
        {
            for(int i = 0; i < sessionAttributes.size(); i++)
            {
                MediaAttribute entry = (MediaAttribute)sessionAttributes.elementAt(i);
                if(!entry.getName().equals(name))
                    continue;
                attribute = entry;
                break;
            }

        }
        return attribute;
    }

    public Vector timeDescriptions;
    public Vector sessionAttributes;
    public boolean connectionIncluded;
    public String version;
    public String origin;
    public String sessionName;
    public String sessionInfo;
    public String uri;
    public String email;
    public String phone;
    public String connectionInfo;
    public String bandwidthInfo;
    public String timezoneAdjustment;
    public String encryptionKey;
}
