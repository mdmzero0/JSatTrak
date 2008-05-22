package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Controller

public class ControllerClosedEvent extends ControllerEvent
{

    protected String message;

    public ControllerClosedEvent(Controller from)
    {
        super(from);
        message = new String("");
    }

    public ControllerClosedEvent(Controller from, String why)
    {
        super(from);
        message = why;
    }

    public String getMessage()
    {
        return message;
    }
}
