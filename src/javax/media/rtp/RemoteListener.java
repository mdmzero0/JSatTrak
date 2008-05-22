// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RemoteListener.java

package javax.media.rtp;

import java.util.EventListener;
import javax.media.rtp.event.RemoteEvent;

public interface RemoteListener
    extends EventListener
{

    public abstract void update(RemoteEvent remoteevent);
}
