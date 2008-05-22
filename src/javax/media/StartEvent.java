// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StartEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            TransitionEvent, ControllerEvent, Time, Controller

public class StartEvent extends TransitionEvent
{

    public StartEvent(Controller from, int previous, int current, int target, Time mediaTime, Time tbTime)
    {
        super(from, previous, current, target);
        this.mediaTime = mediaTime;
        timeBaseTime = tbTime;
    }

    public Time getMediaTime()
    {
        return mediaTime;
    }

    public Time getTimeBaseTime()
    {
        return timeBaseTime;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",previous=" + TransitionEvent.stateName(super.previousState) + ",current=" + TransitionEvent.stateName(super.currentState) + ",target=" + TransitionEvent.stateName(super.targetState) + ",mediaTime=" + mediaTime + ",timeBaseTime=" + timeBaseTime + "]";
    }

    private Time mediaTime;
    private Time timeBaseTime;
}
