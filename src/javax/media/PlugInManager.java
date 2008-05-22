/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   PlugInManager.java

package javax.media;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

// Referenced classes of package javax.media:
//            Format, PackageManager

public class PlugInManager
{

    public PlugInManager()
    {
    }

    static Object runMethod(Method m, Object params[])
    {
        try
        {
            return m.invoke(null, params);
        }
        catch(IllegalAccessException iae)
        {
            System.err.println(iae);
        }
        catch(IllegalArgumentException iare)
        {
            System.err.println(iare);
        }
        catch(InvocationTargetException ite)
        {
            System.err.println(ite);
        }
        return null;
    }

    public static Vector getPlugInList(Format input, Format output, int type)
    {
        if(pim != null && mGetPlugInList != null)
        {
            Object params[] = new Object[3];
            params[0] = input;
            params[1] = output;
            params[2] = new Integer(type);
            return (Vector)runMethod(mGetPlugInList, params);
        } else
        {
            return new Vector(1);
        }
    }

    public static void setPlugInList(Vector plugins, int type)
    {
        if(pim != null && mSetPlugInList != null)
        {
            Object params[] = new Object[2];
            params[0] = plugins;
            params[1] = new Integer(type);
            runMethod(mSetPlugInList, params);
        }
    }

    public static void commit()
        throws IOException
    {
        if(pim != null && mCommit != null)
            runMethod(mCommit, null);
    }

    public static boolean addPlugIn(String classname, Format in[], Format out[], int type)
    {
        if(pim != null && mAddPlugIn != null)
        {
            Object params[] = new Object[4];
            params[0] = classname;
            params[1] = in;
            params[2] = out;
            params[3] = new Integer(type);
            Object result = runMethod(mAddPlugIn, params);
            if(result != null)
                return ((Boolean)result).booleanValue();
            else
                return false;
        } else
        {
            return false;
        }
    }

    public static boolean removePlugIn(String classname, int type)
    {
        if(pim != null && mRemovePlugIn != null)
        {
            Object params[] = new Object[2];
            params[0] = classname;
            params[1] = new Integer(type);
            Object result = runMethod(mRemovePlugIn, params);
            if(result != null)
                return ((Boolean)result).booleanValue();
            else
                return false;
        } else
        {
            return false;
        }
    }

    public static Format[] getSupportedInputFormats(String className, int type)
    {
        if(pim != null && mGetSupportedInputFormats != null)
        {
            Object params[] = new Object[2];
            params[0] = className;
            params[1] = new Integer(type);
            Object result = runMethod(mGetSupportedInputFormats, params);
            return (Format[])result;
        } else
        {
            return emptyFormat;
        }
    }

    public static Format[] getSupportedOutputFormats(String className, int type)
    {
        if(pim != null && mGetSupportedOutputFormats != null)
        {
            Object params[] = new Object[2];
            params[0] = className;
            params[1] = new Integer(type);
            Object result = runMethod(mGetSupportedOutputFormats, params);
            return (Format[])result;
        } else
        {
            return emptyFormat;
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

    private static PlugInManager pim = null;
    public static final int DEMULTIPLEXER = 1;
    public static final int CODEC = 2;
    public static final int EFFECT = 3;
    public static final int RENDERER = 4;
    public static final int MULTIPLEXER = 5;
    private static Method mGetPlugInList = null;
    private static Method mSetPlugInList = null;
    private static Method mCommit = null;
    private static Method mAddPlugIn = null;
    private static Method mRemovePlugIn = null;
    private static Method mGetSupportedInputFormats = null;
    private static Method mGetSupportedOutputFormats = null;
    private static Format emptyFormat[] = new Format[0];

    static 
    {
        Class classPIM = null;
        try
        {
            classPIM = Class.forName("javax.media.pim.PlugInManager");
            if(classPIM != null)
            {
                Object tryPIM = classPIM.newInstance();
                if(tryPIM instanceof PlugInManager)
                {
                    pim = (PlugInManager)tryPIM;
                    mGetSupportedInputFormats = PackageManager.getDeclaredMethod(classPIM, "getSupportedInputFormats", new Class[] {
                        java.lang.String.class, Integer.TYPE
                    });
                    mGetSupportedOutputFormats = PackageManager.getDeclaredMethod(classPIM, "getSupportedOutputFormats", new Class[] {
                        java.lang.String.class, Integer.TYPE
                    });
                    mGetPlugInList = PackageManager.getDeclaredMethod(classPIM, "getPlugInList", new Class[] {
                        javax.media.Format.class, javax.media.Format.class, Integer.TYPE
                    });
                    mSetPlugInList = PackageManager.getDeclaredMethod(classPIM, "setPlugInList", new Class[] {
                        java.util.Vector.class, Integer.TYPE
                    });
                    mAddPlugIn = PackageManager.getDeclaredMethod(classPIM, "addPlugIn", new Class[] {
                        java.lang.String.class, Format.formatArray, Format.formatArray, Integer.TYPE
                    });
                    mRemovePlugIn = PackageManager.getDeclaredMethod(classPIM, "removePlugIn", new Class[] {
                        java.lang.String.class, Integer.TYPE
                    });
                    mCommit = PackageManager.getDeclaredMethod(classPIM, "commit", null);
                }
            }
        }
        catch(ClassNotFoundException e)
        {
            System.err.println(e);
        }
        catch(InstantiationException e)
        {
            System.err.println(e);
        }
        catch(IllegalAccessException e)
        {
            System.err.println(e);
        }
        catch(SecurityException e)
        {
            System.err.println(e);
        }
        catch(NoSuchMethodException e)
        {
            System.err.println(e);
        }
    }
}
