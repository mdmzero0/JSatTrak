// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RateRange.java

package javax.media.protocol;

import java.io.Serializable;

public class RateRange
    implements Serializable
{

    RateRange()
    {
    }

    public RateRange(RateRange r)
    {
        minimum = r.minimum;
        maximum = r.maximum;
        current = r.current;
        exact = r.exact;
    }

    public RateRange(float init, float min, float max, boolean isExact)
    {
        minimum = min;
        maximum = max;
        current = init;
        exact = isExact;
    }

    public float setCurrentRate(float rate)
    {
        current = rate;
        return current;
    }

    public float getCurrentRate()
    {
        return current;
    }

    public float getMinimumRate()
    {
        return minimum;
    }

    public float getMaximumRate()
    {
        return maximum;
    }

    public boolean inRange(float rate)
    {
        return minimum < rate && rate < maximum;
    }

    public boolean isExact()
    {
        return exact;
    }

    float minimum;
    float maximum;
    float current;
    boolean exact;
}
