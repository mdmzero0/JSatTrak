package javax.media;


// Referenced classes of package javax.media:
//            Time

public interface Duration
{

    public static final Time DURATION_UNBOUNDED = new Time(0x7fffffffffffffffL);
    public static final Time DURATION_UNKNOWN = new Time(0x7ffffffffffffffeL);

    public abstract Time getDuration();

}
