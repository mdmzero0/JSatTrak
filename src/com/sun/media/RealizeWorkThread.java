// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicController.java

package com.sun.media;


// Referenced classes of package com.sun.media:
//            StateTransitionWorkThread, BasicController

class RealizeWorkThread extends StateTransitionWorkThread
{

    public RealizeWorkThread(BasicController mc)
    {
        super.controller = mc;
        setName(getName() + ": " + mc);
    }

    protected boolean process()
    {
        return super.controller.doRealize();
    }

    protected void completed()
    {
        super.controller.completeRealize();
    }

    protected void aborted()
    {
        super.controller.abortRealize();
    }

    protected void failed()
    {
        super.controller.doFailedRealize();
    }
}
