// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12CreateThreadRunnableAction.java

package jpg2movie.media.util;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

public class jdk12CreateThreadRunnableAction
    implements PrivilegedAction
{

    public jdk12CreateThreadRunnableAction(Class threadclass, Runnable run, String name)
    {
        this.name = null;
        try
        {
            this.threadclass = threadclass;
            runnable = run;
            this.name = name;
        }
        catch(Throwable e) { }
    }

    public jdk12CreateThreadRunnableAction(Class threadclass, Runnable run)
    {
        this(threadclass, run, null);
    }

    public Object run()
    {
        try
        {
            Constructor cons = threadclass.getConstructor(new Class[] {
                java.lang.Runnable.class
            });
            Object object = cons.newInstance(new Object[] {
                runnable
            });
            if(name != null)
                ((Thread)object).setName(name);
            return object;
        }
        catch(Throwable e)
        {
            return null;
        }
    }

    private Class threadclass;
    private Runnable runnable;
    private String name;
    public static Constructor cons;
    public static Constructor conswithname;

    static 
    {
        try
        {
            cons = (jpg2movie.media.util.jdk12CreateThreadRunnableAction.class).getConstructor(new Class[] {
                java.lang.Class.class, java.lang.Runnable.class
            });
            conswithname = (jpg2movie.media.util.jdk12CreateThreadRunnableAction.class).getConstructor(new Class[] {
                java.lang.Class.class, java.lang.Runnable.class, java.lang.String.class
            });
        }
        catch(Throwable e) { }
    }
}
