package javax.media.protocol;

import java.io.IOException;

// Referenced classes of package javax.media.protocol:
//            SourceStream

public interface PullSourceStream
    extends SourceStream
{

    public abstract boolean willReadBlock();

    public abstract int read(byte abyte0[], int i, int j)
        throws IOException;
}
