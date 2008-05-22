// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12RandomAccessFileAction.java

package com.sun.media.util;

import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

public class jdk12RandomAccessFileAction
    implements PrivilegedAction
{

    public jdk12RandomAccessFileAction(String name, String mode)
    {
        boolean rw = mode.equals("rw");
        if(!rw)
            mode = "r";
        this.mode = mode;
        this.name = name;
    }

    public Object run()
    {
        try
        {
            return new RandomAccessFile(name, mode);
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

    public static Constructor cons;
    private String name;
    private String mode;

    static 
    {
        try
        {
            cons = (com.sun.media.util.jdk12RandomAccessFileAction.class).getConstructor(new Class[] {
                java.lang.String.class, java.lang.String.class
            });
        }
        catch(Throwable e) { }
    }
}
