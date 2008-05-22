package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Time, Controller

public class DurationUpdateEvent extends ControllerEvent
{

    Time duration;

    public DurationUpdateEvent(Controller from, Time newDuration)
    {
        super(from);
        duration = newDuration;
    }

    public Time getDuration()
    {
        return duration;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",duration=" + duration;
    }
}
