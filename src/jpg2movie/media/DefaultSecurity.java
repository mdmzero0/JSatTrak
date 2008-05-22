// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DefaultSecurity.java

package jpg2movie.media;

import java.io.PrintStream;
import java.lang.reflect.Method;

// Referenced classes of package com.sun.media:
//            JMFSecurity

public class DefaultSecurity
    implements JMFSecurity
{

    public static void dummyMethod()
    {
    }

    private DefaultSecurity()
    {
    }

    public String getName()
    {
        return "default";
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
        return clsLoader == null;
    }

    public void permissionFailureNotification(int i)
    {
    }

    public void loadLibrary(String name)
        throws UnsatisfiedLinkError
    {
        if(clsLoader == null)
            System.loadLibrary(name);
        else
            throw new UnsatisfiedLinkError("Unable to get link privilege to " + name);
    }

    public static JMFSecurity security;
    private static ClassLoader clsLoader = null;
    private static Class cls;
    private static Method dummyMethodRef = null;

    static 
    {
        cls = null;
        security = new DefaultSecurity();
        try
        {
            cls = security.getClass();
            clsLoader = cls.getClassLoader();
            dummyMethodRef = cls.getMethod("dummyMethod", new Class[0]);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}
