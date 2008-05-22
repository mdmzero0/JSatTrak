// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicPullSourceStream.java

package com.sun.media.protocol;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.util.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import javax.media.protocol.*;

public class BasicPullSourceStream
    implements PullSourceStream, Seekable
{

    public BasicPullSourceStream(URL url, InputStream stream, long contentLength, boolean needConnectPermission)
        throws IOException
    {
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        this.needConnectPermission = needConnectPermission;
        if(stream != null)
        {
            this.stream = stream;
            this.contentLength = contentLength;
        } else
        {
            try
            {
                urlC = url.openConnection();
                this.contentLength = urlC.getContentLength();
                this.stream = urlC.getInputStream();
                if(this.stream == null)
                    throw new IOException("Got null input stream from url connection");
            }
            catch(IOException ie)
            {
                throw new IOException("error in connection");
            }
        }
        location = 0L;
        eofReached = false;
        this.url = url;
    }

    public ContentDescriptor getContentDescriptor()
    {
        return null;
    }

    public boolean endOfStream()
    {
        return eofReached;
    }

    public boolean willReadBlock()
    {
        try
        {
            return stream.available() == 0;
        }
        catch(IOException e)
        {
            System.err.println("Exception PullSourceStream::willReadBlock " + e.toString());
        }
        return true;
    }

    public int read(byte buffer[], int offset, int length)
        throws IOException
    {
        int len = length;
        int off = offset;
        do
        {
            int bytesRead = stream.read(buffer, off, len);
            if(bytesRead == -1)
            {
                eofReached = true;
                int totalBytesRead = length - len;
                return totalBytesRead <= 0 ? -1 : totalBytesRead;
            }
            location += bytesRead;
            len -= bytesRead;
            off += bytesRead;
        } while(len != 0);
        return length;
    }

    public Object[] getControls()
    {
        Object objects[] = new Object[0];
        return objects;
    }

    public Object getControl(String controlType)
    {
        return null;
    }

    public long seek(long where)
    {
        long oldLocation = location;
        location = where;
        try
        {
            if(where < oldLocation)
            {
                reopenStream();
                eofReached = false;
                return skip(stream, where);
            } else
            {
                return skip(stream, where - oldLocation);
            }
        }
        catch(IOException e)
        {
            return 0L;
        }
    }

    void reopenStream()
    {
        try
        {
            if(stream != null)
                stream.close();
            if(needConnectPermission && jmfSecurity != null)
                try
                {
                    if(jmfSecurity.getName().startsWith("jmf-security"))
                    {
                        jmfSecurity.requestPermission(m, cl, args, 128);
                        m[0].invoke(cl[0], args[0]);
                    } else
                    if(jmfSecurity.getName().startsWith("internet"))
                    {
                        PolicyEngine.checkPermission(PermissionID.NETIO);
                        PolicyEngine.assertPermission(PermissionID.NETIO);
                    }
                }
                catch(Throwable e)
                {
                    securityPrivelege = false;
                    throw new IOException(JMFI18N.getResource("error.connectionerror") + e.getMessage());
                }
            urlC = url.openConnection();
            try
            {
                if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
                {
                    Constructor cons = jdk12ConnectionAction.cons;
                    stream = (InputStream)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            urlC
                        })
                    });
                } else
                {
                    stream = urlC.getInputStream();
                }
            }
            catch(Exception e)
            {
                System.err.println("Unable to re-open a URL connection " + e);
                throw new IOException(JMFI18N.getResource("error.connectionerror") + e.getMessage());
            }
        }
        catch(IOException ex) { }
    }

    public long tell()
    {
        return location;
    }

    public boolean isRandomAccess()
    {
        return true;
    }

    public void close()
    {
        try
        {
            stream.close();
            stream = null;
        }
        catch(Exception e)
        {
            System.out.println("BasicPullSourceStream close - IOException");
        }
    }

    public long getContentLength()
    {
        return contentLength;
    }

    private long skip(InputStream istream, long amount)
        throws IOException
    {
        long actual;
        for(long remaining = amount; remaining > 0L; remaining -= actual)
            actual = istream.skip(remaining);

        return amount;
    }

    protected InputStream stream;
    protected long location;
    protected boolean eofReached;
    protected long contentLength;
    protected URL url;
    protected URLConnection urlC;
    private boolean needConnectPermission;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
    }
}
