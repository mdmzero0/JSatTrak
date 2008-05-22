// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StopTimeChangeEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Time, Controller

public class StopTimeChangeEvent extends ControllerEvent
{

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

    Time stopTime;
}
