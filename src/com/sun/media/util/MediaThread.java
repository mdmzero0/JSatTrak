// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaThread.java

package com.sun.media.util;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.*;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

// Referenced classes of package com.sun.media.util:
//            jdk12Action, jdk12

public class MediaThread extends Thread
{

    private static ThreadGroup getRootThreadGroup()
    {
        ThreadGroup current = null;
        try
        {
            current = Thread.currentThread().getThreadGroup();
            ThreadGroup g;
            for(g = current; g.getParent() != null; g = g.getParent());
            return g;
        }
        catch(Exception e)
        {
            return null;
        }
        catch(Error e)
        {
            return null;
        }
    }

    public MediaThread()
    {
        this("JMF thread");
    }

    public MediaThread(String name)
    {
        super(threadGroup, name);
    }

    public MediaThread(Runnable r)
    {
        this(r, "JMF thread");
    }

    public MediaThread(Runnable r, String name)
    {
        super(threadGroup, r, name);
    }

    public void useControlPriority()
    {
        usePriority(controlPriority);
    }

    public void useAudioPriority()
    {
        usePriority(audioPriority);
    }

    public void useVideoPriority()
    {
        usePriority(videoPriority);
    }

    public void useNetworkPriority()
    {
        usePriority(networkPriority);
    }

    public void useVideoNetworkPriority()
    {
        usePriority(videoNetworkPriority);
    }

    public static int getControlPriority()
    {
        return controlPriority;
    }

    public static int getAudioPriority()
    {
        return audioPriority;
    }

    public static int getVideoPriority()
    {
        return videoPriority;
    }

    public static int getNetworkPriority()
    {
        return networkPriority;
    }

    public static int getVideoNetworkPriority()
    {
        return videoNetworkPriority;
    }

    private void usePriority(int priority)
    {
        try
        {
            setPriority(priority);
        }
        catch(Throwable t) { }
    }

    private void checkPriority(String name, int ask, boolean priv, int got)
    {
        if(ask != got)
            System.out.println("MediaThread: " + name + " privilege? " + priv + "  ask pri: " + ask + " got pri:  " + got);
    }

    static Class _mthclass$(String x0)
    {
        try
        {
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x1)
        {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    private static ThreadGroup threadGroup;
    static boolean securityPrivilege;
    private static final boolean debug = false;
    private static int controlPriority = 9;
    private static int audioPriority;
    private static int videoPriority = 3;
    private static int networkPriority;
    private static int videoNetworkPriority;
    private static int defaultMaxPriority;

    static 
    {
        securityPrivilege = true;
        audioPriority = 5;
        networkPriority = audioPriority + 1;
        videoNetworkPriority = networkPriority - 1;
        defaultMaxPriority = 4;
        JMFSecurity jmfSecurity = null;
        Method m[] = new Method[1];
        Class cl[] = new Class[1];
        Object args[][] = new Object[1][0];
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            if(jmfSecurity != null)
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    boolean haveBoth = true;
                    defaultMaxPriority = Thread.currentThread().getPriority();
                    try
                    {
                        jmfSecurity.requestPermission(m, cl, args, 16);
                        m[0].invoke(cl[0], args[0]);
                    }
                    catch(Throwable t)
                    {
                        jmfSecurity.permissionFailureNotification(16);
                        haveBoth = false;
                    }
                    if(haveBoth)
                        defaultMaxPriority = Thread.currentThread().getThreadGroup().getMaxPriority();
                    try
                    {
                        jmfSecurity.requestPermission(m, cl, args, 32);
                        m[0].invoke(cl[0], args[0]);
                    }
                    catch(Throwable t)
                    {
                        jmfSecurity.permissionFailureNotification(32);
                        haveBoth = false;
                    }
                    if(!haveBoth)
                        throw new Exception("No thread and or threadgroup permission");
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                    PolicyEngine.assertPermission(PermissionID.THREAD);
                else
                if(jmfSecurity.getName().startsWith("jdk12"))
                {
                    Constructor cons = jdk12Action.getCheckPermissionAction();
                    defaultMaxPriority = Thread.currentThread().getPriority();
                    jdk12.doPrivContextM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            JDK12Security.getThreadPermission()
                        }), jdk12.getContextM.invoke(null, null)
                    });
                    defaultMaxPriority = Thread.currentThread().getThreadGroup().getMaxPriority();
                    jdk12.doPrivContextM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            JDK12Security.getThreadGroupPermission()
                        }), jdk12.getContextM.invoke(null, null)
                    });
                } else
                if(jmfSecurity.getName().startsWith("default") && (com.sun.media.util.MediaThread.class).getClassLoader() != null)
                    throw new SecurityException();
        }
        catch(Throwable e)
        {
            securityPrivilege = false;
            controlPriority = defaultMaxPriority;
            audioPriority = defaultMaxPriority;
            videoPriority = defaultMaxPriority - 1;
            networkPriority = defaultMaxPriority;
            videoNetworkPriority = defaultMaxPriority;
        }
        if(securityPrivilege)
            threadGroup = getRootThreadGroup();
        else
            threadGroup = null;
    }
}
