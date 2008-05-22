// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ControllerErrorEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerClosedEvent, ControllerEvent, Controller

public class ControllerErrorEvent extends ControllerClosedEvent
{

    public ControllerErrorEvent(Controller from)
    {
        super(from);
    }

    public ControllerErrorEvent(Controller from, String why)
    {
        super(from, why);
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",message=" + super.message + "]";
    }
}
