// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Log.java

package com.sun.media;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.util.Registry;
import java.io.*;
import java.lang.reflect.Method;

// Referenced classes of package com.sun.media:
//            JMFSecurity, BasicPlayer, JMFSecurityManager

public class Log
{

    public Log()
    {
    }

    private static synchronized boolean requestPerm()
    {
        try
        {
            if(!ieSec)
            {
                permission = "write file";
                permissionid = 4;
                jmfSecurity.requestPermission(m, cl, args, 4);
                m[0].invoke(cl[0], args[0]);
            } else
            {
                PolicyEngine.checkPermission(PermissionID.FILEIO);
                PolicyEngine.assertPermission(PermissionID.FILEIO);
            }
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }

    private static synchronized void writeHeader()
    {
        if(jmfSecurity != null && !requestPerm())
            return;
        write("#\n# JMF " + BasicPlayer.VERSION + "\n#\n");
        String os = null;
        String osver = null;
        String osarch = null;
        String java = null;
        String jver = null;
        try
        {
            os = System.getProperty("os.name");
            osarch = System.getProperty("os.arch");
            osver = System.getProperty("os.version");
            java = System.getProperty("java.vendor");
            jver = System.getProperty("java.version");
        }
        catch(Throwable e)
        {
            return;
        }
        if(os != null)
            comment("Platform: " + os + ", " + osarch + ", " + osver);
        if(java != null)
            comment("Java VM: " + java + ", " + jver);
        write("");
    }

    public static synchronized void comment(Object str)
    {
        if(isEnabled)
        {
            if(jmfSecurity != null && !requestPerm())
                return;
            try
            {
                log.writeBytes("## " + str + "\n");
            }
            catch(IOException e) { }
        }
    }

    public static synchronized void warning(Object str)
    {
        if(isEnabled)
        {
            if(jmfSecurity != null && !requestPerm())
                return;
            try
            {
                log.writeBytes("!! " + str + "\n");
            }
            catch(IOException e) { }
        }
    }

    public static synchronized void profile(Object str)
    {
        if(isEnabled)
        {
            if(jmfSecurity != null && !requestPerm())
                return;
            try
            {
                log.writeBytes("$$ " + str + "\n");
            }
            catch(IOException e) { }
        }
    }

    public static synchronized void error(Object str)
    {
        if(isEnabled)
        {
            if(jmfSecurity != null && !requestPerm())
                return;
            if(!errorWarned)
            {
                System.err.println("An error has occurred.  Check jmf.log for details.");
                errorWarned = true;
            }
            try
            {
                log.writeBytes("XX " + str + "\n");
            }
            catch(IOException e) { }
        } else
        {
            System.err.println(str);
        }
    }

    public static synchronized void dumpStack(Throwable e)
    {
        if(isEnabled)
        {
            if(jmfSecurity != null && !requestPerm())
                return;
            e.printStackTrace(new PrintWriter(log, true));
            write("");
        } else
        {
            e.printStackTrace();
        }
    }

    public static synchronized void write(Object str)
    {
        if(isEnabled)
        {
            if(jmfSecurity != null && !requestPerm())
                return;
            try
            {
                for(int i = indent; i > 0; i--)
                    log.writeBytes("    ");

                log.writeBytes(str + "\n");
            }
            catch(IOException e) { }
        }
    }

    public static synchronized void setIndent(int i)
    {
        indent = i;
    }

    public static synchronized void incrIndent()
    {
        indent++;
    }

    public static synchronized void decrIndent()
    {
        indent--;
    }

    public static int getIndent()
    {
        return indent;
    }

    public static boolean isEnabled;
    private static DataOutputStream log;
    private static String fileName;
    private static int indent = 0;
    private static JMFSecurity jmfSecurity;
    private static Method m[];
    private static Class cl[];
    private static Object args[][];
    private static boolean ieSec = false;
    private static String permission = null;
    private static int permissionid = 0;
    static boolean errorWarned = false;

    static 
    {
        isEnabled = true;
        log = null;
        fileName = "jmf.log";
        jmfSecurity = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        synchronized(fileName)
        {
            if(isEnabled && log == null)
            {
                Object llog = Registry.get("allowLogging");
                if(llog != null && (llog instanceof Boolean) && !((Boolean)llog).booleanValue())
                    isEnabled = false;
                if(isEnabled)
                    try
                    {
                        jmfSecurity = JMFSecurityManager.getJMFSecurity();
                        if(jmfSecurity != null)
                            if(jmfSecurity.getName().startsWith("jmf-security"))
                            {
                                permission = "write file";
                                permissionid = 4;
                                jmfSecurity.requestPermission(m, cl, args, 4);
                                m[0].invoke(cl[0], args[0]);
                                permission = "delete file";
                                permissionid = 8;
                                jmfSecurity.requestPermission(m, cl, args, 8);
                                m[0].invoke(cl[0], args[0]);
                                permission = "read system property";
                                permissionid = 1;
                                jmfSecurity.requestPermission(m, cl, args, 1);
                                m[0].invoke(cl[0], args[0]);
                            } else
                            if(jmfSecurity.getName().startsWith("internet"))
                            {
                                PolicyEngine.checkPermission(PermissionID.FILEIO);
                                PolicyEngine.assertPermission(PermissionID.FILEIO);
                                ieSec = true;
                            }
                    }
                    catch(Exception e)
                    {
                        isEnabled = false;
                    }
                if(isEnabled)
                {
                    isEnabled = false;
                    try
                    {
                        Object ldir = Registry.get("secure.logDir");
                        String dir;
                        if(ldir != null && (ldir instanceof String) && !"".equals(ldir))
                            dir = (String)ldir;
                        else
                            dir = System.getProperty("user.dir");
                        String file = dir + File.separator + fileName;
                        log = new DataOutputStream(new FileOutputStream(file));
                        if(log != null)
                        {
                            System.err.println("Open log file: " + file);
                            isEnabled = true;
                            writeHeader();
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println("Failed to open log file.");
                    }
                }
            }
        }
    }
}
