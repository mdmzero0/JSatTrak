package javax.media.control;

import javax.media.Control;

public interface BitRateControl
    extends Control
{

    public abstract int getBitRate();

    public abstract int setBitRate(int i);

    public abstract int getMinSupportedBitRate();

    public abstract int getMaxSupportedBitRate();
}
