package javax.media;


// Referenced classes of package javax.media:
//            TransitionEvent, ControllerEvent, Time, Controller

public class StopEvent extends TransitionEvent
{

    private Time mediaTime;

    public StopEvent(Controller from, int previous, int current, int target, Time mediaTime)
    {
        super(from, previous, current, target);
        this.mediaTime = mediaTime;
    }

    public Time getMediaTime()
    {
        return mediaTime;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",previous=" + TransitionEvent.stateName(super.previousState) + ",current=" + TransitionEvent.stateName(super.currentState) + ",target=" + TransitionEvent.stateName(super.targetState) + ",mediaTime=" + mediaTime + "]";
    }
}
