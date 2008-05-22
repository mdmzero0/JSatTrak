package javax.media;


// Referenced classes of package javax.media:
//            ControllerErrorEvent, Controller

public class ConnectionErrorEvent extends ControllerErrorEvent
{

    public ConnectionErrorEvent(Controller from)
    {
        super(from);
    }

    public ConnectionErrorEvent(Controller from, String why)
    {
        super(from, why);
    }
}
