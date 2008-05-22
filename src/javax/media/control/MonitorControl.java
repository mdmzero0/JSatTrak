package javax.media.control;

import javax.media.Control;

public interface MonitorControl
    extends Control
{

    public abstract boolean setEnabled(boolean flag);

    public abstract float setPreviewFrameRate(float f);
}
