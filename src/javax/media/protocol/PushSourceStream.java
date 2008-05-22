package javax.media.protocol;

import java.io.IOException;

// Referenced classes of package javax.media.protocol:
//            SourceStream, SourceTransferHandler

public interface PushSourceStream
    extends SourceStream
{

    public abstract int read(byte abyte0[], int i, int j)
        throws IOException;

    public abstract int getMinimumTransferSize();

    public abstract void setTransferHandler(SourceTransferHandler sourcetransferhandler);
}
