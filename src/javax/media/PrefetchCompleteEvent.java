package javax.media;


// Referenced classes of package javax.media:
//            TransitionEvent, Controller

public class PrefetchCompleteEvent extends TransitionEvent
{

    public PrefetchCompleteEvent(Controller from, int previous, int current, int target)
    {
        super(from, previous, current, target);
    }
}
