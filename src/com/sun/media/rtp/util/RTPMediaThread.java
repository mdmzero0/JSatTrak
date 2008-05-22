// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPMediaThread.java

package com.sun.media.rtp.util;

import com.sun.media.util.MediaThread;

public class RTPMediaThread extends MediaThread
{

    public RTPMediaThread()
    {
        this("RTP thread");
    }

    public RTPMediaThread(String name)
    {
        super(name);
    }

    public RTPMediaThread(Runnable r)
    {
        this(r, "RTP thread");
    }

    public RTPMediaThread(Runnable r, String name)
    {
        super(r, name);
    }
}
