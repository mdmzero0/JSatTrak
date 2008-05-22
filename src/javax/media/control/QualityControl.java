package javax.media.control;

import javax.media.Control;

public interface QualityControl
    extends Control
{

    public abstract float getQuality();

    public abstract float setQuality(float f);

    public abstract float getPreferredQuality();

    public abstract boolean isTemporalSpatialTradeoffSupported();
}
