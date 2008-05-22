// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaTimeSetEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Time, Controller

public class MediaTimeSetEvent extends ControllerEvent
{

    public MediaTimeSetEvent(Controller from, Time newMediaTime)
    {
        super(from);
        mediaTime = newMediaTime;
    }

    public Time getMediaTime()
    {
        return mediaTime;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",mediaTime=" + mediaTime + "]";
    }

    Time mediaTime;
}
