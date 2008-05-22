package javax.media.protocol;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Format;

// Referenced classes of package javax.media.protocol:
//            SourceStream

public interface PullBufferStream
    extends SourceStream
{

    public abstract boolean willReadBlock();

    public abstract void read(Buffer buffer)
        throws IOException;

    public abstract Format getFormat();
}
