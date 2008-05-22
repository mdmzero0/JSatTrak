// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CreateSourceThreadAction.java

package com.sun.media;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

// Referenced classes of package com.sun.media:
//            BasicSourceModule

public class CreateSourceThreadAction
    implements PrivilegedAction
{

    public CreateSourceThreadAction(Class sourceThreadClass, BasicSourceModule bsm, Object myoc, int i)
    {
        try
        {
            this.sourceThreadClass = sourceThreadClass;
            this.bsm = bsm;
            this.myoc = myoc;
            this.i = i;
        }
        catch(Throwable e) { }
    }

    public Object run()
    {
        try
        {
            Constructor cons = sourceThreadClass.getConstructor(new Class[] {
                com.sun.media.BasicSourceModule.class, myoc.getClass(), Integer.TYPE
            });
            Object object = cons.newInstance(new Object[] {
                bsm, myoc, new Integer(i)
            });
            return object;
        }
        catch(Throwable e)
        {
            return null;
        }
    }

    private Class sourceThreadClass;
    private BasicSourceModule bsm;
    private Object myoc;
    private int i;
    static Constructor cons;

    static 
    {
        try
        {
            cons = (com.sun.media.CreateSourceThreadAction.class).getConstructor(new Class[] {
                java.lang.Class.class, com.sun.media.BasicSourceModule.class, java.lang.Object.class, Integer.TYPE
            });
        }
        catch(Throwable e) { }
    }
}
