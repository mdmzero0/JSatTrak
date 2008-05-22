// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicController.java

package com.sun.media;

import javax.media.TimeBase;

// Referenced classes of package com.sun.media:
//            TimedActionThread, BasicController

class TimedStartThread extends TimedActionThread
{

    public TimedStartThread(BasicController mc, long tbt)
    {
        super(mc, tbt);
        setName(getName() + ": TimedStartThread");
    }

    protected long getTime()
    {
        return super.controller.getTimeBase().getNanoseconds();
    }

    protected void action()
    {
        super.controller.doStart();
    }
}
