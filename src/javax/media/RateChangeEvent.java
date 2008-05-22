package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Controller

public class RateChangeEvent extends ControllerEvent
{

    float rate;

    public RateChangeEvent(Controller from, float newRate)
    {
        super(from);
        rate = newRate;
    }

    public float getRate()
    {
        return rate;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",rate=" + rate + "]";
    }
}
