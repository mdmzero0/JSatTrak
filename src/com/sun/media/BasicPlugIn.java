// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicPlugIn.java

package com.sun.media;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Vector;
import javax.media.*;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media:
//            NBA, ExtBuffer

public abstract class BasicPlugIn
    implements PlugIn
{

    public BasicPlugIn()
    {
        controls = new Control[0];
    }

    protected void error()
    {
        throw new RuntimeException(getClass().getName() + " PlugIn error");
    }

    public Object[] getControls()
    {
        return (Object[])controls;
    }

    public Object getControl(String controlType)
    {
        try
        {
            Class cls = Class.forName(controlType);
            Object cs[] = getControls();
            for(int i = 0; i < cs.length; i++)
                if(cls.isInstance(cs[i]))
                    return cs[i];

            return null;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static Format matches(Format in, Format outs[])
    {
        for(int i = 0; i < outs.length; i++)
            if(in.matches(outs[i]))
                return outs[i];

        return null;
    }

    protected byte[] validateByteArraySize(Buffer buffer, int newSize)
    {
        Object objectArray = buffer.getData();
        byte typedArray[];
        if(objectArray instanceof byte[])
        {
            typedArray = (byte[])objectArray;
            if(typedArray.length >= newSize)
                return typedArray;
            byte tempArray[] = new byte[newSize];
            System.arraycopy(typedArray, 0, tempArray, 0, typedArray.length);
            typedArray = tempArray;
        } else
        {
            typedArray = new byte[newSize];
        }
        buffer.setData(typedArray);
        return typedArray;
    }

    protected short[] validateShortArraySize(Buffer buffer, int newSize)
    {
        Object objectArray = buffer.getData();
        short typedArray[];
        if(objectArray instanceof short[])
        {
            typedArray = (short[])objectArray;
            if(typedArray.length >= newSize)
                return typedArray;
            short tempArray[] = new short[newSize];
            System.arraycopy(typedArray, 0, tempArray, 0, typedArray.length);
            typedArray = tempArray;
        } else
        {
            typedArray = new short[newSize];
        }
        buffer.setData(typedArray);
        return typedArray;
    }

    protected int[] validateIntArraySize(Buffer buffer, int newSize)
    {
        Object objectArray = buffer.getData();
        int typedArray[];
        if(objectArray instanceof int[])
        {
            typedArray = (int[])objectArray;
            if(typedArray.length >= newSize)
                return typedArray;
            int tempArray[] = new int[newSize];
            System.arraycopy(typedArray, 0, tempArray, 0, typedArray.length);
            typedArray = tempArray;
        } else
        {
            typedArray = new int[newSize];
        }
        buffer.setData(typedArray);
        return typedArray;
    }

    protected final long getNativeData(Object data)
    {
        if(data instanceof NBA)
            return ((NBA)data).getNativeData();
        else
            return 0L;
    }

    protected Object getInputData(Buffer inBuffer)
    {
        Object inData = null;
        if(inBuffer instanceof ExtBuffer)
        {
            ((ExtBuffer)inBuffer).setNativePreferred(true);
            inData = ((ExtBuffer)inBuffer).getNativeData();
        }
        if(inData == null)
            inData = inBuffer.getData();
        return inData;
    }

    protected Object getOutputData(Buffer buffer)
    {
        Object data = null;
        if(buffer instanceof ExtBuffer)
            data = ((ExtBuffer)buffer).getNativeData();
        if(data == null)
            data = buffer.getData();
        return data;
    }

    protected Object validateData(Buffer buffer, int length, boolean allowNative)
    {
        Format format = buffer.getFormat();
        Class dataType = format.getDataType();
        if(length < 1 && format != null && (format instanceof VideoFormat))
            length = ((VideoFormat)format).getMaxDataLength();
        if(allowNative && (buffer instanceof ExtBuffer) && ((ExtBuffer)buffer).isNativePreferred())
        {
            ExtBuffer extb = (ExtBuffer)buffer;
            if(extb.getNativeData() == null || extb.getNativeData().getSize() < length)
                extb.setNativeData(new NBA(format.getDataType(), length));
            return extb.getNativeData();
        }
        if(dataType == Format.byteArray)
            return validateByteArraySize(buffer, length);
        if(dataType == Format.shortArray)
            return validateShortArraySize(buffer, length);
        if(dataType == Format.intArray)
        {
            return validateIntArraySize(buffer, length);
        } else
        {
            System.err.println("Error in validateData");
            return null;
        }
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

    public static Class getClassForName(String className)
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

    public static boolean plugInExists(String name, int type)
    {
        Vector cnames = PlugInManager.getPlugInList(null, null, type);
        for(int i = 0; i < cnames.size(); i++)
            if(name.equals((String)cnames.elementAt(i)))
                return true;

        return false;
    }

    public abstract void reset();

    public abstract void close();

    public abstract void open()
        throws ResourceUnavailableException;

    public abstract String getName();

    private static final boolean DEBUG = false;
    protected Object controls[];
    private static boolean jdkInit = false;
    private static Method forName3ArgsM;
    private static Method getSystemClassLoaderM;
    private static ClassLoader systemClassLoader;
    private static Method getContextClassLoaderM;

}
