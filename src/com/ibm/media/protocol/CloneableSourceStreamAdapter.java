// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CloneableSourceStreamAdapter.java

package com.ibm.media.protocol;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.util.*;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import javax.media.*;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

// Referenced classes of package com.ibm.media.protocol:
//            SourceStreamSlave

public class CloneableSourceStreamAdapter
{
    class PushBufferStreamAdapter extends SourceStreamAdapter
        implements PushBufferStream, BufferTransferHandler
    {

        public Format getFormat()
        {
            return ((PushBufferStream)master).getFormat();
        }

        public void read(Buffer buffer)
            throws IOException
        {
            copyAndRead(buffer);
        }

        public void setTransferHandler(BufferTransferHandler transferHandler)
        {
            handler = transferHandler;
            ((PushBufferStream)master).setTransferHandler(this);
        }

        public void transferData(PushBufferStream stream)
        {
            if(handler != null)
                handler.transferData(this);
        }

        BufferTransferHandler handler;

        PushBufferStreamAdapter()
        {
        }
    }

    class PushSourceStreamAdapter extends SourceStreamAdapter
        implements PushSourceStream, SourceTransferHandler
    {

        public int read(byte buffer[], int offset, int length)
            throws IOException
        {
            return copyAndRead(buffer, offset, length);
        }

        public int getMinimumTransferSize()
        {
            return ((PushSourceStream)master).getMinimumTransferSize();
        }

        public void setTransferHandler(SourceTransferHandler transferHandler)
        {
            handler = transferHandler;
            ((PushSourceStream)master).setTransferHandler(this);
        }

        public void transferData(PushSourceStream stream)
        {
            if(handler != null)
                handler.transferData(this);
        }

        SourceTransferHandler handler;

        PushSourceStreamAdapter()
        {
        }
    }

    class PullBufferStreamAdapter extends SourceStreamAdapter
        implements PullBufferStream
    {

        public boolean willReadBlock()
        {
            return ((PullBufferStream)master).willReadBlock();
        }

        public void read(Buffer buffer)
            throws IOException
        {
            copyAndRead(buffer);
        }

        public Format getFormat()
        {
            return ((PullBufferStream)master).getFormat();
        }

        PullBufferStreamAdapter()
        {
        }
    }

    class PullSourceStreamAdapter extends SourceStreamAdapter
        implements PullSourceStream
    {

        public boolean willReadBlock()
        {
            return ((PullSourceStream)master).willReadBlock();
        }

        public int read(byte buffer[], int offset, int length)
            throws IOException
        {
            return copyAndRead(buffer, offset, length);
        }

        PullSourceStreamAdapter()
        {
        }
    }

    class PushBufferStreamSlave extends PushStreamSlave
        implements PushBufferStream, Runnable
    {

        synchronized void setBuffer(Buffer b)
        {
            this.b = b;
            notifyAll();
        }

        public Format getFormat()
        {
            return ((PushBufferStream)master).getFormat();
        }

        public synchronized void read(Buffer buffer)
            throws IOException
        {
            while(b == null && super.connected) 
                try
                {
                    wait(50L);
                }
                catch(InterruptedException e)
                {
                    System.out.println("Exception: " + e);
                }
            if(!super.connected)
            {
                throw new IOException("DataSource is not connected");
            } else
            {
                buffer.copy(b);
                b = null;
                return;
            }
        }

        public int getMinimumTransferSize()
        {
            return ((PushSourceStream)master).getMinimumTransferSize();
        }

        public void setTransferHandler(BufferTransferHandler transferHandler)
        {
            handler = transferHandler;
        }

        BufferTransferHandler getTransferHandler()
        {
            return handler;
        }

        public void run()
        {
            while(!endOfStream() && super.connected) 
            {
                try
                {
                    synchronized(this)
                    {
                        wait();
                    }
                }
                catch(InterruptedException e)
                {
                    System.out.println("Exception: " + e);
                }
                if(super.connected && handler != null)
                    handler.transferData(this);
            }
        }

        BufferTransferHandler handler;
        private Buffer b;

        PushBufferStreamSlave()
        {
        }
    }

    class PushSourceStreamSlave extends PushStreamSlave
        implements PushSourceStream, Runnable
    {

        synchronized void setBuffer(byte buffer[])
        {
            this.buffer = buffer;
            notifyAll();
        }

        public synchronized int read(byte buffer[], int offset, int length)
            throws IOException
        {
            if(length + offset > buffer.length)
                throw new IOException("buffer is too small");
            while(this.buffer == null && super.connected) 
                try
                {
                    wait(50L);
                }
                catch(InterruptedException e)
                {
                    System.out.println("Exception: " + e);
                }
            if(!super.connected)
            {
                throw new IOException("DataSource is not connected");
            } else
            {
                int copyLength = length <= this.buffer.length ? length : this.buffer.length;
                System.arraycopy(this.buffer, 0, buffer, offset, copyLength);
                this.buffer = null;
                return copyLength;
            }
        }

        public int getMinimumTransferSize()
        {
            return ((PushSourceStream)master).getMinimumTransferSize();
        }

        public void setTransferHandler(SourceTransferHandler transferHandler)
        {
            handler = transferHandler;
        }

        SourceTransferHandler getTransferHandler()
        {
            return handler;
        }

        public void run()
        {
            while(!endOfStream() && super.connected) 
            {
                try
                {
                    synchronized(this)
                    {
                        wait();
                    }
                }
                catch(InterruptedException e)
                {
                    System.out.println("Exception: " + e);
                }
                if(super.connected && handler != null)
                    handler.transferData(this);
            }
        }

        SourceTransferHandler handler;
        private byte buffer[];

        PushSourceStreamSlave()
        {
        }
    }

