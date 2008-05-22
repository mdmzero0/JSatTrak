package javax.media;


// Referenced classes of package javax.media:
//            MediaEvent, Controller

public class ControllerEvent extends MediaEvent
{

    Controller eventSrc;

    public ControllerEvent(Controller from)
    {
        super(from);
        eventSrc = from;
    }

    public Controller getSourceController()
    {
        return eventSrc;
    }

    public Object getSource()
    {
        return eventSrc;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + eventSrc + "]";
    }
}
