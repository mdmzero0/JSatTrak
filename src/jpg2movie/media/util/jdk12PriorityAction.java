// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12PriorityAction.java

package jpg2movie.media.util;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

public class jdk12PriorityAction
    implements PrivilegedAction
{

    public jdk12PriorityAction(Thread t, int priority)
    {
        this.t = t;
        this.priority = priority;
    }

    public Object run()
    {
        try
        {
            this.t.setPriority(priority);
            return null;
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

    private Thread t;
    private int priority;
    public static Constructor cons;

    static 
    {
        try
        {
            cons = (jpg2movie.media.util.jdk12PriorityAction.class).getConstructor(new Class[] {
                java.lang.Thread.class, Integer.TYPE
            });
        }
        catch(Throwable e) { }
    }
}
