package javax.media.control;

import javax.media.Control;

public interface FrameRateControl
    extends Control
{

    public abstract float getFrameRate();

    public abstract float setFrameRate(float f);

    public abstract float getMaxSupportedFrameRate();

    public abstract float getPreferredFrameRate();
}
