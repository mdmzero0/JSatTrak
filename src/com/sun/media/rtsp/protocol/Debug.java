// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Debug.java

package com.sun.media.rtsp.protocol;

import java.io.PrintStream;

abstract class Debug
{

    Debug()
    {
    }

    public static void println(Object object)
    {
        if(debug_enabled)
            System.out.println(object);
    }

    public static void dump(byte data[])
    {
        if(debug_enabled)
        {
            for(int i = 0; i < data.length; i++)
            {
                int value = data[i] & 0xff;
                System.out.println(i + ": " + Integer.toHexString(value));
            }

        }
    }

    static boolean debug_enabled = false;

}
