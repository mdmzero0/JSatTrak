// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JDK12Security.java

package com.sun.media;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.*;

// Referenced classes of package com.sun.media:
//            JMFSecurity

public class JDK12Security
    implements JMFSecurity
{

    public static Permission getReadFilePermission(String name)
    {
        try
        {
            return (Permission)filepermcons.newInstance(new Object[] {
                name, "read"
            });
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static Permission getWriteFilePermission(String name)
    {
        try
        {
            return (Permission)filepermcons.newInstance(new Object[] {
                name, "read, write"
            });
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static void dummyMethod()
    {
    }

    private JDK12Security()
    {
    }

    public String getName()
    {
        return "jdk12";
    }

    public static Permission getThreadPermission()
    {
        return threadPermission;
    }

    public static Permission getThreadGroupPermission()
    {
        return threadGroupPermission;
    }

    public static Permission getConnectPermission()
    {
        return connectPermission;
    }

    public static Permission getMulticastPermission()
    {
        return multicastPermission;
    }

    public static Permission getReadAllFilesPermission()
    {
        return readAllFilesPermission;
    }

    public void requestPermission(Method m[], Class c[], Object args[][], int request)
        throws SecurityException
    {
        m[0] = dummyMethodRef;
        c[0] = cls;
        args[0] = null;
    }

    public void requestPermission(Method m[], Class c[], Object args[][], int request, String parameter)
        throws SecurityException
    {
        requestPermission(m, c, args, request);
    }

    public boolean isLinkPermissionEnabled()
    {
        return true;
    }

    public void permissionFailureNotification(int i)
    {
    }

    public void loadLibrary(final String name)
        throws UnsatisfiedLinkError
    {
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run()
            {
                System.loadLibrary(name);
                return null;
            }

        }
);
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

    public static final JMFSecurity security;
    private static Class cls;
    private static Method dummyMethodRef = null;
    private static Permission threadPermission = null;
    private static Permission threadGroupPermission = null;
    private static Permission connectPermission = null;
    private static Permission multicastPermission = null;
    private static Permission readAllFilesPermission = null;
    private static Constructor filepermcons;

    static 
    {
        cls = null;
        security = new JDK12Security();
        try
        {
            cls = security.getClass();
            dummyMethodRef = cls.getMethod("dummyMethod", new Class[0]);
            Class rtperm = Class.forName("java.lang.RuntimePermission");
            Class socketperm = Class.forName("java.net.SocketPermission");
            Class fileperm = Class.forName("java.io.FilePermission");
            filepermcons = fileperm.getConstructor(new Class[] {
                java.lang.String.class, java.lang.String.class
            });
            Constructor cons = rtperm.getConstructor(new Class[] {
                java.lang.String.class
            });
            threadPermission = (Permission)cons.newInstance(new Object[] {
                "modifyThread"
            });
            threadGroupPermission = (Permission)cons.newInstance(new Object[] {
                "modifyThreadGroup"
            });
            cons = socketperm.getConstructor(new Class[] {
                java.lang.String.class, java.lang.String.class
            });
            connectPermission = (Permission)cons.newInstance(new Object[] {
                "*", "connect"
            });
            multicastPermission = (Permission)cons.newInstance(new Object[] {
                "*", "accept,connect"
            });
        }
        catch(Exception e) { }
    }
}
