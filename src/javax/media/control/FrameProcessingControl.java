package javax.media.control;

import javax.media.Control;

public interface FrameProcessingControl
    extends Control
{

    public abstract void setFramesBehind(float f);

    public abstract boolean setMinimalProcessing(boolean flag);

    public abstract int getFramesDropped();
}
