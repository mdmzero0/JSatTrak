// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Manager.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerAdapter, ConfigureCompleteEvent, RealizeCompleteEvent, ControllerErrorEvent, 
//            DeallocateEvent, ControllerClosedEvent

class MCA extends ControllerAdapter
{

    public MCA(boolean sync[], int state)
    {
        this.sync = sync;
        this.state = state;
    }

    private void succeed()
    {
        synchronized(sync)
        {
            sync[0] = true;
            sync[1] = true;
            sync.notify();
        }
    }

    private void fail()
    {
        synchronized(sync)
        {
            sync[0] = true;
            sync[1] = false;
            sync.notify();
        }
    }

    public void configureComplete(ConfigureCompleteEvent evt)
    {
        if(state == 180)
            succeed();
    }

    public void realizeComplete(RealizeCompleteEvent evt)
    {
        if(state == 300)
            succeed();
    }

    public void controllerError(ControllerErrorEvent evt)
    {
        fail();
    }

    public void deallocate(DeallocateEvent evt)
    {
        fail();
    }

    public void controllerClosed(ControllerClosedEvent evt)
    {
        fail();
    }

    boolean sync[];
    int state;
}
