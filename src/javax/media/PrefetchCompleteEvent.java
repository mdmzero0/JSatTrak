// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PrefetchCompleteEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            TransitionEvent, Controller

public class PrefetchCompleteEvent extends TransitionEvent
{

    public PrefetchCompleteEvent(Controller from, int previous, int current, int target)
    {
        super(from, previous, current, target);
    }
}
