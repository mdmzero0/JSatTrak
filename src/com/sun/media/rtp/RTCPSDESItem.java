// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPSDESItem.java

package com.sun.media.rtp;


public class RTCPSDESItem
{

    public RTCPSDESItem()
    {
    }

    public RTCPSDESItem(int type, String s)
    {
        this.type = type;
        data = new byte[s.length()];
        data = s.getBytes();
    }

    public String toString()
    {
        return "\t\t\t" + names[type - 1] + ": " + new String(data) + "\n";
    }

    public static String toString(RTCPSDESItem items[])
    {
        String s = "";
        for(int i = 0; i < items.length; i++)
            s = s + items[i];

        return s;
    }

    public int type;
    public byte data[];
    public static final int CNAME = 1;
    public static final int NAME = 2;
    public static final int EMAIL = 3;
    public static final int PHONE = 4;
    public static final int LOC = 5;
    public static final int TOOL = 6;
    public static final int NOTE = 7;
    public static final int PRIV = 8;
    public static final int HIGHEST = 8;
    public static final String names[] = {
        "CNAME", "NAME", "EMAIL", "PHONE", "LOC", "TOOL", "NOTE", "PRIV"
    };

}
