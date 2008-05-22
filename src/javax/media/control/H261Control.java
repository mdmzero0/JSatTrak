package javax.media.control;

import javax.media.Control;

public interface H261Control
    extends Control
{

    public abstract boolean isStillImageTransmissionSupported();

    public abstract boolean setStillImageTransmission(boolean flag);

    public abstract boolean getStillImageTransmission();
}
