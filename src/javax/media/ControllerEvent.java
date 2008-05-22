// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ControllerEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            MediaEvent, Controller

public class ControllerEvent extends MediaEvent
{

    public ControllerEvent(Controller from)
    {
        super(from);
        eventSrc = from;
    }

    public Controller getSourceController()
    {
        return eventSrc;
    }

    public Object getSource()
    {
        return eventSrc;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + eventSrc + "]";
    }

    Controller eventSrc;
}
