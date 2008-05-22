package javax.media.control;

import javax.media.Control;

public interface SilenceSuppressionControl
    extends Control
{

    public abstract boolean getSilenceSuppression();

    public abstract boolean setSilenceSuppression(boolean flag);

    public abstract boolean isSilenceSuppressionSupported();
}
