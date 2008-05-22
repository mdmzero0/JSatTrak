// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPSDES.java

package com.sun.media.rtp;


// Referenced classes of package com.sun.media.rtp:
//            RTCPSDESItem

public class RTCPSDES
{

    public RTCPSDES()
    {
    }

    public String toString()
    {
        return "\t\tSource Description for sync source " + ssrc + ":\n" + RTCPSDESItem.toString(items);
    }

    public static String toString(RTCPSDES chunks[])
    {
        String s = "";
        for(int i = 0; i < chunks.length; i++)
            s = s + chunks[i];

        return s;
    }

    public int ssrc;
    public RTCPSDESItem items[];
}
