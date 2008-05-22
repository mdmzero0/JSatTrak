package javax.media;


// Referenced classes of package javax.media:
//            Duration, Time, Format, Buffer, 
//            TrackListener

public interface Track
    extends Duration
{

    public static final Time TIME_UNKNOWN = Time.TIME_UNKNOWN;
    public static final int FRAME_UNKNOWN = 0x7fffffff;

    public abstract Format getFormat();

    public abstract void setEnabled(boolean flag);

    public abstract boolean isEnabled();

    public abstract Time getStartTime();

    public abstract void readFrame(Buffer buffer);

    public abstract int mapTimeToFrame(Time time);

    public abstract Time mapFrameToTime(int i);

    public abstract void setTrackListener(TrackListener tracklistener);

}
