package javax.media.control;

import javax.media.Control;

public interface BufferControl
    extends Control
{

    public static final long DEFAULT_VALUE = -1L;
    public static final long MAX_VALUE = -2L;

    public abstract long getBufferLength();

    public abstract long setBufferLength(long l);

    public abstract long getMinimumThreshold();

    public abstract long setMinimumThreshold(long l);

    public abstract void setEnabledThreshold(boolean flag);

    public abstract boolean getEnabledThreshold();
}
