// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LoopThread.java

package com.sun.media.util;

import java.io.PrintStream;

// Referenced classes of package com.sun.media.util:
//            MediaThread

public abstract class LoopThread extends MediaThread
{

    public LoopThread()
    {
        paused = false;
        started = false;
        killed = false;
        waitingAtPaused = false;
        setName("Loop thread");
    }

    public synchronized void pause()
    {
        paused = true;
    }

    public synchronized void blockingPause()
    {
        if(waitingAtPaused || killed)
        {
            return;
        } else
        {
            paused = true;
            waitForCompleteStop();
            return;
        }
    }

    public boolean isPaused()
    {
        return paused;
    }

    public synchronized void waitForCompleteStop()
    {
        try
        {
            while(!killed && !waitingAtPaused && paused) 
                wait();
        }
        catch(InterruptedException e) { }
    }

    public synchronized void waitForCompleteStop(int millis)
    {
        try
        {
            if(!killed && !waitingAtPaused && paused)
                wait(millis);
        }
        catch(InterruptedException e) { }
    }

    public synchronized void start()
    {
        if(!started)
        {
            super.start();
            started = true;
        }
        paused = false;
        notifyAll();
    }

    public synchronized void kill()
    {
        killed = true;
        notifyAll();
    }

    public synchronized boolean waitHereIfPaused()
    {
        if(killed)
            return false;
        waitingAtPaused = true;
        if(paused)
            notifyAll();
        try
        {
            while(!killed && paused) 
                wait();
        }
        catch(InterruptedException e)
        {
            System.err.println("Timer: timeLoop() wait interrupted " + e);
        }
        waitingAtPaused = false;
        return !killed;
    }

    protected abstract boolean process();

    public void run()
    {
        while(waitHereIfPaused() && process()) ;
    }

    protected boolean paused;
    protected boolean started;
    protected boolean killed;
    private boolean waitingAtPaused;
}
