package javax.media.control;

import javax.media.*;

public interface FramePositioningControl
    extends Control
{

    public static final Time TIME_UNKNOWN = Track.TIME_UNKNOWN;
    public static final int FRAME_UNKNOWN = 0x7fffffff;

    public abstract int seek(int i);

    public abstract int skip(int i);

    public abstract Time mapFrameToTime(int i);

    public abstract int mapTimeToFrame(Time time);

}
