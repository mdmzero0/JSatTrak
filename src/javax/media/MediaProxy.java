package javax.media;

import java.io.IOException;
import javax.media.protocol.DataSource;

// Referenced classes of package javax.media:
//            MediaHandler, NoDataSourceException

public interface MediaProxy
    extends MediaHandler
{

    public abstract DataSource getDataSource()
        throws IOException, NoDataSourceException;
}
