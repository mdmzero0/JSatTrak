/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
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
