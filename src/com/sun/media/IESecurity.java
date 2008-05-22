// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   IESecurity.java

package com.sun.media;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import java.lang.reflect.Method;

// Referenced classes of package com.sun.media:
//            JMFSecurity

public class IESecurity
    implements JMFSecurity
{

    private IESecurity()
    {
    }

    public String getName()
    {
        return "internetexplorer";
    }

    public static void dummyMethod()
    {
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
        return jview;
    }

    public void permissionFailureNotification(int i)
    {
    }

    public void loadLibrary(String name)
        throws UnsatisfiedLinkError
    {
        try
        {
            try
            {
                if(!jview)
                    PolicyEngine.assertPermission(PermissionID.SYSTEM);
            }
            catch(Throwable t) { }
            System.loadLibrary(name);
        }
        catch(Exception e)
        {
            throw new UnsatisfiedLinkError("Unable to get link privilege to " + name);
        }
        catch(Error e)
        {
            throw new UnsatisfiedLinkError("Unable to get link privilege to " + name);
        }
    }

    public static JMFSecurity security;
    public static boolean jview = false;
    private static Class cls;
    private static Method dummyMethodRef = null;
    public static final boolean DEBUG = false;

    static 
    {
        cls = null;
        security = new IESecurity();
        cls = security.getClass();
        try
        {
            dummyMethodRef = cls.getMethod("dummyMethod", new Class[0]);
        }
        catch(Exception e) { }
    }
}
