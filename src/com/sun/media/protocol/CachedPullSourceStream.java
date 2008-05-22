// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CachedPullSourceStream.java

package com.sun.media.protocol;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.*;
import com.sun.media.util.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.DownloadProgressListener;
import javax.media.protocol.*;

public class CachedPullSourceStream
    implements Runnable, PullSourceStream, Seekable, CachedStream
{

    public CachedPullSourceStream(InputStream stream, String fileName, long contentLength, String protocol)
        throws IOException
    {
        readRAF = null;
        writeRAF = null;
        bufferSize = 2048;
        buffer = new byte[bufferSize];
        eosReached = false;
        ioException = false;
        readAborted = false;
        paused = false;
        abort = false;
        highMarkFactor = 10;
        blockRead = true;
        highMark = DEFAULT_HIGH_MARK;
        lowMark = 0;
        enabled = true;
        jitterEnabled = true;
        listener = null;
        numKiloBytesUpdateIncrement = -1;
        closed = true;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        maxCacheSize = 0x7fffffff;
        this.stream = stream;
        this.contentLength = contentLength;
        this.fileName = fileName;
        this.protocol = protocol;
        if(jmfSecurity != null)
        {
            String permission = null;
            int permissionid = 0;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    try
                    {
                        permission = "thread";
                        jmfSecurity.requestPermission(m, cl, args, 16);
                        m[0].invoke(cl[0], args[0]);
                        permission = "thread group";
                        jmfSecurity.requestPermission(m, cl, args, 32);
                        m[0].invoke(cl[0], args[0]);
                    }
                    catch(Throwable t) { }
                    permission = "read file";
                    permissionid = 2;
                    jmfSecurity.requestPermission(m, cl, args, 2);
                    m[0].invoke(cl[0], args[0]);
                    permission = "write file";
                    permissionid = 4;
                    jmfSecurity.requestPermission(m, cl, args, 4);
                    m[0].invoke(cl[0], args[0]);
                    permission = "delete file";
                    permissionid = 8;
                    jmfSecurity.requestPermission(m, cl, args, 8);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.FILEIO);
                    PolicyEngine.assertPermission(PermissionID.FILEIO);
                    try
                    {
                        PolicyEngine.checkPermission(PermissionID.THREAD);
                        PolicyEngine.assertPermission(PermissionID.THREAD);
                    }
                    catch(Throwable t) { }
                }
            }
            catch(Exception e)
            {
                if(permissionid > 0)
                    jmfSecurity.permissionFailureNotification(permissionid);
                securityPrivelege = false;
            }
        }
        if(!securityPrivelege)
            throw new IOException("No security privilege for caching");
        createFilesAndThread(fileName);
        Object cdir = Registry.get("secure.maxCacheSizeMB");
        if(cdir != null && (cdir instanceof Integer))
        {
            int size = ((Integer)cdir).intValue();
            if(size < 1)
                size = 1;
            maxCacheSize = size * 0xf4240;
        }
        highMark = getHighMark(contentLength);
        closed = false;
    }

    private int getHighMark(long contentLength)
    {
        if(contentLength <= 0L)
            return DEFAULT_HIGH_MARK;
        long tryHighMark = contentLength / (long)highMarkFactor;
        if(tryHighMark < (long)MIN_HIGH_MARK)
            tryHighMark = MIN_HIGH_MARK;
        else
        if(tryHighMark > (long)MAX_HIGH_MARK)
            tryHighMark = MAX_HIGH_MARK;
        return (int)tryHighMark;
    }

    public void setEnabledBuffering(boolean b)
    {
        jitterEnabled = b;
    }

    public boolean getEnabledBuffering()
    {
        return jitterEnabled;
    }

    private void createFilesAndThread(String fileName)
        throws IOException
    {
        try
        {
            file = new File(fileName);
            String parent = file.getParent();
            File parentFile = null;
            if(parent != null)
                parentFile = new File(parent);
            if(securityPrivelege && jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons;
                if(parentFile != null)
                {
                    cons = jdk12MakeDirectoryAction.cons;
                    Boolean success = (Boolean)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            parentFile
                        })
                    });
                    if(success == null || !success.booleanValue())
                        throw new IOException("Unable to create directory " + parentFile);
                }
                cons = jdk12RandomAccessFileAction.cons;
                writeRAF = (RandomAccessFile)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        file.getPath(), "rw"
                    })
                });
                if(writeRAF == null)
                    throw new IOException("Cannot create cache file");
                readRAF = (RandomAccessFile)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        file.getPath(), "r"
                    })
                });
                if(readRAF == null)
                    throw new IOException("Cannot create cache file");
                cons = jdk12CreateThreadRunnableAction.cons;
                downloadThread = (MediaThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.util.MediaThread.class, this
                    })
                });
                downloadThread.setName("download");
                cons = jdk12PriorityAction.cons;
                jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        downloadThread, new Integer(getVideoPriority())
                    })
                });
            } else
            {
                if(parentFile != null && !parentFile.exists() && !parentFile.mkdirs())
                    throw new IOException("Unable to create directory " + parentFile);
                writeRAF = new RandomAccessFile(file, "rw");
                readRAF = new RandomAccessFile(file, "r");
                downloadThread = new MediaThread(this, "download");
                downloadThread.useVideoPriority();
            }
        }
        catch(Throwable e)
        {
            throw new IOException(e.getMessage());
        }
    }

    private synchronized void setLength(long length)
    {
        this.length = length;
    }

    private synchronized long getLength()
    {
        return length;
    }

    public void run()
    {
        int totalBytesRead = 0;
        int nextUpdate = numKiloBytesUpdateIncrement;
        int debugIndex = 1;
        if(ioException)
            return;
        while(!eosReached) 
        {
            if(abort)
                return;
            try
            {
                if(contentLength > 0L && !protocol.equals("https"))
                    while(stream.available() == 0) 
                    {
                        synchronized(this)
                        {
                            try
                            {
                                wait(25L);
                            }
                            catch(InterruptedException e) { }
                        }
                        if(abort)
                            return;
                    }
                while(paused) 
                    synchronized(this)
                    {
                        try
                        {
                            wait(1000L);
                        }
                        catch(InterruptedException e) { }
                        if(abort)
                            return;
                    }
                int bytesRead = stream.read(buffer, 0, buffer.length);
                if(bytesRead != -1)
                {
                    if(getLength() + (long)bytesRead > (long)maxCacheSize)
                    {
                        Log.warning("MAX CACHESIZE of " + maxCacheSize + " reached ");
                        contentLength = totalBytesRead;
                        eosReached = true;
                    }
                    writeRAF.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    long length = totalBytesRead;
                    setLength(length);
                    if(length == contentLength)
                        eosReached = true;
                    if(listener != null && totalBytesRead >= nextUpdate)
                    {
                        listener.downloadUpdate();
                        nextUpdate += numKiloBytesUpdateIncrement;
                    }
                } else
                {
                    setLength(totalBytesRead);
                    contentLength = totalBytesRead;
                    eosReached = true;
                }
                loadUpdate();
                continue;
            }
            catch(IOException e)
            {
                Log.warning(e + " : Check if you have enough space in the cache directory");
                ioException = true;
                eosReached = true;
                blockRead = false;
            }
            break;
        }
        if(listener != null)
            listener.downloadUpdate();
        if(writeRAF != null)
        {
            try
            {
                writeRAF.close();
                writeRAF = null;
            }
            catch(IOException e) { }
            writeRAF = null;
        }
    }

    void startDownload()
    {
        if(enabled && downloadThread != null)
            downloadThread.start();
    }

    void pauseDownload()
    {
        if(downloadThread != null && !downloadThread.isAlive())
            return;
        if(enabled)
            synchronized(this)
            {
                if(!paused)
                {
                    paused = true;
                    notify();
                }
            }
    }

    void resumeDownload()
    {
        if(downloadThread != null && !downloadThread.isAlive())
            return;
        if(enabled)
            synchronized(this)
            {
                if(paused)
                {
                    paused = false;
                    notify();
                }
            }
    }

    public void abortDownload()
    {
        abort = true;
    }

    public void abortRead()
    {
        synchronized(this)
        {
            readAborted = true;
        }
    }

    public long seek(long where)
    {
        int debugTime = 0;
        synchronized(this)
        {
            readAborted = false;
        }
        try
        {
            if((!jitterEnabled || !drainCondition(where)) && where <= getLength())
            {
                long l = doSeek(where);
                return l;
            }
            do
            {
                if(eosReached)
                {
                    if(where <= getLength())
                    {
                        long l1 = doSeek(where);
                        return l1;
                    }
                    long l2 = -1L;
                    return l2;
                }
                if(jitterEnabled)
                    synchronized(this)
                    {
                        while(blockRead) 
                        {
                            try
                            {
                                wait(100L);
                            }
                            catch(InterruptedException e) { }
                            if(readAborted)
                            {
                                readAborted = false;
                                long l5 = -2L;
                                return l5;
                            }
                        }
                    }
                if(readAborted)
                {
                    readAborted = false;
                    long l3 = -2L;
                    return l3;
                }
                if(where <= getLength())
                {
                    long l4 = doSeek(where);
                    return l4;
                }
                try
                {
                    Thread.currentThread();
                    Thread.sleep(250L);
                }
                catch(InterruptedException e) { }
            } while(true);
        }
        finally
        {
            if(jitterEnabled)
                drainCondition(where);
        }
    }

    private long getWriteReadPtrOffset()
    {
        return getLength() - tell();
    }

    private synchronized void loadUpdate()
    {
        if(blockRead && (eosReached || getWriteReadPtrOffset() >= (long)highMark))
        {
            blockRead = false;
            synchronized(this)
            {
                notify();
            }
        }
    }

    private synchronized boolean drainCondition()
    {
        return drainCondition(tell());
    }

    private synchronized boolean drainCondition(long offset)
    {
        offset = getLength() - offset;
        if(eosReached)
        {
            if(blockRead)
            {
                blockRead = false;
                notify();
            }
            return false;
        }
        if(blockRead)
            if(offset < (long)highMark)
            {
                return true;
            } else
            {
                blockRead = false;
                notify();
                return false;
            }
        if(offset < (long)lowMark)
        {
            blockRead = true;
            return true;
        } else
        {
            return false;
        }
    }

    public boolean willReadBytesBlock(long offset, int numBytes)
    {
        if(jitterEnabled && drainCondition(offset))
            return true;
        else
            return offset + (long)numBytes > getLength();
    }

    public boolean willReadBytesBlock(int numBytes)
    {
        return willReadBytesBlock(tell(), numBytes);
    }

    private int waitUntilSeekWillSucceed(long where)
        throws IOException
    {
        boolean debugPrint = true;
        if((!jitterEnabled || !drainCondition(where)) && where <= getLength())
            return 0;
        do
        {
            if(eosReached)
                return where > getLength() ? -1 : 0;
            if(jitterEnabled)
                synchronized(this)
                {
                    while(blockRead) 
                    {
                        try
                        {
                            wait(100L);
                        }
                        catch(InterruptedException e) { }
                        if(readAborted)
                        {
                            byte byte0 = -2;
                            return byte0;
                        }
                    }
                }
            if(readAborted)
                return -2;
            if(where <= getLength())
                return 0;
            try
            {
                Thread.currentThread();
                Thread.sleep(250L);
            }
            catch(InterruptedException e) { }
        } while(true);
    }

    public long tell()
    {
        synchronized(this)
        {
            if(closed)
            {
                long l = -1L;
                return l;
            }
            try
            {
                long l1 = readRAF.getFilePointer();
                return l1;
            }
            catch(IOException e)
            {
                long l2 = -1L;
                return l2;
            }
        }
    }

    private synchronized long doSeek(long where)
    {
        if(closed)
            return -1L;
        try
        {
            readRAF.seek(where);
            return readRAF.getFilePointer();
        }
        catch(IOException e)
        {
            return -1L;
        }
    }

    public synchronized int doRead(byte buffer[], int offset, int length)
        throws IOException
    {
        if(closed)
            return -1;
        try
        {
            int actual = readRAF.read(buffer, offset, length);
            return actual;
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }
        return -2;
    }

    private synchronized void doClose()
    {
        try
        {
            closed = true;
            if(readRAF != null)
                readRAF.close();
            if(writeRAF != null)
                writeRAF.close();
            if(file == null)
                return;
            deleteFile(file);
            file = null;
        }
        catch(IOException e) { }
    }

    public boolean isRandomAccess()
    {
        if(enabled)
            return true;
        try
        {
            Seekable s = (Seekable)stream;
            return s.isRandomAccess();
        }
        catch(ClassCastException e)
        {
            return false;
        }
    }

    public boolean willReadBlock()
    {
        return false;
    }

    public int read(byte buffer[], int offset, int length)
        throws IOException
    {
        try
        {
            int result = waitUntilSeekWillSucceed(tell() + (long)length);
            if(result == -1)
            {
                byte byte0 = -1;
                return byte0;
            }
            if(result != -2)
            {
                int i = doRead(buffer, offset, length);
                return i;
            }
            int j = result;
            return j;
        }
        finally
        {
            if(jitterEnabled)
                drainCondition();
        }
    }

    public ContentDescriptor getContentDescriptor()
    {
        return null;
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

    void close()
    {
        if(!abort)
            abortDownload();
        if(downloadThread != null)
        {
            for(int i = 0; i < 20; i++)
            {
                if(!downloadThread.isAlive())
                    break;
                try
                {
                    Thread.currentThread();
                    Thread.sleep(100L);
                }
                catch(InterruptedException e) { }
            }

        }
        doClose();
    }

    public long getContentLength()
    {
        return contentLength;
    }

    long getContentProgress()
    {
        return length;
    }

    void addDownloadProgressListener(DownloadProgressListener l, int numKiloBytes)
    {
        listener = l;
        if(numKiloBytes <= 0)
            numKiloBytes = 1024;
        numKiloBytesUpdateIncrement = numKiloBytes * 1024;
    }

    void removeDownloadProgressListener(DownloadProgressListener l)
    {
        listener = null;
    }

    long getStartOffset()
    {
        return 0L;
    }

    long getEndOffset()
    {
        return length;
    }

    private boolean deleteFile(File file)
    {
        boolean fileDeleted = false;
        try
        {
            if(jmfSecurity != null)
                try
                {
                    if(jmfSecurity.getName().startsWith("jmf-security"))
                    {
                        jmfSecurity.requestPermission(m, cl, args, 8);
                        m[0].invoke(cl[0], args[0]);
                    } else
                    if(jmfSecurity.getName().startsWith("internet"))
                    {
                        PolicyEngine.checkPermission(PermissionID.FILEIO);
                        PolicyEngine.assertPermission(PermissionID.FILEIO);
                    }
                }
                catch(Exception e)
                {
                    securityPrivelege = false;
                }
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12DeleteFileAction.cons;
                Boolean success = (Boolean)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        file
                    })
                });
                fileDeleted = success.booleanValue();
            } else
            {
                fileDeleted = file.delete();
            }
        }
        catch(Throwable e) { }
        return fileDeleted;
    }

    boolean isDownloading()
    {
        if(eosReached)
            return false;
        else
            return length != -1L;
    }

    private InputStream stream;
    private RandomAccessFile readRAF;
    private RandomAccessFile writeRAF;
    private String fileName;
    private int bufferSize;
    private byte buffer[];
    private boolean eosReached;
    private boolean ioException;
    private long length;
    private File file;
    private String protocol;
    private boolean readAborted;
    private boolean paused;
    private boolean abort;
    private MediaThread downloadThread;
    private long contentLength;
    private int highMarkFactor;
    private boolean blockRead;
    private static int MAX_HIGH_MARK = 0x1e8480;
    private static int DEFAULT_HIGH_MARK = 0xf4240;
    private static int MIN_HIGH_MARK = 8192;
    private int highMark;
    private int lowMark;
    private boolean enabled;
    private boolean jitterEnabled;
    private DownloadProgressListener listener;
    private int numKiloBytesUpdateIncrement;
    private boolean closed;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    private int maxCacheSize;

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
