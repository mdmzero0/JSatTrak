// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RtspControl.java

package javax.media.control;

import javax.media.Control;
import javax.media.rtp.RTPManager;

public interface RtspControl
    extends Control
{

    public abstract RTPManager[] getRTPManagers();
}
