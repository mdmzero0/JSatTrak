// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ConfigureCompleteEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            TransitionEvent, Controller

public class ConfigureCompleteEvent extends TransitionEvent
{

    public ConfigureCompleteEvent(Controller processor, int previous, int current, int target)
    {
        super(processor, previous, current, target);
    }
}
