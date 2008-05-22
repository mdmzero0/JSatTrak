/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   JDK12Security.java

package jpg2movie.media;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.*;

// Referenced classes of package com.sun.media:
//            JMFSecurity

public class JDK12Security
    implements JMFSecurity
{

    public static Permission getReadFilePermission(String name)
    {
        try
        {
            return (Permission)filepermcons.newInstance(new Object[] {
                name, "read"
            });
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static Permission getWriteFilePermission(String name)
    {
        try
        {
            return (Permission)filepermcons.newInstance(new Object[] {
                name, "read, write"
            });
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static void dummyMethod()
    {
    }

    private JDK12Security()
    {
    }

    public String getName()
    {
        return "jdk12";
    }

    public static Permission getThreadPermission()
    {
        return threadPermission;
    }

    public static Permission getThreadGroupPermission()
    {
        return threadGroupPermission;
    }

    public static Permission getConnectPermission()
    {
        return connectPermission;
    }

    public static Permission getMulticastPermission()
    {
        return multicastPermission;
    }

    public static Permission getReadAllFilesPermission()
    {
        return readAllFilesPermission;
    }

    public void requestPermission(Method m[], Class c[], Object args[][], int request)
        throws SecurityException
    {
        m[0] = dummyMethodRef;
        c[0] = cls;
        args[0] = null;
    }

    public void requestPermission(Method m[], Class c[], Object args[][], int request, String parameter)
        throws SecurityException
    {
        requestPermission(m, c, args, request);
    }

    public boolean isLinkPermissionEnabled()
    {
        return true;
    }

    public void permissionFailureNotification(int i)
    {
    }

    public void loadLibrary(final String name)
        throws UnsatisfiedLinkError
    {
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run()
            {
                System.loadLibrary(name);
                return null;
            }

        }
);
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

    public static final JMFSecurity security;
    private static Class cls;
    private static Method dummyMethodRef = null;
    private static Permission threadPermission = null;
    private static Permission threadGroupPermission = null;
    private static Permission connectPermission = null;
    private static Permission multicastPermission = null;
    private static Permission readAllFilesPermission = null;
    private static Constructor filepermcons;

    static 
    {
        cls = null;
        security = new JDK12Security();
        try
        {
            cls = security.getClass();
            dummyMethodRef = cls.getMethod("dummyMethod", new Class[0]);
            Class rtperm = Class.forName("java.lang.RuntimePermission");
            Class socketperm = Class.forName("java.net.SocketPermission");
            Class fileperm = Class.forName("java.io.FilePermission");
            filepermcons = fileperm.getConstructor(new Class[] {
                java.lang.String.class, java.lang.String.class
            });
            Constructor cons = rtperm.getConstructor(new Class[] {
                java.lang.String.class
            });
            threadPermission = (Permission)cons.newInstance(new Object[] {
                "modifyThread"
            });
            threadGroupPermission = (Permission)cons.newInstance(new Object[] {
                "modifyThreadGroup"
            });
            cons = socketperm.getConstructor(new Class[] {
                java.lang.String.class, java.lang.String.class
            });
            connectPermission = (Permission)cons.newInstance(new Object[] {
                "*", "connect"
            });
            multicastPermission = (Permission)cons.newInstance(new Object[] {
                "*", "accept,connect"
            });
        }
        catch(Exception e) { }
    }
}
