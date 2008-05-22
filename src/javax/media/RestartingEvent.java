package javax.media;


// Referenced classes of package javax.media:
//            StopEvent, Controller, Time

public class RestartingEvent extends StopEvent
{

    public RestartingEvent(Controller from, int previous, int current, int target, Time mediaTime)
    {
        super(from, previous, current, target, mediaTime);
    }
}
