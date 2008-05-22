package javax.media;


// Referenced classes of package javax.media:
//            TransitionEvent, Controller

public class RealizeCompleteEvent extends TransitionEvent
{

    public RealizeCompleteEvent(Controller from, int previous, int current, int target)
    {
        super(from, previous, current, target);
    }
}
