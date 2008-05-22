package javax.media;


// Referenced classes of package javax.media:
//            StopEvent, Controller, Time

public class EndOfMediaEvent extends StopEvent
{

    public EndOfMediaEvent(Controller from, int previous, int current, int target, Time mediaTime)
    {
        super(from, previous, current, target, mediaTime);
    }
}
