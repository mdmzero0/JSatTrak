// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Handler.java

package com.sun.media.datasink.file;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.*;
import com.sun.media.datasink.BasicDataSink;
import com.sun.media.datasink.RandomAccess;
import com.sun.media.util.jdk12;
import com.sun.media.util.jdk12DeleteFileAction;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.*;
import javax.media.protocol.*;

public class Handler extends BasicDataSink
    implements SourceTransferHandler, Seekable, Runnable, RandomAccess, Syncable
{

    public Handler()
    {
        state = 0;
        errorEncountered = false;
        errorReason = null;
        tempFile = null;
        raFile = null;
        qtStrRaFile = null;
        fileClosed = false;
        fileDescriptor = null;
        locator = null;
        contentType = null;
        fileSize = 0;
        filePointer = 0;
        bytesWritten = 0;
        syncEnabled = false;
        buffer1 = new byte[0x20000];
        buffer2 = new byte[0x20000];
        buffer1Pending = false;
        buffer1PendingLocation = -1L;
        buffer2Pending = false;
        buffer2PendingLocation = -1L;
        nextLocation = 0L;
        writeThread = null;
        bufferLock = new Integer(0);
        receivedEOS = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        WRITE_CHUNK_SIZE = 16384;
        streamingEnabled = false;
        errorCreatingStreamingFile = false;
        lastSyncTime = -1L;
    }

    public void setSource(DataSource ds)
        throws IncompatibleSourceException
    {
        if(!(ds instanceof PushDataSource) && !(ds instanceof PullDataSource))
            throw new IncompatibleSourceException("Incompatible datasource");
        source = ds;
        if(source instanceof PushDataSource)
        {
            push = true;
            try
            {
                ((PushDataSource)source).connect();
            }
            catch(IOException ioe) { }
            streams = ((PushDataSource)source).getStreams();
        } else
        {
            push = false;
            try
            {
                ((PullDataSource)source).connect();
            }
            catch(IOException ioe) { }
            streams = ((PullDataSource)source).getStreams();
        }
        if(streams == null || streams.length != 1)
            throw new IncompatibleSourceException("DataSource should have 1 stream");
        stream = streams[0];
        contentType = source.getContentType();
        if(push)
            ((PushSourceStream)stream).setTransferHandler(this);
    }

    public void setOutputLocator(MediaLocator output)
    {
        locator = output;
    }

    public void setEnabled(boolean b)
    {
        streamingEnabled = b;
    }

    public void setSyncEnabled()
    {
        syncEnabled = true;
    }

    public boolean write(long inOffset, int numBytes)
    {
        try
        {
            if(inOffset >= 0L && numBytes > 0)
            {
                int remaining = numBytes;
                raFile.seek(inOffset);
                int bytesToRead;
                for(; remaining > 0; remaining -= bytesToRead)
                {
                    bytesToRead = remaining <= 0x20000 ? remaining : 0x20000;
                    raFile.read(buffer1, 0, bytesToRead);
                    qtStrRaFile.write(buffer1, 0, bytesToRead);
                }

            } else
            if(inOffset < 0L && numBytes > 0)
            {
                qtStrRaFile.seek(0L);
                qtStrRaFile.seek(numBytes - 1);
                qtStrRaFile.writeByte(0);
                qtStrRaFile.seek(0L);
            } else
            {
                sendEndofStreamEvent();
            }
        }
        catch(Exception e)
        {
            errorCreatingStreamingFile = true;
            System.err.println("Exception when creating streamable version of media file: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void open()
        throws IOException, SecurityException
    {
        try
        {
            if(state == 0 && locator != null)
            {
                String pathName;
                for(pathName = locator.getRemainder(); pathName.charAt(0) == '/' && (pathName.charAt(1) == '/' || pathName.charAt(2) == ':'); pathName = pathName.substring(1));
                String fileSeparator = System.getProperty("file.separator");
                if(fileSeparator.equals("\\"))
                    pathName = pathName.replace('/', '\\');
                JMFSecurityManager.checkFileSave();
                if(securityPrivelege && jmfSecurity != null)
                {
                    String permission = null;
                    try
                    {
                        if(jmfSecurity.getName().startsWith("jmf-security"))
                        {
                            permission = "read file";
                            jmfSecurity.requestPermission(m, cl, args, 2);
                            m[0].invoke(cl[0], args[0]);
                            permission = "write file";
                            jmfSecurity.requestPermission(m, cl, args, 4);
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
                        if(push)
                            ((PushSourceStream)stream).setTransferHandler(null);
                        throw new SecurityException(e.getMessage());
                    }
                    catch(Error e)
                    {
                        securityPrivelege = false;
                        if(push)
                            ((PushSourceStream)stream).setTransferHandler(null);
                        throw new SecurityException(e.getMessage());
                    }
                }
                if(!securityPrivelege)
                {
                    if(push)
                        ((PushSourceStream)stream).setTransferHandler(null);
                    throw new IOException("Datasink: Unable to get security privileges for file writing");
                }
                file = new File(pathName);
                if(file.exists() && !deleteFile(file))
                {
                    System.err.println("datasink open: Existing file " + pathName + " cannot be deleted. Check if " + "some other process is using " + " this file");
                    if(push)
                        ((PushSourceStream)stream).setTransferHandler(null);
                    throw new IOException("Existing file " + pathName + " cannot be deleted");
                }
                String parent = file.getParent();
                if(parent != null)
                    (new File(parent)).mkdirs();
                try
                {
                    if(!streamingEnabled)
                    {
                        raFile = new RandomAccessFile(file, "rw");
                        fileDescriptor = raFile.getFD();
                    } else
                    {
                        String fileqt;
                        int index;
                        if((index = pathName.lastIndexOf(".")) > 0)
                            fileqt = pathName.substring(0, index) + ".nonstreamable" + pathName.substring(index, pathName.length());
                        else
                            fileqt = file + ".nonstreamable.mov";
                        tempFile = new File(fileqt);
                        raFile = new RandomAccessFile(tempFile, "rw");
                        fileDescriptor = raFile.getFD();
                        qtStrRaFile = new RandomAccessFile(file, "rw");
                    }
                }
                catch(IOException e)
                {
                    System.err.println("datasink open: IOException when creating RandomAccessFile " + pathName + " : " + e);
                    if(push)
                        ((PushSourceStream)stream).setTransferHandler(null);
                    throw e;
                }
                setState(1);
            }
        }
        finally
        {
            if(state == 0 && stream != null)
                ((PushSourceStream)stream).setTransferHandler(null);
        }
    }

    public MediaLocator getOutputLocator()
    {
        return locator;
    }

    public void start()
        throws IOException
    {
        if(state == 1)
        {
            if(source != null)
                source.start();
            if(writeThread == null)
            {
                writeThread = new Thread(this);
                writeThread.start();
            }
            setState(2);
        }
    }

    public void stop()
        throws IOException
    {
        if(state == 2)
        {
            if(source != null)
                source.stop();
            setState(1);
        }
    }

    protected void setState(int state)
    {
        synchronized(this)
        {
            this.state = state;
        }
    }

    public void close()
    {
        close(null);
    }

    protected final void close(String reason)
    {
        synchronized(this)
        {
            if(state == 3)
                return;
            setState(3);
        }
        if(push)
        {
            for(int i = 0; i < streams.length; i++)
                ((PushSourceStream)streams[i]).setTransferHandler(null);

        }
        if(reason != null)
        {
            errorEncountered = true;
            sendDataSinkErrorEvent(reason);
            synchronized(bufferLock)
            {
                bufferLock.notifyAll();
            }
        }
        try
        {
            source.stop();
        }
        catch(IOException e)
        {
            System.err.println("IOException when stopping source " + e);
        }
        try
        {
            if(raFile != null)
                raFile.close();
            if(streamingEnabled && qtStrRaFile != null)
                qtStrRaFile.close();
            if(source != null)
                source.disconnect();
            boolean status;
            if(streamingEnabled && tempFile != null)
                if(!errorCreatingStreamingFile)
                    status = deleteFile(tempFile);
                else
                    status = deleteFile(file);
        }
        catch(IOException e)
        {
            System.out.println("close: " + e);
        }
        raFile = null;
        qtStrRaFile = null;
        removeAllListeners();
    }

    public String getContentType()
    {
        return contentType;
    }

    public Object[] getControls()
    {
        if(controls == null)
            controls = new Control[0];
        return controls;
    }

    public Object getControl(String controlName)
    {
        return null;
    }

    public synchronized void transferData(PushSourceStream pss)
    {
        int totalRead = 0;
        int spaceAvailable = 0x20000;
        int bytesRead = 0;
        if(errorEncountered)
            return;
        if(buffer1Pending)
            synchronized(bufferLock)
            {
                while(buffer1Pending) 
                    try
                    {
                        bufferLock.wait();
                    }
                    catch(InterruptedException ie) { }
            }
        for(; spaceAvailable > 0; spaceAvailable -= bytesRead)
        {
            try
            {
                bytesRead = pss.read(buffer1, totalRead, spaceAvailable);
                if(bytesRead > 16384 && WRITE_CHUNK_SIZE < 32768)
                    if(bytesRead > 0x10000 && WRITE_CHUNK_SIZE < 0x20000)
                        WRITE_CHUNK_SIZE = 0x20000;
                    else
                    if(bytesRead > 32768 && WRITE_CHUNK_SIZE < 0x10000)
                        WRITE_CHUNK_SIZE = 0x10000;
                    else
                    if(WRITE_CHUNK_SIZE < 32768)
                        WRITE_CHUNK_SIZE = 32768;
            }
            catch(IOException ioe) { }
            if(bytesRead <= 0)
                break;
            totalRead += bytesRead;
        }

        if(totalRead > 0)
            synchronized(bufferLock)
            {
                buffer1Pending = true;
                buffer1PendingLocation = nextLocation;
                buffer1Length = totalRead;
                nextLocation = -1L;
                bufferLock.notifyAll();
            }
        if(bytesRead == -1)
        {
            receivedEOS = true;
            while(!fileClosed && !errorEncountered && state != 3) 
                try
                {
                    Thread.sleep(50L);
                }
                catch(InterruptedException ie) { }
        }
    }

    public void run()
    {
        while(state != 3 && !errorEncountered) 
        {
            synchronized(bufferLock)
            {
                while(!buffer1Pending && !buffer2Pending && !errorEncountered && state != 3 && !receivedEOS) 
                    try
                    {
                        bufferLock.wait(500L);
                    }
                    catch(InterruptedException ie) { }
            }
            if(buffer2Pending)
            {
                write(buffer2, buffer2PendingLocation, buffer2Length);
                buffer2Pending = false;
            }
            synchronized(bufferLock)
            {
                if(buffer1Pending)
                {
                    byte tempBuffer[] = buffer2;
                    buffer2 = buffer1;
                    buffer2Pending = true;
                    buffer2PendingLocation = buffer1PendingLocation;
                    buffer2Length = buffer1Length;
                    buffer1Pending = false;
                    buffer1 = tempBuffer;
                    bufferLock.notifyAll();
                } else
                if(receivedEOS)
                    break;
            }
        }
        if(receivedEOS)
        {
            if(raFile != null)
            {
                if(!streamingEnabled)
                {
                    try
                    {
                        raFile.close();
                    }
                    catch(IOException ioe) { }
                    raFile = null;
                }
                fileClosed = true;
            }
            if(!streamingEnabled)
                sendEndofStreamEvent();
        }
        if(errorEncountered && state != 3)
            close(errorReason);
    }

    public synchronized long seek(long where)
    {
        nextLocation = where;
        return where;
    }

    private void write(byte buffer[], long location, int length)
    {
        try
        {
            if(location != -1L)
                doSeek(location);
            int offset = 0;
            while(length > 0) 
            {
                int toWrite = WRITE_CHUNK_SIZE;
                if(length < toWrite)
                    toWrite = length;
                raFile.write(buffer, offset, toWrite);
                bytesWritten += toWrite;
                if(fileDescriptor != null && syncEnabled && bytesWritten >= WRITE_CHUNK_SIZE)
                {
                    bytesWritten -= WRITE_CHUNK_SIZE;
                    fileDescriptor.sync();
                }
                filePointer += toWrite;
                length -= toWrite;
                offset += toWrite;
                if(filePointer > fileSize)
                    fileSize = filePointer;
                Thread.yield();
            }
        }
        catch(IOException ioe)
        {
            errorEncountered = true;
            errorReason = ioe.toString();
        }
    }

    public long doSeek(long where)
    {
        if(raFile != null)
            try
            {
                raFile.seek(where);
                filePointer = (int)where;
                return where;
            }
            catch(IOException ioe)
            {
                close("Error in seek: " + ioe);
            }
        return -1L;
    }

    public long tell()
    {
        return nextLocation;
    }

    public long doTell()
    {
        if(raFile != null)
            try
            {
                return raFile.getFilePointer();
            }
            catch(IOException ioe)
            {
                close("Error in tell: " + ioe);
            }
        return -1L;
    }

    public boolean isRandomAccess()
    {
        return true;
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

    private static final boolean DEBUG = false;
    protected static final int NOT_INITIALIZED = 0;
    protected static final int OPENED = 1;
    protected static final int STARTED = 2;
    protected static final int CLOSED = 3;
    protected int state;
    protected DataSource source;
    protected SourceStream streams[];
    protected SourceStream stream;
    protected boolean push;
    protected boolean errorEncountered;
    protected String errorReason;
    protected Control controls[];
    protected File file;
    protected File tempFile;
    protected RandomAccessFile raFile;
    protected RandomAccessFile qtStrRaFile;
    protected boolean fileClosed;
    protected FileDescriptor fileDescriptor;
    protected MediaLocator locator;
    protected String contentType;
    protected int fileSize;
    protected int filePointer;
    protected int bytesWritten;
    protected static final int BUFFER_LEN = 0x20000;
    protected boolean syncEnabled;
    protected byte buffer1[];
    protected byte buffer2[];
    protected boolean buffer1Pending;
    protected long buffer1PendingLocation;
    protected int buffer1Length;
    protected boolean buffer2Pending;
    protected long buffer2PendingLocation;
    protected int buffer2Length;
    protected long nextLocation;
    protected Thread writeThread;
    private Integer bufferLock;
    private boolean receivedEOS;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    public int WRITE_CHUNK_SIZE;
    private boolean streamingEnabled;
    private boolean errorCreatingStreamingFile;
    long lastSyncTime;

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
