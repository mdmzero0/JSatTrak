// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PushBufferStream.java

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
