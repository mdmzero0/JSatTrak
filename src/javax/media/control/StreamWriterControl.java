package javax.media.control;

import javax.media.Control;

public interface StreamWriterControl
    extends Control
{

    public abstract boolean setStreamSizeLimit(long l);

    public abstract long getStreamSize();
}
