// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package com.sun.media.protocol.avi;

import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.util.ContentType;
import com.sun.media.util.JMFI18N;
import java.io.*;
import java.lang.reflect.Method;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.*;

class FileDataSource extends PullDataSource
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


    public FileDataSource(String fileName)
        throws IOException
    {
        connected = false;
        length = -1L;
        contentType = null;
        pssArray = new PullSourceStream[1];
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        this.fileName = fileName;
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
        try
        {
            if(securityPrivelege && jmfSecurity != null)
                try
                {
                    jmfSecurity.requestPermission(m, cl, args, 2);
                    m[0].invoke(cl[0], args[0]);
                }
                catch(Exception e)
                {
                    System.err.println("Unable to get read file privilege  " + e);
                    securityPrivelege = false;
                }
            contentType = ContentType.getCorrectedContentType("content/unknown", fileName);
            contentType = ContentType.getCorrectedContentType("content/unknown", fileName);
            contentType = ContentDescriptor.mimeTypeToPackageName(contentType);
            System.out.println("contentType is " + contentType);
            raf = new RandomAccessFile(fileName, "r");
            length = raf.length();
            if(length < 0L)
                length = -1L;
            PullSourceStream pss = new RAFPullSourceStream();
            pssArray[0] = pss;
            connected = true;
        }
        catch(IOException ioe)
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
        return null;
    }

    public Object[] getControls()
    {
        return new Object[0];
    }

    public Object getControl(String controlType)
    {
        return null;
    }

    protected String getCorrectedContentType(String contentType)
    {
        if(contentType != null)
        {
            if(contentType.equals("audio/wav"))
                contentType = "audio/x-wav";
            else
            if(contentType.equals("audio/aiff"))
                contentType = "audio/x-aiff";
            else
            if(contentType.equals("application/x-troff-msvideo"))
                contentType = "video/x-msvideo";
            else
            if(contentType.equals("video/msvideo"))
                contentType = "video/x-msvideo";
            else
            if(contentType.equals("audio/x-mpegaudio"))
                contentType = "audio/mpeg";
            else
            if(contentType.equals("content/unknown"))
            {
                String type = guessContentType(getLocator());
                if(type != null)
                    contentType = type;
            }
        } else
        {
            contentType = "content/unknown";
        }
        return contentType;
    }

    private String guessContentType(MediaLocator locator)
    {
        String path = locator.getRemainder();
        int i = path.lastIndexOf(".");
        if(i != -1)
        {
            String ext = path.substring(i + 1).toLowerCase();
            if(ext.equals("mov"))
                return "video/quicktime";
            if(ext.equals("avi"))
                return "video/x_msvideo";
            if(ext.equals("mpg"))
                return "video/mpeg";
            if(ext.equals("mpv"))
                return "video/mpeg";
            if(ext.equals("viv"))
                return "video/vivo";
            if(ext.equals("au"))
                return "audio/basic";
            if(ext.equals("wav"))
                return "audio/x_wav";
            if(ext.equals("mid") || ext.equals("midi"))
                return "audio/midi";
            if(ext.equals("rmf"))
                return "audio/rmf";
            if(ext.equals("gsm"))
                return "audio/x_gsm";
            if(ext.equals("mp2"))
                return "audio/mpeg";
            if(ext.equals("mp3"))
                return "audio/mpeg";
            if(ext.equals("mpa"))
                return "audio/mpeg";
            if(ext.equals("swf"))
                return "application/x-shockwave-flash";
            if(ext.equals("spl"))
                return "application/futuresplash";
        }
        return null;
    }

    private String fileName;
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
