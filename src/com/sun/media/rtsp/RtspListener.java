// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RtspListener.java

package com.sun.media.rtsp;

import com.sun.media.rtsp.protocol.Message;

public interface RtspListener
{

    public abstract void rtspMessageIndication(int i, Message message);

    public abstract void rtspConnectionTerminated(int i);
}
