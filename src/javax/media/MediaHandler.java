package javax.media;

import java.io.IOException;
import javax.media.protocol.DataSource;

// Referenced classes of package javax.media:
//            IncompatibleSourceException

public interface MediaHandler
{

    public abstract void setSource(DataSource datasource)
        throws IOException, IncompatibleSourceException;
}
