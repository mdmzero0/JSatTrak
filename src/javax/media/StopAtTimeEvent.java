// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StopAtTimeEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            StopEvent, Controller, Time

public class StopAtTimeEvent extends StopEvent
{

    public StopAtTimeEvent(Controller from, int previous, int current, int target, Time mediaTime)
    {
        super(from, previous, current, target, mediaTime);
    }
}
