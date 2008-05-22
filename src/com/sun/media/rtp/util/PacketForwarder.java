// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PacketForwarder.java

package com.sun.media.rtp.util;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.util.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

// Referenced classes of package com.sun.media.rtp.util:
//            RTPMediaThread, PacketSource, PacketConsumer

public class PacketForwarder
    implements Runnable
{

    public PacketForwarder(PacketSource s, PacketConsumer c)
    {
        source = null;
        consumer = null;
        closed = false;
        exception = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        source = s;
        consumer = c;
        closed = false;
        exception = null;
    }

    public void startPF()
    {
        startPF(null);
    }

    public void startPF(String threadname)
    {
        if(thread != null)
            throw new IllegalArgumentException("Called start more than once");
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "thread";
                    jmfSecurity.requestPermission(m, cl, args, 16);
                    m[0].invoke(cl[0], args[0]);
                    permission = "thread group";
                    jmfSecurity.requestPermission(m, cl, args, 32);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.THREAD);
                    PolicyEngine.assertPermission(PermissionID.THREAD);
                }
            }
            catch(Throwable e)
            {
                if(permission.endsWith("group"))
                    jmfSecurity.permissionFailureNotification(32);
                else
                    jmfSecurity.permissionFailureNotification(16);
            }
        }
        if(threadname == null)
            threadname = "RTPMediaThread";
        if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
        {
            try
            {
                Constructor cons = jdk12CreateThreadRunnableAction.cons;
                thread = (RTPMediaThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.rtp.util.RTPMediaThread.class, this
                    })
                });
                thread.setName(threadname);
                cons = jdk12PriorityAction.cons;
                jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        thread, new Integer(MediaThread.getNetworkPriority())
                    })
                });
            }
            catch(Exception e) { }
        } else
        {
            thread = new RTPMediaThread(this, threadname);
            thread.useNetworkPriority();
        }
        thread.setDaemon(true);
        thread.start();
    }

    public void setVideoPriority()
    {
        thread.useVideoNetworkPriority();
    }

    public PacketSource getSource()
    {
        return source;
    }

    public PacketConsumer getConsumer()
    {
        return consumer;
    }

    public String getId()
    {
        if(thread == null)
        {
            System.err.println("the packetforwarders thread is null");
            return null;
        } else
        {
            return thread.getName();
        }
    }

    public void run()
    {
        InterruptedIOException e;
        if(closed || exception != null)
        {
            if(source != null)
                source.closeSource();
            return;
        }
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
            // Misplaced declaration of an exception variable
            catch(InterruptedIOException e)
            {
                jmfSecurity.permissionFailureNotification(128);
            }
        do
            try
            {
                do
                {
                    Packet p = source.receiveFrom();
                    if(checkForClose())
                        return;
                    if(p != null)
                        consumer.sendTo(p);
                } while(!checkForClose());
                return;
            }
            // Misplaced declaration of an exception variable
            catch(Packet p) { }
        while(!checkForClose());
        return;
        p;
        if(checkForClose())
            return;
        exception = p;
        break MISSING_BLOCK_LABEL_255;
        local;
        consumer.closeConsumer();
        JVM INSTR ret 3;
    }

    private boolean checkForClose()
    {
        if(closed && thread != null)
        {
            if(source != null)
                source.closeSource();
            return true;
        } else
        {
            return false;
        }
    }

    public void close()
    {
        closed = true;
        if(consumer != null)
            consumer.closeConsumer();
    }

    PacketSource source;
    PacketConsumer consumer;
    RTPMediaThread thread;
    boolean closed;
    private boolean paused;
    public IOException exception;
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
