// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TimeDescription.java

package com.sun.media.sdp;

import java.io.ByteArrayInputStream;
import java.util.Vector;

// Referenced classes of package com.sun.media.sdp:
//            Parser

public class TimeDescription extends Parser
{

    public TimeDescription(ByteArrayInputStream bin)
    {
        timeActive = getLine(bin);
        repeatTimes = new Vector();
        for(boolean found = getToken(bin, "r=", false); found; found = getToken(bin, "r=", false))
        {
            String repeatTime = getLine(bin);
            repeatTimes.addElement(repeatTime);
        }

    }

    public String timeActive;
    public Vector repeatTimes;
}
