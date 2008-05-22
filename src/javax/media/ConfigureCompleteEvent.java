package javax.media;


// Referenced classes of package javax.media:
//            TransitionEvent, Controller

public class ConfigureCompleteEvent extends TransitionEvent
{

    public ConfigureCompleteEvent(Controller processor, int previous, int current, int target)
    {
        super(processor, previous, current, target);
    }
}
