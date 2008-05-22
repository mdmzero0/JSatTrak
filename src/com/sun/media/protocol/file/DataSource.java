// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package com.sun.media.protocol.file;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.MimeManager;
import com.sun.media.util.ContentType;
import com.sun.media.util.JMFI18N;
import com.sun.media.util.jdk12;
import com.sun.media.util.jdk12RandomAccessFileAction;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import javax.media.Duration;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;
import javax.media.protocol.SourceCloneable;

public class DataSource extends PullDataSource
    implements SourceCloneable
{
    class RAFPullSourceStream
        implements PullSourceStream, Seekable
    {

        public long seek(long where)
        {
            try
            {
                raf.seek(where);
                return tell();
            }
            catch(IOException e)
            {
                System.out.println("seek: " + e);
            }
            return -1L;
        }

        public long tell()
        {
            try
            {
                return raf.getFilePointer();
            }
            catch(IOException e)
            {
                System.out.println("tell: " + e);
            }
            return -1L;
        }

        public boolean isRandomAccess()
        {
            return true;
        }

        public boolean willReadBlock()
        {
            return false;
        }

        public int read(byte buffer[], int offset, int length)
            throws IOException
        {
            return raf.read(buffer, offset, length);
        }

        public ContentDescriptor getContentDescriptor()
        {
            return null;
        }

        public long getContentLength()
        {
            return length;
        }

        public boolean endOfStream()
        {
            return false;
        }

        public Object[] getControls()
        {
            return new Object[0];
        }

        public Object getControl(String controlType)
        {
            return null;
        }

        RAFPullSourceStream()
        {
        }
    }


    public DataSource()
    {
        connected = false;
        length = -1L;
        contentType = null;
        pssArray = new PullSourceStream[1];
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
    }

    public String getContentType()
    {
        if(!connected)
            return null;
        else
            return contentType;
    }

    public void connect()
        throws IOException
    {
        if(connected)
            return;
        MediaLocator locator = getLocator();
        if(locator == null)
        {
            System.err.println("medialocator is null");
            throw new IOException(this + ": connect() failed");
        }
        URL url;
        try
        {
            url = locator.getURL();
        }
        catch(MalformedURLException e)
        {
            System.err.println(getLocator() + ": Don't know how to deal with non-URL locator yet!");
            throw new IOException(this + ": connect() failed");
        }
        String fileName = getFileName(locator);
        if(jmfSecurity != null)
        {
            int i = fileName.lastIndexOf(".");
            if(i != -1)
            {
                String ext = fileName.substring(i + 1).toLowerCase();
                if(!mimeTable.containsKey(ext) && !ext.equalsIgnoreCase("aif"))
                    throw new IOException("Permission Denied: From an applet cannot read media file with extension " + ext);
            } else
            {
                throw new IOException("For security reasons, from an applet, cannot read a media file with no extension");
            }
        }
        try
        {
            if(jmfSecurity != null)
                try
                {
                    if(jmfSecurity.getName().startsWith("jmf-security"))
                    {
                        jmfSecurity.requestPermission(m, cl, args, 2);
                        m[0].invoke(cl[0], args[0]);
                    } else
                    if(jmfSecurity.getName().startsWith("internet"))
                    {
                        PolicyEngine.checkPermission(PermissionID.FILEIO);
                        PolicyEngine.assertPermission(PermissionID.FILEIO);
                    }
                }
                catch(Throwable e)
                {
                    jmfSecurity.permissionFailureNotification(2);
                    throw new IOException("No permissions to read file");
                }
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
                try
                {
                    Constructor cons = jdk12RandomAccessFileAction.cons;
                    raf = (RandomAccessFile)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            fileName, "r"
                        })
                    });
                }
                catch(Throwable e)
                {
                    throw new IOException(JMFI18N.getResource("error.filenotfound"));
                }
            else
                raf = new RandomAccessFile(fileName, "r");
            length = raf.length();
            if(length < 0L)
                length = -1L;
            PullSourceStream pss = new RAFPullSourceStream();
            pssArray[0] = pss;
            URLConnection urlC = url.openConnection();
            try
            {
                contentType = urlC.getContentType();
            }
            catch(Throwable t)
            {
                contentType = null;
            }
            contentType = ContentType.getCorrectedContentType(contentType, locator.getRemainder());
            contentType = ContentDescriptor.mimeTypeToPackageName(contentType);
            connected = true;
        }
        catch(Throwable e)
        {
            throw new IOException(JMFI18N.getResource("error.filenotfound"));
        }
    }

    public void disconnect()
    {
        try
        {
            if(raf != null)
                raf.close();
        }
        catch(IOException e) { }
        if(pssArray != null)
            pssArray[0] = null;
        connected = false;
    }

    public void start()
        throws IOException
    {
    }

    public void stop()
        throws IOException
    {
    }

    public void setLocator(MediaLocator ml)
    {
        if(ml != null && ml.getProtocol() != null && ml.getProtocol().equals("file"))
        {
            MediaLocator saved = ml;
            String file = ml.getRemainder();
            boolean changed = false;
            if(file == null)
            {
                super.setLocator(ml);
                return;
            }
            try
            {
                for(int idx = 0; (idx = file.indexOf("%", idx)) >= 0; idx++)
                    if(file.length() > idx + 2)
                    {
                        byte bytes[] = new byte[1];
                        try
                        {
                            bytes[0] = (byte)Integer.valueOf(file.substring(idx + 1, idx + 3), 16).intValue();
                            file = file.substring(0, idx) + new String(bytes) + file.substring(idx + 3);
                            changed = true;
                        }
                        catch(NumberFormatException ne) { }
                    }

                if(changed)
                    ml = new MediaLocator(ml.getProtocol() + ":" + file);
            }
            catch(Exception e)
            {
                ml = saved;
            }
        }
        super.setLocator(ml);
    }

    public PullSourceStream[] getStreams()
    {
        return pssArray;
    }

    public Time getDuration()
    {
        return Duration.DURATION_UNKNOWN;
    }

    public Object[] getControls()
    {
        return new Object[0];
    }

    public Object getControl(String controlType)
    {
        return null;
    }

    public javax.media.protocol.DataSource createClone()
    {
        DataSource ds = new DataSource();
        ds.setLocator(getLocator());
        if(connected)
            try
            {
                ds.connect();
            }
            catch(IOException e)
            {
                return null;
            }
        return ds;
    }

    public static String getFileName(MediaLocator locator)
    {
        try
        {
            URL url = locator.getURL();
            String fileName = locator.getRemainder();
            String saved = fileName;
            try
            {
                for(int idx = 0; (idx = fileName.indexOf("%", idx)) >= 0; idx++)
                    if(fileName.length() > idx + 2)
                    {
                        byte bytes[] = new byte[1];
                        try
                        {
                            bytes[0] = (byte)Integer.valueOf(fileName.substring(idx + 1, idx + 3), 16).intValue();
                            fileName = fileName.substring(0, idx) + new String(bytes) + fileName.substring(idx + 3);
                        }
                        catch(NumberFormatException ne) { }
                    }

                for(int idx = 0; (idx = fileName.indexOf("|")) >= 0;)
                    if(idx > 0)
                        fileName = fileName.substring(0, idx) + ":" + fileName.substring(idx + 1);
                    else
                        fileName = fileName.substring(1);

                for(; fileName.startsWith("///"); fileName = fileName.substring(2));
                if(System.getProperty("os.name").startsWith("Windows"))
                    for(; fileName.charAt(0) == '/' && fileName.charAt(2) == ':'; fileName = fileName.substring(1));
            }
            catch(Exception e)
            {
                fileName = saved;
            }
            return fileName;
        }
        catch(Throwable t)
        {
            return null;
        }
    }

    private RandomAccessFile raf;
    private boolean connected;
    private long length;
    private String contentType;
    private PullSourceStream pssArray[];
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    private static Hashtable mimeTable;

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
            mimeTable = MimeManager.getDefaultMimeTable();
        }
        catch(SecurityException e) { }
    }


}
