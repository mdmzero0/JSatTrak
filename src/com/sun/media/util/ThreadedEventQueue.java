// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ThreadedEventQueue.java

package com.sun.media.util;

import java.io.PrintStream;
import java.util.Vector;
import javax.media.ControllerEvent;

// Referenced classes of package com.sun.media.util:
//            MediaThread

public abstract class ThreadedEventQueue extends MediaThread
{

    public ThreadedEventQueue()
    {
        eventQueue = new Vector();
        killed = false;
        useControlPriority();
    }

    protected abstract void processEvent(ControllerEvent controllerevent);

    protected boolean dispatchEvents()
    {
        ControllerEvent evt = null;
        synchronized(this)
        {
            try
            {
                for(; !killed && eventQueue.size() == 0; wait());
            }
            catch(InterruptedException e)
            {
                System.err.println("MediaNode event thread " + e);
                boolean flag = true;
                return flag;
            }
            if(eventQueue.size() > 0)
            {
                evt = (ControllerEvent)eventQueue.firstElement();
                eventQueue.removeElementAt(0);
            }
        }
        if(evt != null)
            processEvent(evt);
        return !killed || eventQueue.size() != 0;
    }

    public synchronized void postEvent(ControllerEvent evt)
    {
        eventQueue.addElement(evt);
        notifyAll();
    }

    public synchronized void kill()
    {
        killed = true;
        notifyAll();
    }

    public void run()
    {
        while(dispatchEvents()) ;
    }

    private Vector eventQueue;
    private boolean killed;
}
