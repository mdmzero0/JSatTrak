// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicController.java

package com.sun.media;

import com.sun.media.util.MediaThread;
import java.io.PrintStream;
import java.util.Vector;

// Referenced classes of package com.sun.media:
//            BasicController

abstract class StateTransitionWorkThread extends MediaThread
{

    StateTransitionWorkThread()
    {
        eventQueue = new Vector();
        allEventsArrived = false;
        useControlPriority();
    }

    protected abstract boolean process();

    protected abstract void completed();

    protected abstract void failed();

    protected abstract void aborted();

    public void run()
    {
        controller.resetInterrupt();
        try
        {
            boolean success = process();
            if(controller.isInterrupted())
                aborted();
            else
            if(success)
                completed();
            else
                failed();
        }
        catch(OutOfMemoryError e)
        {
            System.err.println("Out of memory!");
        }
        controller.resetInterrupt();
    }

    BasicController controller;
    Vector eventQueue;
    boolean allEventsArrived;
}
