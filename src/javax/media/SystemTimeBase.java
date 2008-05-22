package javax.media;


// Referenced classes of package javax.media:
//            Time, TimeBase

public final class SystemTimeBase
    implements TimeBase
{

    static long offset = System.currentTimeMillis() * 0xf4240L;

    public SystemTimeBase()
    {
    }

    public Time getTime()
    {
        return new Time(getNanoseconds());
    }

    public long getNanoseconds()
    {
        return System.currentTimeMillis() * 0xf4240L - offset;
    }

}
