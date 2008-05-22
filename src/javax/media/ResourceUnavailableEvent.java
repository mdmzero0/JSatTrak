// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResourceUnavailableEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerErrorEvent, Controller

public class ResourceUnavailableEvent extends ControllerErrorEvent
{

    public ResourceUnavailableEvent(Controller from)
    {
        super(from);
    }

    public ResourceUnavailableEvent(Controller from, String message)
    {
        super(from, message);
    }
}
