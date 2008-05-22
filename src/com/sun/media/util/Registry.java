// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Registry.java

package com.sun.media.util;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import javax.media.Format;

// Referenced classes of package com.sun.media.util:
//            jdk12ReadFileAction, jdk12, jdk12PropertyAction

public class Registry
{

    public Registry()
    {
    }

    private static String getJMFDir()
    {
        try
        {
            String pointerFile;
            if(File.separator.equals("/"))
            {
                if(userhome == null)
                    return null;
                pointerFile = userhome;
            } else
            {
                JMFSecurityManager.loadLibrary("jmutil");
                pointerFile = nGetUserHome() + File.separator + "java";
            }
            pointerFile = pointerFile + File.separator + ".jmfdir";
            File f = new File(pointerFile);
            FileInputStream fis = getRegistryStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String dir = br.readLine();
            br.close();
            return dir;
        }
        catch(Throwable t)
        {
            return null;
        }
    }

    private static native String nGetUserHome();

    public static final synchronized boolean set(String key, Object value)
    {
        if(key != null && value != null)
        {
            if(jmfSecurity != null && key.indexOf("secure.") == 0)
            {
                return false;
            } else
            {
                hash.put(key, value);
                return true;
            }
        } else
        {
            return false;
        }
    }

    public static final synchronized Object get(String key)
    {
        if(key != null)
            return hash.get(key);
        else
            return null;
    }

    public static final synchronized boolean remove(String key)
    {
        if(key != null && hash.containsKey(key))
        {
            hash.remove(key);
            return true;
        } else
        {
            return false;
        }
    }

    public static final synchronized void removeGroup(String keyStart)
    {
        Vector keys = new Vector();
        if(keyStart != null)
        {
            for(Enumeration e = hash.keys(); e.hasMoreElements();)
            {
                String key = (String)e.nextElement();
                if(key.startsWith(keyStart))
                    keys.addElement(key);
            }

        }
        for(int i = 0; i < keys.size(); i++)
            hash.remove(keys.elementAt(i));

    }

    public static final synchronized boolean commit()
        throws IOException
    {
        if(jmfSecurity != null)
            throw new SecurityException("commit: Permission denied");
        if(filename == null)
            throw new IOException("Can't find registry file");
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        int tableSize = hash.size();
        oos.writeInt(tableSize);
        oos.writeInt(200);
        for(Enumeration e = hash.keys(); e.hasMoreElements(); oos.flush())
        {
            String key = (String)e.nextElement();
            Object value = hash.get(key);
            oos.writeUTF(key);
            oos.writeObject(value);
        }

        oos.close();
        return true;
    }

    private static final synchronized InputStream findJMFPropertiesFile()
    {
        StringTokenizer tokens = new StringTokenizer(classpath, File.pathSeparator);
        String strJMF = "jmf.properties";
        File file = null;
        InputStream ris = null;
        while(tokens.hasMoreTokens()) 
        {
            String dir = tokens.nextToken();
            String caps = dir.toUpperCase();
            try
            {
                if(caps.indexOf(".ZIP") > 0 || caps.indexOf(".JAR") > 0)
                {
                    int sep = dir.lastIndexOf(File.separator);
                    if(sep == -1 && !File.separator.equals("/"))
                        sep = dir.lastIndexOf("/");
                    if(sep == -1)
                    {
                        sep = dir.lastIndexOf(":");
                        if(sep == -1)
                            dir = strJMF;
                        else
                            dir = dir.substring(0, sep) + ":" + strJMF;
                    } else
                    {
                        dir = dir.substring(0, sep) + File.separator + strJMF;
                    }
                } else
                {
                    dir = dir + File.separator + strJMF;
                }
            }
            catch(Exception e)
            {
                dir = dir + File.separator + strJMF;
            }
            try
            {
                file = new File(dir);
                ris = getRegistryStream(file);
                if(ris != null)
                {
                    filename = dir;
                    break;
                }
            }
            catch(Throwable t)
            {
                filename = null;
                return null;
            }
        }
        try
        {
            if(filename == null || file == null)
                return null;
        }
        catch(Throwable t)
        {
            return null;
        }
        return ris;
    }

