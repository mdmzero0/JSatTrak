// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RtspAdapter.java

package com.sun.media.controls;

import java.awt.Component;
import javax.media.control.RtspControl;
import javax.media.rtp.RTPManager;

public class RtspAdapter
    implements RtspControl
{

    public RtspAdapter()
    {
    }

    public void setRTPManagers(RTPManager managers[])
    {
        this.managers = managers;
    }

    public RTPManager[] getRTPManagers()
    {
        return managers;
    }

    public void setMediaTypes(String mediaTypes[])
    {
        this.mediaTypes = mediaTypes;
    }

    public String[] getMediaTypes()
    {
        return mediaTypes;
    }

    public Component getControlComponent()
    {
        return null;
    }

    private RTPManager managers[];
    private String mediaTypes[];
}
