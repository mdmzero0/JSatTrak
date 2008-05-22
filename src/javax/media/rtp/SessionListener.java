// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SessionListener.java

package javax.media.rtp;

import java.util.EventListener;
import javax.media.rtp.event.SessionEvent;

public interface SessionListener
    extends EventListener
{

    public abstract void update(SessionEvent sessionevent);
}
