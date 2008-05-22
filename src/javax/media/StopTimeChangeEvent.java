package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Time, Controller

public class StopTimeChangeEvent extends ControllerEvent
{

    Time stopTime;

    public StopTimeChangeEvent(Controller from, Time newStopTime)
    {
        super(from);
        stopTime = newStopTime;
    }

    public Time getStopTime()
    {
        return stopTime;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",stopTime=" + stopTime + "]";
    }
}
