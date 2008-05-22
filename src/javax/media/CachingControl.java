package javax.media;

import java.awt.Component;

// Referenced classes of package javax.media:
//            Control

public interface CachingControl
    extends Control
{

    public static final long LENGTH_UNKNOWN = 0x7fffffffffffffffL;

    public abstract boolean isDownloading();

    public abstract long getContentLength();

    public abstract long getContentProgress();

    public abstract Component getProgressBarComponent();

    public abstract Component getControlComponent();
}
