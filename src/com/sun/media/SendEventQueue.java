// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicController.java

package com.sun.media;

import com.sun.media.util.ThreadedEventQueue;
import javax.media.ControllerEvent;

// Referenced classes of package com.sun.media:
//            BasicController

class SendEventQueue extends ThreadedEventQueue
{

    public SendEventQueue(BasicController c)
    {
        controller = c;
    }

    public void processEvent(ControllerEvent evt)
    {
        controller.dispatchEvent(evt);
    }

    private BasicController controller;
}
