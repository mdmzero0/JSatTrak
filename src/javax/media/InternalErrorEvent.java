// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   InternalErrorEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerErrorEvent, Controller

public class InternalErrorEvent extends ControllerErrorEvent
{

    public InternalErrorEvent(Controller from)
    {
        super(from);
    }

    public InternalErrorEvent(Controller from, String message)
    {
        super(from, message);
    }
}
