/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   CaptureDeviceManager.java

package javax.media;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

// Referenced classes of package javax.media:
//            CaptureDeviceInfo, PackageManager, Format

public class CaptureDeviceManager
{

    public CaptureDeviceManager()
    {
    }

    private static Object runMethod(Method m, Object params[])
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

    public static CaptureDeviceInfo getDevice(String deviceName)
    {
        if(cdm != null && mGetDevice != null)
        {
            Object params[] = new Object[1];
            params[0] = deviceName;
            return (CaptureDeviceInfo)runMethod(mGetDevice, params);
        } else
        {
            return null;
        }
    }

    public static Vector getDeviceList(Format format)
    {
        if(cdm != null && mGetDeviceList != null)
        {
            Object params[] = new Object[1];
            params[0] = format;
            Vector returnVal = (Vector)runMethod(mGetDeviceList, params);
            if(returnVal == null)
                return new Vector(1);
            else
                return returnVal;
        } else
        {
            return new Vector(1);
        }
    }

    public static boolean addDevice(CaptureDeviceInfo newDevice)
    {
        if(cdm != null && mAddDevice != null)
        {
            Object params[] = new Object[1];
            params[0] = newDevice;
            Object result = runMethod(mAddDevice, params);
            if(result != null)
                return ((Boolean)result).booleanValue();
            else
                return false;
        } else
        {
            return false;
        }
    }

    public static boolean removeDevice(CaptureDeviceInfo device)
    {
        if(cdm != null && mRemoveDevice != null)
        {
            Object params[] = new Object[1];
            params[0] = device;
            Object result = runMethod(mRemoveDevice, params);
            if(result != null)
                return ((Boolean)result).booleanValue();
            else
                return false;
        } else
        {
            return false;
        }
    }

    public static void commit()
        throws IOException
    {
        if(cdm != null && mCommit != null)
            runMethod(mCommit, null);
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

    private static CaptureDeviceManager cdm = null;
    private static Method mGetDeviceList = null;
    private static Method mGetDevice = null;
    private static Method mAddDevice = null;
    private static Method mRemoveDevice = null;
    private static Method mCommit = null;

    static 
    {
        try
        {
            Class classCDM = Class.forName("javax.media.cdm.CaptureDeviceManager");
            if(classCDM != null)
            {
                Object tryCDM = classCDM.newInstance();
                if(tryCDM instanceof CaptureDeviceManager)
                {
                    cdm = (CaptureDeviceManager)tryCDM;
                    mGetDeviceList = PackageManager.getDeclaredMethod(classCDM, "getDeviceList", new Class[] {
                        javax.media.Format.class
                    });
                    mGetDevice = PackageManager.getDeclaredMethod(classCDM, "getDevice", new Class[] {
                        java.lang.String.class
                    });
                    mCommit = PackageManager.getDeclaredMethod(classCDM, "commit", null);
                    mAddDevice = PackageManager.getDeclaredMethod(classCDM, "addDevice", new Class[] {
                        javax.media.CaptureDeviceInfo.class
                    });
                    mRemoveDevice = PackageManager.getDeclaredMethod(classCDM, "removeDevice", new Class[] {
                        javax.media.CaptureDeviceInfo.class
                    });
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
            System.err.println(se);
        }
        catch(NoSuchMethodException e)
        {
            System.err.println(e);
        }
    }
}
