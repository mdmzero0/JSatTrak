package javax.media.protocol;


// Referenced classes of package javax.media.protocol:
//            Controls, ContentDescriptor

public interface SourceStream
    extends Controls
{

    public static final long LENGTH_UNKNOWN = -1L;

    public abstract ContentDescriptor getContentDescriptor();

    public abstract long getContentLength();

    public abstract boolean endOfStream();
}
