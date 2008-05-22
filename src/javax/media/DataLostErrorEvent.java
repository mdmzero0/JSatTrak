package javax.media;


// Referenced classes of package javax.media:
//            ControllerClosedEvent, Controller

public class DataLostErrorEvent extends ControllerClosedEvent
{

    public DataLostErrorEvent(Controller from)
    {
        super(from);
    }

    public DataLostErrorEvent(Controller from, String why)
    {
        super(from, why);
    }
}
