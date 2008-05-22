// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package com.sun.media.protocol;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.Log;
import com.sun.media.ui.CacheControlComponent;
import com.sun.media.util.ContentType;
import com.sun.media.util.JMFI18N;
import com.sun.media.util.Registry;
import com.sun.media.util.jdk12;
import com.sun.media.util.jdk12ConnectionAction;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import javax.media.DownloadProgressListener;
import javax.media.ExtendedCachingControl;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;

// Referenced classes of package com.sun.media.protocol:
//            CachedPullSourceStream, BasicPullSourceStream

public class DataSource extends PullDataSource
{
    class CachingControl
        implements ExtendedCachingControl
    {

        public boolean isDownloading()
        {
            return cpss.isDownloading();
        }

        public long getContentLength()
        {
            return contentLength;
        }

        public long getContentProgress()
        {
            return cpss.getContentProgress();
        }

        public Component getProgressBarComponent()
        {
            return progressBar;
        }

        public Component getControlComponent()
        {
            return controlComponent;
        }

        public void pauseDownload()
        {
            cpss.pauseDownload();
        }

        public void resumeDownload()
        {
            cpss.resumeDownload();
        }

        public long getStartOffset()
        {
            return cpss.getStartOffset();
        }

        public long getEndOffset()
        {
            return cpss.getEndOffset();
        }

        public void setBufferSize(Time time)
        {
        }

        public Time getBufferSize()
        {
            return null;
        }

        public void addDownloadProgressListener(DownloadProgressListener l, int numKiloBytes)
        {
            cpss.addDownloadProgressListener(l, numKiloBytes);
        }

        public void removeDownloadProgressListener(DownloadProgressListener l)
        {
            cpss.removeDownloadProgressListener(l);
        }

        private CacheControlComponent controlComponent;
        private Component progressBar;
        private CachedPullSourceStream cpss;

        CachingControl(CachedPullSourceStream cpss)
        {
            this.cpss = cpss;
            controlComponent = new CacheControlComponent(this, null);
            progressBar = controlComponent.getProgressBar();
        }
    }