    abstract class PushStreamSlave extends SourceStreamAdapter
        implements SourceStreamSlave, Runnable
    {

        public synchronized void connect()
        {
            if(connected)
                return;
            connected = true;
            if(CloneableSourceStreamAdapter.jmfSecurity != null)
            {
                String permission = null;
                try
                {
                    if(CloneableSourceStreamAdapter.jmfSecurity.getName().startsWith("jmf-security"))
                    {
                        permission = "thread";
                        CloneableSourceStreamAdapter.jmfSecurity.requestPermission(m, cl, args, 16);
                        m[0].invoke(cl[0], args[0]);
                        permission = "thread group";
                        CloneableSourceStreamAdapter.jmfSecurity.requestPermission(m, cl, args, 32);
                        m[0].invoke(cl[0], args[0]);
                    } else
                    if(CloneableSourceStreamAdapter.jmfSecurity.getName().startsWith("internet"))
                    {
                        PolicyEngine.checkPermission(PermissionID.THREAD);
                        PolicyEngine.assertPermission(PermissionID.THREAD);
                    }
                }
                catch(Throwable e)
                {
                    CloneableSourceStreamAdapter.securityPrivelege = false;
                }
            }
            if(CloneableSourceStreamAdapter.jmfSecurity != null && CloneableSourceStreamAdapter.jmfSecurity.getName().startsWith("jdk12"))
                try
                {
                    Constructor cons = jdk12CreateThreadRunnableAction.cons;
                    notifyingThread = (MediaThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            com.sun.media.util.MediaThread.class, this
                        })
                    });
                }
                catch(Exception e) { }
            else
                notifyingThread = new MediaThread(this);
            if(notifyingThread != null)
            {
                if(master instanceof PushBufferStream)
                    if(((PushBufferStream)master).getFormat() instanceof VideoFormat)
                        notifyingThread.useVideoPriority();
                    else
                        notifyingThread.useAudioPriority();
                notifyingThread.start();
            }
        }

        public synchronized void disconnect()
        {
            connected = false;
            notifyAll();
        }

        public abstract void run();

        MediaThread notifyingThread;
        boolean connected;

        PushStreamSlave()
        {
            connected = false;
        }
    }

    class SourceStreamAdapter
        implements SourceStream
    {

        public ContentDescriptor getContentDescriptor()
        {
            return master.getContentDescriptor();
        }

        public long getContentLength()
        {
            return master.getContentLength();
        }

        public boolean endOfStream()
        {
            return master.endOfStream();
        }

        public Object[] getControls()
        {
            return master.getControls();
        }

        public Object getControl(String controlType)
        {
            return master.getControl(controlType);
        }

        SourceStreamAdapter()
        {
        }
    }


    CloneableSourceStreamAdapter(SourceStream master)
    {
        adapter = null;
        slaves = new Vector();
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        numTracks = 0;
        this.master = master;
        if(master instanceof PullSourceStream)
            adapter = new PullSourceStreamAdapter();
        if(master instanceof PullBufferStream)
            adapter = new PullBufferStreamAdapter();
        if(master instanceof PushSourceStream)
            adapter = new PushSourceStreamAdapter();
        if(master instanceof PushBufferStream)
            adapter = new PushBufferStreamAdapter();
    }

    SourceStream getAdapter()
    {
        return adapter;
    }

    SourceStream createSlave()
    {
        SourceStream slave = null;
        if((master instanceof PullSourceStream) || (master instanceof PushSourceStream))
            slave = new PushSourceStreamSlave();
        if((master instanceof PullBufferStream) || (master instanceof PushBufferStream))
            slave = new PushBufferStreamSlave();
        slaves.addElement(slave);
        return slave;
    }

    void copyAndRead(Buffer b)
        throws IOException
    {
        if(master instanceof PullBufferStream)
            ((PullBufferStream)master).read(b);
        if(master instanceof PushBufferStream)
            ((PushBufferStream)master).read(b);
        for(Enumeration e = slaves.elements(); e.hasMoreElements(); Thread.yield())
        {
            Object stream = e.nextElement();
            ((PushBufferStreamSlave)stream).setBuffer((Buffer)b.clone());
        }

    }

    int copyAndRead(byte buffer[], int offset, int length)
        throws IOException
    {
        int totalRead = 0;
        if(master instanceof PullSourceStream)
            totalRead = ((PullSourceStream)master).read(buffer, offset, length);
        if(master instanceof PushSourceStream)
            totalRead = ((PushSourceStream)master).read(buffer, offset, length);
        Object stream;
        byte copyBuffer[];
        for(Enumeration e = slaves.elements(); e.hasMoreElements(); ((PushSourceStreamSlave)stream).setBuffer(copyBuffer))
        {
            stream = e.nextElement();
            copyBuffer = new byte[totalRead];
            System.arraycopy(buffer, offset, copyBuffer, 0, totalRead);
        }

        return totalRead;
    }

    SourceStream master;
    SourceStream adapter;
    Vector slaves;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    protected int numTracks;
    protected Format trackFormats[];

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
