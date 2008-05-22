// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RateChangeEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, Controller

public class RateChangeEvent extends ControllerEvent
{

    public RateChangeEvent(Controller from, float newRate)
    {
        super(from);
        rate = newRate;
    }

    public float getRate()
    {
        return rate;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",rate=" + rate + "]";
    }

    float rate;
}
