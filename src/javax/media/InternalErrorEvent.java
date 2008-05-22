package javax.media;


// Referenced classes of package javax.media:
//            ControllerErrorEvent, Controller

public class InternalErrorEvent extends ControllerErrorEvent
{

    public InternalErrorEvent(Controller from)
    {
        super(from);
    }

    public InternalErrorEvent(Controller from, String message)
    {
        super(from, message);
    }
}
