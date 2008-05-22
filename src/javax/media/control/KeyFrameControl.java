package javax.media.control;

import javax.media.Control;

public interface KeyFrameControl
    extends Control
{

    public abstract int setKeyFrameInterval(int i);

    public abstract int getKeyFrameInterval();

    public abstract int getPreferredKeyFrameInterval();
}
