// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Seekable.java

package javax.media.protocol;


public interface Seekable
{

    public abstract long seek(long l);

    public abstract long tell();

    public abstract boolean isRandomAccess();
}
