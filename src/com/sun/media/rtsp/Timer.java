// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Timer.java

package com.sun.media.rtsp;

import java.util.Vector;

// Referenced classes of package com.sun.media.rtsp:
//            TimerListener

public class Timer extends Thread
    implements Runnable
{

    public Timer(TimerListener listener, long duration)
    {
        listeners = new Vector();
        this.duration = duration / 0xf4240L;
        addListener(listener);
        stopped = false;
    }

    public void reset()
    {
    }

    public void stopTimer()
    {
        stopped = true;
        synchronized(this)
        {
            notify();
        }
    }

    public void run()
    {
        synchronized(this)
        {
            try
            {
                wait(duration);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        if(!stopped)
        {
            for(int i = 0; i < listeners.size(); i++)
            {
                TimerListener listener = (TimerListener)listeners.elementAt(i);
                listener.timerExpired();
            }

        }
    }

    public void addListener(TimerListener listener)
    {
        listeners.addElement(listener);
    }

    public void removeListener(TimerListener listener)
    {
        listeners.removeElement(listener);
    }

    private Vector listeners;
    private long duration;
    private boolean stopped;
}
