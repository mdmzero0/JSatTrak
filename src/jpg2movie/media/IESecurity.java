/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   IESecurity.java

package jpg2movie.media;


import java.lang.reflect.Method;

// Referenced classes of package com.sun.media:
//            JMFSecurity

public class IESecurity
    implements JMFSecurity
{

    private IESecurity()
    {
    }

    public String getName()
    {
        return "internetexplorer";
    }

    public static void dummyMethod()
    {
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
        return jview;
    }

    public void permissionFailureNotification(int i)
    {
    }

    public void loadLibrary(String name)
        throws UnsatisfiedLinkError
    {
        try
        {
            try
            {
                //if(!jview)
                    //PolicyEngine.assertPermission(PermissionID.SYSTEM);
            }
            catch(Throwable t) { }
            System.loadLibrary(name);
        }
        catch(Exception e)
        {
            throw new UnsatisfiedLinkError("Unable to get link privilege to " + name);
        }
        catch(Error e)
        {
            throw new UnsatisfiedLinkError("Unable to get link privilege to " + name);
        }
    }

    public static JMFSecurity security;
    public static boolean jview = false;
    private static Class cls;
    private static Method dummyMethodRef = null;
    public static final boolean DEBUG = false;

    static 
    {
        cls = null;
        security = new IESecurity();
        cls = security.getClass();
        try
        {
            dummyMethodRef = cls.getMethod("dummyMethod", new Class[0]);
        }
        catch(Exception e) { }
    }
}
