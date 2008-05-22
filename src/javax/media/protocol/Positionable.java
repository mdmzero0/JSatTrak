package javax.media.protocol;

import javax.media.Time;

public interface Positionable
{

    public static final int RoundUp = 1;
    public static final int RoundDown = 2;
    public static final int RoundNearest = 3;

    public abstract Time setPosition(Time time, int i);

    public abstract boolean isRandomAccess();
}
