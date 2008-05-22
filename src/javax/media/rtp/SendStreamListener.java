// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SendStreamListener.java

package javax.media.rtp;

import java.util.EventListener;
import javax.media.rtp.event.SendStreamEvent;

public interface SendStreamListener
    extends EventListener
{

    public abstract void update(SendStreamEvent sendstreamevent);
}
