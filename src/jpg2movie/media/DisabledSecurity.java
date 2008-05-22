// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DisabledSecurity.java

package jpg2movie.media;

import java.lang.reflect.Method;

// Referenced classes of package com.sun.media:
//            JMFSecurity

public class DisabledSecurity
    implements JMFSecurity
{

    private DisabledSecurity()
    {
    }

    public String getName()
    {
        return "jmf-security-disabled";
    }

    public void requestPermission(Method m[], Class c[], Object args[][], int request)
        throws SecurityException
    {
        throw new SecurityException("DisabledSecurity : Cannot request permission");
    }

    public void requestPermission(Method m[], Class c[], Object args[][], int request, String parameter)
        throws SecurityException
    {
        requestPermission(m, c, args, request);
    }

    public boolean isLinkPermissionEnabled()
    {
        return false;
    }

    public void permissionFailureNotification(int i)
    {
    }

    public void loadLibrary(String name)
        throws UnsatisfiedLinkError
    {
        throw new UnsatisfiedLinkError("Unable to get link privilege to " + name);
    }

    public static JMFSecurity security = new DisabledSecurity();

}
