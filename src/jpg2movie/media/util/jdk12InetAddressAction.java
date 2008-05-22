// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12InetAddressAction.java

package jpg2movie.media.util;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.security.PrivilegedAction;

public class jdk12InetAddressAction
    implements PrivilegedAction
{

    public jdk12InetAddressAction(InetAddress addr, String method, String arg)
    {
        this.addr = addr;
        this.method = method;
        this.arg = arg;
    }

    public Object run()
    {
        try
        {
            if(method.equals("getLocalHost"))
                return InetAddress.getLocalHost();
            if(method.equals("getAllByName"))
                return InetAddress.getAllByName(arg);
            if(method.equals("getByName"))
                return InetAddress.getByName(arg);
            if(method.equals("getHostName"))
                return addr.getHostName();
            else
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

    public static Constructor cons;
    private InetAddress addr;
    private String method;
    private String arg;

    static 
    {
        try
        {
            cons = (jpg2movie.media.util.jdk12InetAddressAction.class).getConstructor(new Class[] {
                java.net.InetAddress.class, java.lang.String.class, java.lang.String.class
            });
        }
        catch(Throwable e) { }
    }
}
