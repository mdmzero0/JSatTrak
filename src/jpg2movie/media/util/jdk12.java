// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12.java

package jpg2movie.media.util;

import java.lang.reflect.Method;

public class jdk12
{

    public jdk12()
    {
    }

    public static Class ac;
    public static Class accontextC;
    public static Class permissionC;
    public static Class privActionC;
    public static Method checkPermissionM;
    public static Method doPrivM;
    public static Method doPrivContextM;
    public static Method getContextM;

    static 
    {
        try
        {
            ac = Class.forName("java.security.AccessController");
            accontextC = Class.forName("java.security.AccessControlContext");
            permissionC = Class.forName("java.security.Permission");
            privActionC = Class.forName("java.security.PrivilegedAction");
            checkPermissionM = ac.getMethod("checkPermission", new Class[] {
                permissionC
            });
            doPrivM = ac.getMethod("doPrivileged", new Class[] {
                privActionC
            });
            getContextM = ac.getMethod("getContext", null);
            doPrivContextM = ac.getMethod("doPrivileged", new Class[] {
                privActionC, accontextC
            });
        }
        catch(Throwable t) { }
    }
}
