// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CachedStream.java

package javax.media.protocol;


public interface CachedStream
{

    public abstract void setEnabledBuffering(boolean flag);

    public abstract boolean getEnabledBuffering();

    public abstract boolean willReadBytesBlock(long l, int i);

    public abstract boolean willReadBytesBlock(int i);

    public abstract void abortRead();
}
