// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   InputSourceStream.java

package javax.media.protocol;

import java.io.IOException;
import java.io.InputStream;

// Referenced classes of package javax.media.protocol:
//            PullSourceStream, ContentDescriptor

public class InputSourceStream
    implements PullSourceStream
{

    public InputSourceStream(InputStream s, ContentDescriptor type)
    {
        stream = s;
        eosReached = false;
        contentType = type;
    }

    public ContentDescriptor getContentDescriptor()
    {
        return contentType;
    }

    public long getContentLength()
    {
        return -1L;
    }

    public boolean willReadBlock()
    {
        if(eosReached)
            return true;
        try
        {
            return stream.available() == 0;
        }
        catch(IOException e)
        {
            return true;
        }
    }

    public int read(byte buffer[], int offset, int length)
        throws IOException
    {
        int bytesRead = stream.read(buffer, offset, length);
        if(bytesRead == -1)
            eosReached = true;
        return bytesRead;
    }

    public void close()
        throws IOException
    {
        stream.close();
    }

    public boolean endOfStream()
    {
        return eosReached;
    }

    public Object[] getControls()
    {
        return new Object[0];
    }

    public Object getControl(String controlName)
    {
        return null;
    }

    protected InputStream stream;
    protected boolean eosReached;
    ContentDescriptor contentType;
}
