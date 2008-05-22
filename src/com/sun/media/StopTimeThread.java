// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicController.java

package com.sun.media;


// Referenced classes of package com.sun.media:
//            TimedActionThread, BasicController

class StopTimeThread extends TimedActionThread
{

    public StopTimeThread(BasicController mc, long nanoseconds)
    {
        super(mc, nanoseconds);
        setName(getName() + ": StopTimeThread");
        super.wakeupTime = getTime() + nanoseconds;
    }

    protected long getTime()
    {
        return super.controller.getMediaNanoseconds();
    }

    protected void action()
    {
        super.controller.stopAtTime();
    }
}