    public DataSource()
    {
        connected = false;
        contentType = null;
        pssArray = new PullSourceStream[1];
        cachedStream = null;
        contentLength = -1L;
        fileSeparator = System.getProperty("file.separator");
        downLoadThreadStarted = false;
        isEnabledCaching = false;
        cachingControls = new ExtendedCachingControl[0];
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
            throw new IOException(this + ": connect() failed");
        URL url;
        URLConnection urlC;
        try
        {
            url = locator.getURL();
            urlC = url.openConnection();
            urlC.setAllowUserInteraction(true);
        }
        catch(MalformedURLException e)
        {
            throw new IOException(this + ": connect() failed");
        }
        String protocol = url.getProtocol();
        boolean needConnectPermission = true;
        try
        {
            inputStream = urlC.getInputStream();
            needConnectPermission = false;
        }
        catch(Throwable e) { }
        if(inputStream == null)
        {
            if(jmfSecurity != null)
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
                catch(Exception e)
                {
                    jmfSecurity.permissionFailureNotification(128);
                    throw new IOException("Unable to get connect permission" + e.getMessage());
                }
            try
            {
                if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
                {
                    Constructor cons = jdk12ConnectionAction.cons;
                    inputStream = (InputStream)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            urlC
                        })
                    });
                } else
                {
                    inputStream = urlC.getInputStream();
                }
            }
            catch(Throwable e)
            {
                throw new IOException(JMFI18N.getResource("error.connectionerror") + e.getMessage());
            }
        }
        if(inputStream == null)
            throw new IOException(JMFI18N.getResource("error.connectionerror") + "Unable to open a URL connection");
        if(protocol.equals("ftp"))
        {
            contentType = "content/unknown";
        } else
        {
            contentType = urlC.getContentType();
            contentLength = urlC.getContentLength();
        }
        contentType = ContentType.getCorrectedContentType(contentType, locator.getRemainder());
        contentType = ContentDescriptor.mimeTypeToPackageName(contentType);
        boolean cachingRequested = ((Boolean)Manager.getHint(2)).booleanValue();
        if(contentType.endsWith(".mvr") || contentType.endsWith("x_shockwave_flash") || contentType.endsWith("futuresplash"))
            cachingRequested = false;
        String filePrefix = null;
        if(cachingRequested)
        {
            filePrefix = Manager.getCacheDirectory();
            if(filePrefix != null)
            {
                Object allowCachingObj = Registry.get("secure.allowCaching");
                if(allowCachingObj != null)
                    isEnabledCaching = ((Boolean)allowCachingObj).booleanValue();
            }
        }
        if(isEnabledCaching)
        {
            String fileName = filePrefix + fileSeparator + generateFileName(getLocator().getRemainder());
            try
            {
                cachedStream = new CachedPullSourceStream(inputStream, fileName, contentLength, protocol);
                pssArray[0] = cachedStream;
                cachingControls = new ExtendedCachingControl[1];
                cachingControls[0] = new CachingControl(cachedStream);
                Log.comment("Caching in " + filePrefix);
            }
            catch(IOException e)
            {
                isEnabledCaching = false;
            }
        }
        if(!isEnabledCaching)
            try
            {
                pssArray[0] = new BasicPullSourceStream(url, inputStream, contentLength, needConnectPermission);
                cachedStream = null;
            }
            catch(Exception ie)
            {
                pssArray[0] = null;
                throw new IOException(JMFI18N.getResource("error.connectionerror") + ie.getMessage());
            }
        connected = true;
    }

    public void disconnect()
    {
        if(!connected)
            return;
        if(cachedStream != null)
        {
            cachedStream.close();
            cachedStream = null;
        }
        pssArray[0] = null;
        connected = false;
    }

    public void start()
        throws IOException
    {
        if(!connected)
            return;
        if(cachedStream != null)
            if(!downLoadThreadStarted)
            {
                cachedStream.startDownload();
                downLoadThreadStarted = true;
            } else
            {
                cachedStream.resumeDownload();
            }
    }

    public void stop()
        throws IOException
    {
        if(!connected)
            return;
        else
            return;
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
        return cachingControls;
    }

    public Object getControl(String controlType)
    {
        if(cachingControls.length > 0 && controlType.equals("javax.media.CachingControl"))
            return cachingControls[0];
        else
            return null;
    }

    public static String generateFileName(String infile)
    {
        String ext = null;
        int sepindex = 0;
        Random generator = new Random();
        int dotindex = infile.lastIndexOf('.');
        int suffix = generator.nextInt();
        if(dotindex != -1)
            ext = new String(infile.substring(dotindex));
        else
            dotindex = infile.length();
        sepindex = infile.lastIndexOf(File.separatorChar);
        sepindex = Math.max(infile.lastIndexOf('/'), sepindex);
        if(sepindex >= dotindex)
        {
            dotindex = infile.length();
            ext = null;
        }
        String filename = infile.substring(sepindex + 1, dotindex);
        String in;
        if(ext != null)
            in = new String(filename + suffix + ext);
        else
            in = new String(filename + suffix);
        return convertNonAlphaNumericToUnderscore(in);
    }

    private static String convertNonAlphaNumericToUnderscore(String in)
    {
        if(in == null)
            return null;
        int len = in.length();
        char nm[] = new char[len];
        in.getChars(0, len, nm, 0);
        for(int i = 0; i < len; i++)
        {
            char c = nm[i];
            if(c != '.' && ('A' > c || c > 'Z') && ('a' > c || c > 'z') && ('0' > c || c > '9'))
                nm[i] = '_';
        }

        return new String(nm);
    }

    protected boolean connected;
    private String contentType;
    private PullSourceStream pssArray[];
    private CachedPullSourceStream cachedStream;
    private long contentLength;
    private InputStream inputStream;
    private String fileSeparator;
    private boolean downLoadThreadStarted;
    private boolean isEnabledCaching;
    private ExtendedCachingControl cachingControls[];
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
