// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CreateWorkThreadAction.java

package com.sun.media;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

public class CreateWorkThreadAction
    implements PrivilegedAction
{

    public CreateWorkThreadAction(Class objclass, Class baseClass, Object arg)
    {
        try
        {
            this.objclass = objclass;
            this.baseClass = baseClass;
            this.arg = arg;
        }
        catch(Throwable e) { }
    }

    public Object run()
    {
        try
        {
            Constructor cons = objclass.getConstructor(new Class[] {
                baseClass
            });
            Object object = cons.newInstance(new Object[] {
                arg
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
    Class baseClass;
    Object arg;
    static Constructor cons;

    static 
    {
        try
        {
            cons = (com.sun.media.CreateWorkThreadAction.class).getConstructor(new Class[] {
                java.lang.Class.class, java.lang.Class.class, java.lang.Object.class
            });
        }
        catch(Throwable e) { }
    }
}
