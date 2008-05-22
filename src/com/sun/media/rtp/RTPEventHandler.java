// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPEventHandler.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.RTPMediaThread;
import com.sun.media.util.MediaThread;
import java.util.Vector;
import javax.media.rtp.*;
import javax.media.rtp.event.*;

// Referenced classes of package com.sun.media.rtp:
//            RTPSessionMgr

public class RTPEventHandler extends RTPMediaThread
{

    public RTPEventHandler(RTPSessionMgr sm)
    {
        super("RTPEventHandler");
        eventQueue = new Vector();
        killed = false;
        this.sm = sm;
        useControlPriority();
        setDaemon(true);
        start();
    }

    protected void processEvent(RTPEvent evt)
    {
        if(evt instanceof SessionEvent)
        {
            for(int i = 0; i < sm.sessionlistener.size(); i++)
            {
                SessionListener sl = (SessionListener)sm.sessionlistener.elementAt(i);
                if(sl != null)
                    sl.update((SessionEvent)evt);
            }

            return;
        }
        if(evt instanceof RemoteEvent)
        {
            for(int i = 0; i < sm.remotelistener.size(); i++)
            {
                RemoteListener sl = (RemoteListener)sm.remotelistener.elementAt(i);
                if(sl != null)
                    sl.update((RemoteEvent)evt);
            }

            return;
        }
        if(evt instanceof ReceiveStreamEvent)
        {
            for(int i = 0; i < sm.streamlistener.size(); i++)
            {
                ReceiveStreamListener sl = (ReceiveStreamListener)sm.streamlistener.elementAt(i);
                if(sl != null)
                    sl.update((ReceiveStreamEvent)evt);
            }

            return;
        }
        if(evt instanceof SendStreamEvent)
        {
            for(int i = 0; i < sm.sendstreamlistener.size(); i++)
            {
                SendStreamListener sl = (SendStreamListener)sm.sendstreamlistener.elementAt(i);
                if(sl != null)
                    sl.update((SendStreamEvent)evt);
            }

        }
    }

    protected void dispatchEvents()
    {
        RTPEvent evt;
        synchronized(this)
        {
            try
            {
                for(; eventQueue.size() == 0 && !killed; wait());
            }
            catch(InterruptedException e) { }
            if(killed)
                return;
            evt = (RTPEvent)eventQueue.elementAt(0);
            eventQueue.removeElementAt(0);
        }
        processEvent(evt);
    }

    public synchronized void postEvent(RTPEvent evt)
    {
        eventQueue.addElement(evt);
        notifyAll();
    }

    public synchronized void close()
    {
        killed = true;
        notifyAll();
    }

    public void run()
    {
        while(!killed) 
            dispatchEvents();
    }

    private RTPSessionMgr sm;
    private Vector eventQueue;
    private boolean killed;
}
