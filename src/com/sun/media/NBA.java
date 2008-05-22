// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NBA.java

package com.sun.media;

import java.io.PrintStream;

// Referenced classes of package com.sun.media:
//            JMFSecurityManager

public final class NBA
{

    public NBA(Class type, int size)
    {
        this.type = null;
        javaData = null;
        atype = 1;
        this.type = type;
        this.size = size;
        if(type == (short[].class))
        {
            atype = 2;
            size *= 2;
        } else
        if(type == (int[].class))
        {
            atype = 4;
            size *= 4;
        } else
        if(type == (long[].class))
        {
            atype = 8;
            size *= 8;
        }
        data = nAllocate(size);
        if(data == 0L)
            throw new OutOfMemoryError("Couldn't allocate native buffer");
        else
            return;
    }

    protected final synchronized void finalize()
    {
        if(data != 0L)
            nDeallocate(data);
        data = 0L;
    }

    public synchronized Object getData()
    {
        if(javaData == null)
            if(type == (byte[].class))
                javaData = new byte[size];
            else
            if(type == (short[].class))
                javaData = new short[size];
            else
            if(type == (int[].class))
                javaData = new int[size];
            else
            if(type == (long[].class))
            {
                javaData = new long[size];
            } else
            {
                System.err.println("NBA: Don't handle this data type");
                return null;
            }
        nCopyToJava(data, javaData, size, atype);
        return javaData;
    }

    public synchronized Object clone()
    {
        NBA cl = new NBA(type, size);
        nCopyToNative(data, cl.data, size);
        return cl;
    }

    public synchronized void copyTo(NBA nba)
    {
        if(nba.size >= size)
            nCopyToNative(data, nba.data, size);
    }

    public synchronized void copyTo(byte javadata[])
    {
        if(javadata.length >= size)
            nCopyToJava(data, javadata, size, atype);
    }

    public synchronized long getNativeData()
    {
        return data;
    }

    public int getSize()
    {
        return size;
    }

    private native long nAllocate(int i);

    private native void nDeallocate(long l);

    private native void nCopyToNative(long l, long l1, int i);

    private native void nCopyToJava(long l, Object obj, int i, int j);

    private long data;
    private int size;
    private Class type;
    private Object javaData;
    private int atype;

    static 
    {
        try
        {
            JMFSecurityManager.loadLibrary("jmutil");
        }
        catch(Throwable t) { }
    }
}
