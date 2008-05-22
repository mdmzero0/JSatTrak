package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Controller

public class TransitionEvent extends ControllerEvent
{

    int previousState;
    int currentState;
    int targetState;

    public TransitionEvent(Controller from, int previous, int current, int target)
    {
        super(from);
        previousState = previous;
        currentState = current;
        targetState = target;
    }

    public int getPreviousState()
    {
        return previousState;
    }

    public int getCurrentState()
    {
        return currentState;
    }

    public int getTargetState()
    {
        return targetState;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",previous=" + stateName(previousState) + ",current=" + stateName(currentState) + ",target=" + stateName(targetState) + "]";
    }

    static String stateName(int state)
    {
        switch(state)
        {
        case 100: // 'd'
            return "Unrealized";

        case 200: 
            return "Realizing";

        case 300: 
            return "Realized";

        case 400: 
            return "Prefetching";

        case 500: 
            return "Prefetched";

        case 600: 
            return "Started";

        case 140: 
            return "Configuring";

        case 180: 
            return "Configured";
        }
        return "<Unknown>";
    }
}
