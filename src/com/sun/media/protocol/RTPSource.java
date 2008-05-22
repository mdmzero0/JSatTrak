// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPSource.java

package com.sun.media.protocol;


// Referenced classes of package com.sun.media.protocol:
//            BufferListener

public interface RTPSource
{

    public abstract int getSSRC();

    public abstract String getCNAME();

    public abstract void prebuffer();

    public abstract void flush();

    public abstract void setBufferListener(BufferListener bufferlistener);
}
