// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PullSourceStream.java

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
