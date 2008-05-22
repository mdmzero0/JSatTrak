// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ConnectionErrorEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerErrorEvent, Controller

public class ConnectionErrorEvent extends ControllerErrorEvent
{

    public ConnectionErrorEvent(Controller from)
    {
        super(from);
    }

    public ConnectionErrorEvent(Controller from, String why)
    {
        super(from, why);
    }
}
