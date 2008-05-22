package javax.media;


// Referenced classes of package javax.media:
//            Time, IncompatibleTimeBaseException, ClockStoppedException, TimeBase

public interface Clock
{

    public static final Time RESET = new Time(0x7fffffffffffffffL);

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

}
