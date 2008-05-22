// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JMFSecurityManager.java

package com.sun.media;

//import com.sun.media.util.Registry;
import java.io.PrintStream;

// Referenced classes of package com.sun.media:
//            JMFSecurity, DisabledSecurity, NetscapeSecurity, IESecurity, 
//            DefaultSecurity, JDK12Security

public class JMFSecurityManager
{

    public JMFSecurityManager()
    {
    }

    public static JMFSecurity getJMFSecurity()
        throws SecurityException
    {
        return security;
    }

    public static boolean isLinkPermissionEnabled()
    {
        if(security == null)
            return true;
        else
            return security.isLinkPermissionEnabled();
    }

    public static void loadLibrary(String name)
        throws UnsatisfiedLinkError
    {
        try
        {
            JMFSecurity s = getJMFSecurity();
            if(s != null)
                s.loadLibrary(name);
            else
                System.loadLibrary(name);
        }
        catch(Throwable t)
        {
            throw new UnsatisfiedLinkError("JMFSecurityManager: " + t);
        }
    }

    public static synchronized void disableSecurityFeatures()
    {
        security = DisabledSecurity.security;
        count++;
    }

    public static synchronized void enableSecurityFeatures()
    {
        count--;
        if(count <= 0)
            security = enabledSecurity;
    }

//    public static void checkCapture()
//    {
//        if(security == null)
//            return;
//        Object captureFromApplets = Registry.get("secure.allowCaptureFromApplets");
//        if(captureFromApplets == null || !(captureFromApplets instanceof Boolean) || !((Boolean)captureFromApplets).booleanValue())
//            throw new RuntimeException("No permission to capture from applets");
//        else
//            return;
//    }
//
//    public static void checkFileSave()
//    {
//        if(security == null)
//            return;
//        Object saveFromApplets = Registry.get("secure.allowSaveFileFromApplets");
//        if(saveFromApplets == null || !(saveFromApplets instanceof Boolean) || !((Boolean)saveFromApplets).booleanValue())
//            throw new RuntimeException("No permission to write files from applets");
//        else
//            return;
//    }

    public static boolean isJDK12()
    {
        return jdk12;
    }

    private static JMFSecurity security;
    private static JMFSecurity enabledSecurity = null;
    private static SecurityManager securityManager;
    private static int count = 0;
    public static final boolean DEBUG = false;
    private static boolean jdk12;
    private static final String STR_NOPERMCAPTURE = "No permission to capture from applets";
    private static final String STR_NOPERMFILE = "No permission to write files from applets";

    static 
    {
        security = null;
        jdk12 = false;
        securityManager = System.getSecurityManager();
        boolean jdk11 = false;
        boolean msjvm = false;
        try
        {
            String javaVersion = System.getProperty("java.version");
            if(!javaVersion.equals(""))
                if(javaVersion.startsWith("1.1"))
                {
                    jdk11 = true;
                } else
                {
                    char c = javaVersion.charAt(0);
                    if(c >= '0' && c <= '9' && javaVersion.compareTo("1.2") >= 0)
                        jdk12 = true;
                }
            String javaVendor = System.getProperty("java.vendor", "Sun").toLowerCase();
            if(javaVendor.indexOf("icrosoft") > 0)
                msjvm = true;
        }
        catch(Throwable t)
        {
            System.out.println(t);
        }
        if(securityManager != null)
            if(securityManager.toString().indexOf("netscape") != -1)
                security = NetscapeSecurity.security;
            else
            if(securityManager.toString().indexOf("com.ms.security") != -1 || msjvm)
                security = IESecurity.security;
            else
            if(securityManager.toString().indexOf("sun.applet.AppletSecurity") != -1 || securityManager.toString().indexOf("sun.plugin.ActivatorSecurityManager") != -1)
            {
                if(jdk11)
                    security = DefaultSecurity.security;
                if(jdk12)
                    security = JDK12Security.security;
            } else
            if(securityManager.toString().indexOf("java.lang.SecurityManager") != -1)
            {
                if(jdk12)
                    security = JDK12Security.security;
            } else
            if(jdk12)
                security = JDK12Security.security;
            else
                security = DefaultSecurity.security;
        enabledSecurity = security;
    }
}
