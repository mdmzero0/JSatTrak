// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RtspAppListener.java

package com.sun.media.rtsp;


public interface RtspAppListener
{

    public abstract void streamsReceivedEvent();

    public abstract void postStatusMessage(int i, String s);
}
