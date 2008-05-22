// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MonitorAdapter.java

package com.sun.media.controls;

import com.sun.media.util.LoopThread;
import com.sun.media.util.MediaThread;

// Referenced classes of package com.sun.media.controls:
//            MonitorAdapter

class MonitorThread extends LoopThread
{

    public MonitorThread(MonitorAdapter ad)
    {
        setName(getName() + " : MonitorAdapter");
        useVideoPriority();
        this.ad = ad;
    }

    protected boolean process()
    {
        return ad.doProcess();
    }

    MonitorAdapter ad;
}
