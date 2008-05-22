package javax.media;


// Referenced classes of package javax.media:
//            ControllerAdapter, ConfigureCompleteEvent, RealizeCompleteEvent, ControllerErrorEvent, 
//            DeallocateEvent, ControllerClosedEvent

class MCA extends ControllerAdapter
{

    boolean sync[];
    int state;

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
        {
            succeed();
        }
    }

    public void realizeComplete(RealizeCompleteEvent evt)
    {
        if(state == 300)
        {
            succeed();
        }
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
}
