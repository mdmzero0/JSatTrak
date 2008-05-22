package javax.media;

import java.io.IOException;
import javax.media.datasink.DataSinkListener;

// Referenced classes of package javax.media:
//            MediaHandler, Controls, MediaLocator

public interface DataSink
    extends MediaHandler, Controls
{

    public abstract void setOutputLocator(MediaLocator medialocator);

    public abstract MediaLocator getOutputLocator();

    public abstract void start()
        throws IOException;

    public abstract void stop()
        throws IOException;

    public abstract void open()
        throws IOException, SecurityException;

    public abstract void close();

    public abstract String getContentType();

    public abstract void addDataSinkListener(DataSinkListener datasinklistener);

    public abstract void removeDataSinkListener(DataSinkListener datasinklistener);
}
