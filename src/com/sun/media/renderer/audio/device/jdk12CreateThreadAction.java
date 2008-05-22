// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12CreateThreadAction.java

package com.sun.media.renderer.audio.device;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

public class jdk12CreateThreadAction
    implements PrivilegedAction
{

    public jdk12CreateThreadAction(Class threadclass, String name)
    {
        this.name = null;
        try
        {
            this.threadclass = threadclass;
            this.name = name;
        }
        catch(Throwable e) { }
    }

    public jdk12CreateThreadAction(Class threadclass)
    {
        this(threadclass, null);
    }

    public Object run()
    {
        try
        {
            Object object = threadclass.newInstance();
            if(name != null)
                ((Thread)object).setName(name);
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

    private Class threadclass;
    private String name;
    public static Constructor cons;
    public static Constructor conswithname;

    static 
    {
        try
        {
            cons = (com.sun.media.renderer.audio.device.jdk12CreateThreadAction.class).getConstructor(new Class[] {
                java.lang.Class.class
            });
            conswithname = (com.sun.media.renderer.audio.device.jdk12CreateThreadAction.class).getConstructor(new Class[] {
                java.lang.Class.class, java.lang.String.class
            });
        }
        catch(Throwable e) { }
    }
}
