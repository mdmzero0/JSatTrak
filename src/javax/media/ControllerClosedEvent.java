// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ControllerClosedEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Controller

public class ControllerClosedEvent extends ControllerEvent
{

    public ControllerClosedEvent(Controller from)
    {
        super(from);
        message = new String("");
    }

    public ControllerClosedEvent(Controller from, String why)
    {
        super(from);
        message = why;
    }

    public String getMessage()
    {
        return message;
    }

    protected String message;
}
