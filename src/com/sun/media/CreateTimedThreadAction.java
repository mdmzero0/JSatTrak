// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CreateTimedThreadAction.java

package com.sun.media;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

public class CreateTimedThreadAction
    implements PrivilegedAction
{

    public CreateTimedThreadAction(Class objclass, Class baseClass, Object arg1, long nanoseconds)
    {
        try
        {
            this.objclass = objclass;
            this.baseClass = baseClass;
            this.arg1 = arg1;
            this.nanoseconds = nanoseconds;
        }
        catch(Throwable e) { }
    }

    public Object run()
    {
        try
        {
            Constructor cons = objclass.getConstructor(new Class[] {
                baseClass, Long.TYPE
            });
            Object object = cons.newInstance(new Object[] {
                arg1, new Long(nanoseconds)
            });
            return object;
        }
        catch(Throwable e)
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

    private Class objclass;
    private Class baseClass;
    private Object arg1;
    private long nanoseconds;
    static Constructor cons;

    static 
    {
        try
        {
            cons = (com.sun.media.CreateTimedThreadAction.class).getConstructor(new Class[] {
                java.lang.Class.class, java.lang.Class.class, java.lang.Object.class, Long.TYPE
            });
        }
        catch(Throwable e) { }
    }
}
