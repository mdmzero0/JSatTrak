package javax.media;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

public class PackageManager
{

    private static PackageManager pm = null;
    private static Method mGetProtocolPrefixList = null;
    private static Method mSetProtocolPrefixList = null;
    private static Method mCommitProtocolPrefixList = null;
    private static Method mGetContentPrefixList = null;
    private static Method mSetContentPrefixList = null;
    private static Method mCommitContentPrefixList = null;
    private static final Class sigNone[];
    private static final Class sigVector[];
    private static Vector protoPrefixList;
    private static Vector contentPrefixList;

    public PackageManager()
    {
    }

    private static Object runMethod(Method m, Object params[])
    {
        try
        {
            return m.invoke(null, params);
        }
        catch(IllegalAccessException iae) { }
        catch(IllegalArgumentException iare) { }
        catch(InvocationTargetException ite) { }
        return null;
    }

    public static Vector getProtocolPrefixList()
    {
        if(pm != null && mGetProtocolPrefixList != null)
        {
            return (Vector)runMethod(mGetProtocolPrefixList, null);
        } else
        {
            return protoPrefixList;
        }
    }

    public static void setProtocolPrefixList(Vector list)
    {
        if(pm != null && mSetProtocolPrefixList != null)
        {
            Object params[] = new Object[1];
            params[0] = list.clone();
            runMethod(mSetProtocolPrefixList, params);
            protoPrefixList = getProtocolPrefixList();
        } else
        {
            protoPrefixList = (Vector)list.clone();
        }
    }

    public static void commitProtocolPrefixList()
    {
        if(pm != null && mCommitProtocolPrefixList != null)
        {
            runMethod(mCommitProtocolPrefixList, null);
        }
    }

    public static Vector getContentPrefixList()
    {
        if(pm != null && mGetContentPrefixList != null)
        {
            return (Vector)runMethod(mGetContentPrefixList, null);
        } else
        {
            return contentPrefixList;
        }
    }

    public static void setContentPrefixList(Vector list)
    {
        if(pm != null && mSetContentPrefixList != null)
        {
            Object params[] = new Object[1];
            params[0] = list.clone();
            runMethod(mSetContentPrefixList, params);
            contentPrefixList = getContentPrefixList();
        } else
        {
            contentPrefixList = (Vector)list.clone();
        }
    }

    public static void commitContentPrefixList()
    {
        if(pm != null && mCommitContentPrefixList != null)
        {
            runMethod(mCommitContentPrefixList, null);
        }
    }

    static Method getDeclaredMethod(Class c, String name, Class params[])
        throws NoSuchMethodException
    {
        Method m = c.getMethod(name, params);
        if(m.getDeclaringClass() == c)
        {
            return m;
        } else
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

    static 
    {
        sigNone = new Class[0];
        sigVector = (new Class[] {
            java.util.Vector.class
        });
        protoPrefixList = new Vector();
        protoPrefixList.addElement("javax");
        protoPrefixList.addElement("com.sun");
        try
        {
            Class pmClass = Class.forName("javax.media.pm.PackageManager");
            if(pmClass != null)
            {
                Object tryPM = pmClass.newInstance();
                if(tryPM instanceof PackageManager)
                {
                    pm = (PackageManager)tryPM;
                    mGetProtocolPrefixList = getDeclaredMethod(pmClass, "getProtocolPrefixList", sigNone);
                    mSetProtocolPrefixList = getDeclaredMethod(pmClass, "setProtocolPrefixList", sigVector);
                    mCommitProtocolPrefixList = getDeclaredMethod(pmClass, "commitProtocolPrefixList", sigNone);
                    mGetContentPrefixList = getDeclaredMethod(pmClass, "getContentPrefixList", sigNone);
                    mSetContentPrefixList = getDeclaredMethod(pmClass, "setContentPrefixList", sigVector);
                    mCommitContentPrefixList = getDeclaredMethod(pmClass, "commitContentPrefixList", sigNone);
                }
            }
        }
        catch(ClassNotFoundException cnfe)
        {
            System.err.println(cnfe);
        }
        catch(InstantiationException ie)
        {
            System.err.println(ie);
        }
        catch(IllegalAccessException iae)
        {
            System.err.println(iae);
        }
        catch(SecurityException se)
        {
            System.out.println("PackageManager: SecurityException: " + se);
            System.err.println(se);
        }
        catch(NoSuchMethodException nsme) { }
        contentPrefixList = new Vector();
        contentPrefixList.addElement("javax");
        contentPrefixList.addElement("com.sun");
    }
}
