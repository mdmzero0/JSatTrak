package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Time, Controller

public class MediaTimeSetEvent extends ControllerEvent
{

    Time mediaTime;

    public MediaTimeSetEvent(Controller from, Time newMediaTime)
    {
        super(from);
        mediaTime = newMediaTime;
    }

    public Time getMediaTime()
    {
        return mediaTime;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",mediaTime=" + mediaTime + "]";
    }
}
