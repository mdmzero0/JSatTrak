// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ExtBuffer.java

package com.sun.media;

import javax.media.Buffer;

// Referenced classes of package com.sun.media:
//            NBA

public class ExtBuffer extends Buffer
{

    public ExtBuffer()
    {
        nativeData = null;
        nativePreferred = false;
    }

    public void setNativeData(NBA nativeData)
    {
        this.nativeData = nativeData;
    }

    public NBA getNativeData()
    {
        return nativeData;
    }

    public boolean isNativePreferred()
    {
        return nativePreferred;
    }

    public void setNativePreferred(boolean prefer)
    {
        nativePreferred = prefer;
    }

    public Object getData()
    {
        if(nativeData != null)
            return nativeData.getData();
        else
            return super.data;
    }

    public void setData(Object data)
    {
        nativeData = null;
        super.data = data;
    }

    public void copy(Buffer buffer, boolean swap)
    {
        super.copy(buffer, swap);
        if(buffer instanceof ExtBuffer)
        {
            ExtBuffer fromBuf = (ExtBuffer)buffer;
            if(swap)
            {
                NBA temp = fromBuf.nativeData;
                fromBuf.nativeData = nativeData;
                nativeData = temp;
                boolean prefer = fromBuf.nativePreferred;
                fromBuf.nativePreferred = nativePreferred;
                nativePreferred = prefer;
            } else
            {
                nativeData = fromBuf.nativeData;
                nativePreferred = fromBuf.nativePreferred;
            }
        }
    }

    protected NBA nativeData;
    protected boolean nativePreferred;
}
