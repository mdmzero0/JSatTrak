// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12MakeDirectoryAction.java

package com.sun.media.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

public class jdk12MakeDirectoryAction
    implements PrivilegedAction
{

    public jdk12MakeDirectoryAction(File file)
    {
        this.file = file;
    }

    public Object run()
    {
        try
        {
            if(file != null)
            {
                if(file.exists() || file.mkdirs())
                    return TRUE;
                else
                    return FALSE;
            } else
            {
                return FALSE;
            }
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
    private File file;
    private static Boolean TRUE = new Boolean(true);
    private static Boolean FALSE = new Boolean(false);

    static 
    {
        try
        {
            cons = (com.sun.media.util.jdk12MakeDirectoryAction.class).getConstructor(new Class[] {
                java.io.File.class
            });
        }
        catch(Throwable e) { }
    }
}
