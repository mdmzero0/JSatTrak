// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   URLDataSource.java

package javax.media.protocol;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import javax.media.*;

// Referenced classes of package javax.media.protocol:
//            PullDataSource, ContentDescriptor, DataSource, InputSourceStream, 
//            PullSourceStream

public class URLDataSource extends PullDataSource
{
    class URLSourceStream extends InputSourceStream
    {

        public long getContentLength()
        {
            long len = conn.getContentLength();
            len = len != -1L ? len : -1L;
            return len;
        }

        protected URLConnection conn;

        public URLSourceStream(URLConnection conn, ContentDescriptor type)
            throws IOException
        {
            super(conn.getInputStream(), type);
            this.conn = conn;
        }
    }


    protected URLDataSource()
    {
    }

    public URLDataSource(URL url)
        throws IOException
    {
        setLocator(new MediaLocator(url));
        connected = false;
    }

    public PullSourceStream[] getStreams()
    {
        if(!connected)
            throw new Error("Unconnected source.");
        else
            return sources;
    }

    public void connect()
        throws IOException
    {
        conn = getLocator().getURL().openConnection();
        conn.connect();
        connected = true;
        String mimeType = conn.getContentType();
        if(mimeType == null)
            mimeType = "UnknownContent";
        contentType = new ContentDescriptor(ContentDescriptor.mimeTypeToPackageName(mimeType));
        sources = new URLSourceStream[1];
        sources[0] = new URLSourceStream(conn, contentType);
    }

    public String getContentType()
    {
        if(!connected)
            throw new Error("Source is unconnected.");
        else
            return contentType.getContentType();
    }

    public void disconnect()
    {
        if(connected)
        {
            try
            {
                sources[0].close();
            }
            catch(IOException e) { }
            connected = false;
        }
    }

    public void start()
        throws IOException
    {
    }

    public void stop()
        throws IOException
    {
    }

    public Time getDuration()
    {
        return Duration.DURATION_UNKNOWN;
    }

    public Object[] getControls()
    {
        return new Object[0];
    }

    public Object getControl(String controlName)
    {
        return null;
    }

    protected URLConnection conn;
    protected ContentDescriptor contentType;
    protected URLSourceStream sources[];
    protected boolean connected;
}
