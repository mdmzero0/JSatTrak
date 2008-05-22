// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicController.java

package com.sun.media;


// Referenced classes of package com.sun.media:
//            StateTransitionWorkThread, BasicController

class PrefetchWorkThread extends StateTransitionWorkThread
{

    public PrefetchWorkThread(BasicController mc)
    {
        super.controller = mc;
        setName(getName() + ": " + mc);
    }

    protected boolean process()
    {
        return super.controller.doPrefetch();
    }

    protected void completed()
    {
        super.controller.completePrefetch();
    }

    protected void aborted()
    {
        super.controller.abortPrefetch();
    }

    protected void failed()
    {
        super.controller.doFailedPrefetch();
    }
}
