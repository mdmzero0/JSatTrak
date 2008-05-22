// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicController.java

package com.sun.media;

import com.sun.media.util.MediaThread;

// Referenced classes of package com.sun.media:
//            BasicController

abstract class TimedActionThread extends MediaThread
{

    TimedActionThread(BasicController mc, long nanoseconds)
    {
        aborted = false;
        controller = mc;
        useControlPriority();
        wakeupTime = nanoseconds;
    }

    protected abstract long getTime();

    protected abstract void action();

    public synchronized void abort()
    {
        aborted = true;
        notify();
    }

    public void run()
    {
        do
        {
            long now = getTime();
            if(now >= wakeupTime || aborted)
                break;
            long sleepTime = wakeupTime - now;
            if(sleepTime > 0x3b9aca00L)
                sleepTime = 0x3b9aca00L;
            synchronized(this)
            {
                try
                {
                    wait(sleepTime / 0xf4240L);
                }
                catch(InterruptedException e)
                {
                    break;
                }
            }
        } while(true);
        if(!aborted)
            action();
    }

    protected BasicController controller;
    protected long wakeupTime;
    protected boolean aborted;
}