    private static FileInputStream getRegistryStream(File file)
        throws IOException
    {
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12ReadFileAction.cons;
                return (FileInputStream)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        file.getPath()
                    })
                });
            }
            if(!file.exists())
                return null;
            else
                return new FileInputStream(file.getPath());
        }
        catch(Throwable t)
        {
            return null;
        }
    }

    private static final synchronized boolean readRegistry(InputStream ris)
    {
        if(realReadRegistry(ris))
            return true;
        byte data[];
        try
        {
            Class c = Class.forName("com.sun.media.util.RegistryLib");
            data = (byte[])c.getMethod("getData", null).invoke(c, null);
        }
        catch(Exception e)
        {
            return false;
        }
        if(data == null)
        {
            return false;
        } else
        {
            InputStream def_ris = new ByteArrayInputStream(data);
            return realReadRegistry(def_ris);
        }
    }

    private static final boolean realReadRegistry(InputStream ris)
    {
        if(ris == null)
            return false;
        try
        {
            ObjectInputStream ois = new ObjectInputStream(ris);
            int tableSize = ois.readInt();
            int version = ois.readInt();
            if(version > 200)
                System.err.println("Version number mismatch.\nThere could be errors in reading the registry");
            hash = new Hashtable();
            for(int i = 0; i < tableSize; i++)
            {
                String key = ois.readUTF();
                boolean failed = false;
                try
                {
                    Object value = ois.readObject();
                    hash.put(key, value);
                }
                catch(ClassNotFoundException cnfe)
                {
                    failed = true;
                }
                catch(OptionalDataException ode)
                {
                    failed = true;
                }
            }

            ois.close();
            ris.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException in readRegistry: " + ioe);
            return false;
        }
        catch(Throwable t)
        {
            return false;
        }
        return true;
    }

    private static boolean checkIfJDK12()
    {
        if(jdkInit)
            return forName3ArgsM != null;
        jdkInit = true;
        try
        {
            forName3ArgsM = (java.lang.Class.class).getMethod("forName", new Class[] {
                java.lang.String.class, Boolean.TYPE, java.lang.ClassLoader.class
            });
            getSystemClassLoaderM = (java.lang.ClassLoader.class).getMethod("getSystemClassLoader", null);
            systemClassLoader = (ClassLoader)getSystemClassLoaderM.invoke(java.lang.ClassLoader.class, null);
            getContextClassLoaderM = (java.lang.Thread.class).getMethod("getContextClassLoader", null);
            return true;
        }
        catch(Throwable t)
        {
            forName3ArgsM = null;
        }
        return false;
    }

    static Class getClassForName(String className)
        throws ClassNotFoundException
    {
        try
        {
            return Class.forName(className);
        }
        catch(Exception e)
        {
            if(!checkIfJDK12())
                throw new ClassNotFoundException(e.getMessage());
        }
        catch(Error e)
        {
            if(!checkIfJDK12())
                throw e;
        }
        try
        {
            return (Class)forName3ArgsM.invoke(java.lang.Class.class, new Object[] {
                className, new Boolean(true), systemClassLoader
            });
        }
        catch(Throwable e) { }
        try
        {
            ClassLoader contextClassLoader = (ClassLoader)getContextClassLoaderM.invoke(Thread.currentThread(), null);
            return (Class)forName3ArgsM.invoke(java.lang.Class.class, new Object[] {
                className, new Boolean(true), contextClassLoader
            });
        }
        catch(Exception e)
        {
            throw new ClassNotFoundException(e.getMessage());
        }
        catch(Error e)
        {
            throw e;
        }
    }

    public static void main(String args[])
    {
        Format f1 = new Format("crap", java.lang.Integer.class);
        Object bad = new Integer(5);
        try
        {
            Class vfclass = Class.forName("javax.media.format.H261Format");
            bad = vfclass.newInstance();
        }
        catch(ClassNotFoundException cnfe)
        {
            System.err.println("H261Format not found in main()");
        }
        catch(InstantiationException ie) { }
        catch(IllegalAccessException iae) { }
        Format f2 = new Format("crap2", java.lang.Boolean.class);
        set("obj1", f1);
        set("obj2", bad);
        set("obj3", f2);
        try
        {
            commit();
        }
        catch(IOException ioe)
        {
            System.err.println("main: IO error in commit " + ioe);
        }
    }

    private static Hashtable hash = null;
    private static String filename = null;
    private static final int versionNumber = 200;
    private static boolean securityPrivelege;
    private static JMFSecurity jmfSecurity;
    private static Method m[];
    private static Class cl[];
    private static Object args[][];
    private static final String CLASSPATH = "java.class.path";
    private static String userhome = null;
    private static String classpath;
    private static boolean jdkInit = false;
    private static Method forName3ArgsM;
    private static Method getSystemClassLoaderM;
    private static ClassLoader systemClassLoader;
    private static Method getContextClassLoaderM;

    static 
    {
        securityPrivelege = false;
        jmfSecurity = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        classpath = null;
        hash = new Hashtable();
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "read property";
                    jmfSecurity.requestPermission(m, cl, args, 1);
                    m[0].invoke(cl[0], args[0]);
                    permission = "read file";
                    jmfSecurity.requestPermission(m, cl, args, 2);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.PROPERTY);
                    PolicyEngine.checkPermission(PermissionID.FILEIO);
                    PolicyEngine.assertPermission(PermissionID.PROPERTY);
                    PolicyEngine.assertPermission(PermissionID.FILEIO);
                }
            }
            catch(Throwable e)
            {
                securityPrivelege = false;
            }
        }
        if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            try
            {
                Constructor cons = jdk12PropertyAction.cons;
                classpath = (String)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        "java.class.path"
                    })
                });
                userhome = (String)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        "user.home"
                    })
                });
            }
            catch(Throwable e)
            {
                securityPrivelege = false;
            }
        else
            try
            {
                if(securityPrivelege)
                {
                    classpath = System.getProperty("java.class.path");
                    userhome = System.getProperty("user.home");
                }
            }
            catch(Exception e)
            {
                filename = null;
                securityPrivelege = false;
            }
        if(classpath == null)
            securityPrivelege = false;
        InputStream registryInputStream = null;
        String jmfDir = getJMFDir();
        if(jmfDir != null)
            classpath = classpath + File.pathSeparator + jmfDir;
        if(securityPrivelege)
        {
            registryInputStream = findJMFPropertiesFile();
            if(registryInputStream == null)
                securityPrivelege = false;
        }
        if(!readRegistry(registryInputStream))
            hash = new Hashtable();
    }
}
