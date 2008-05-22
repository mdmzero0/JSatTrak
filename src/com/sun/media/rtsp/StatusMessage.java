// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StatusMessage.java

package com.sun.media.rtsp;


public abstract class StatusMessage
{

    public StatusMessage()
    {
    }

    public static final int INVALID_ADDRESS = 1;
    public static final int SERVER_DOWN = 2;
    public static final int TIMEOUT = 3;
    public static final int NOT_FOUND = 4;
    public static final int PLAYING = 5;
    public static final int PAUSING = 6;
    public static final int END_REACHED = 7;
    public static final int READY = 8;
}
