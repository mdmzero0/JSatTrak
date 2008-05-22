// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Clock.java

package javax.media;


// Referenced classes of package javax.media:
//            Time, IncompatibleTimeBaseException, ClockStoppedException, TimeBase

public interface Clock
{

    public abstract void setTimeBase(TimeBase timebase)
        throws IncompatibleTimeBaseException;

    public abstract void syncStart(Time time);

    public abstract void stop();

    public abstract void setStopTime(Time time);

    public abstract Time getStopTime();

    public abstract void setMediaTime(Time time);

    public abstract Time getMediaTime();

    public abstract long getMediaNanoseconds();

    public abstract Time getSyncTime();

    public abstract TimeBase getTimeBase();

    public abstract Time mapToTimeBase(Time time)
        throws ClockStoppedException;

    public abstract float getRate();

    public abstract float setRate(float f);

    public static final Time RESET = new Time(0x7fffffffffffffffL);

}
