package javax.media;

import java.io.IOException;
import javax.media.protocol.ContentDescriptor;

// Referenced classes of package javax.media:
//            PlugIn, MediaHandler, Duration, BadHeaderException, 
//            Track, Time

public interface Demultiplexer
    extends PlugIn, MediaHandler, Duration
{

    public abstract ContentDescriptor[] getSupportedInputContentDescriptors();

    public abstract void start()
        throws IOException;

    public abstract void stop();

    public abstract Track[] getTracks()
        throws IOException, BadHeaderException;

    public abstract boolean isPositionable();

    public abstract boolean isRandomAccess();

    public abstract Time setPosition(Time time, int i);

    public abstract Time getMediaTime();

    public abstract Time getDuration();
}
