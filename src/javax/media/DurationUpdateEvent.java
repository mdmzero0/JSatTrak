// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DurationUpdateEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Time, Controller

public class DurationUpdateEvent extends ControllerEvent
{

    public DurationUpdateEvent(Controller from, Time newDuration)
    {
        super(from);
        duration = newDuration;
    }

    public Time getDuration()
    {
        return duration;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",duration=" + duration;
    }

    Time duration;
}
