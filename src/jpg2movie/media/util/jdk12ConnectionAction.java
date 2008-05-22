// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12ConnectionAction.java

package jpg2movie.media.util;

import java.lang.reflect.Constructor;
import java.net.URLConnection;
import java.security.PrivilegedAction;

public class jdk12ConnectionAction
    implements PrivilegedAction
{

    public jdk12ConnectionAction(URLConnection urlC)
    {
        try
        {
            this.urlC = urlC;
        }
        catch(Throwable e) { }
    }

    public Object run()
    {
        try
        {
            return urlC.getInputStream();
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
    private URLConnection urlC;

    static 
    {
        try
        {
            cons = (jpg2movie.media.util.jdk12ConnectionAction.class).getConstructor(new Class[] {
                java.net.URLConnection.class
            });
        }
        catch(Throwable e) { }
    }
}
