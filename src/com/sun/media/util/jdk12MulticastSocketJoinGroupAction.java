// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12MulticastSocketJoinGroupAction.java

package com.sun.media.util;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.PrivilegedAction;

public class jdk12MulticastSocketJoinGroupAction
    implements PrivilegedAction
{

    public jdk12MulticastSocketJoinGroupAction(MulticastSocket s, InetAddress a)
    {
        this.s = s;
        this.a = a;
    }

    public Object run()
    {
        try
        {
            s.joinGroup(a);
            return s;
        }
        catch(Throwable t)
        {
            return null;
        }
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

    public static Constructor cons;
    private MulticastSocket s;
    private InetAddress a;

    static 
    {
        try
        {
            cons = (com.sun.media.util.jdk12MulticastSocketJoinGroupAction.class).getConstructor(new Class[] {
                java.net.MulticastSocket.class, java.net.InetAddress.class
            });
        }
        catch(Throwable e) { }
    }
}
