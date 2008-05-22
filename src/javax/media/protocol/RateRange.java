package javax.media.protocol;

import java.io.Serializable;

public class RateRange
    implements Serializable
{

    float minimum;
    float maximum;
    float current;
    boolean exact;

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
}
