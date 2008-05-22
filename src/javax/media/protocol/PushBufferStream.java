package javax.media.protocol;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Format;

// Referenced classes of package javax.media.protocol:
//            SourceStream, BufferTransferHandler

public interface PushBufferStream
    extends SourceStream
{

    public abstract Format getFormat();

    public abstract void read(Buffer buffer)
        throws IOException;

    public abstract void setTransferHandler(BufferTransferHandler buffertransferhandler);
}
