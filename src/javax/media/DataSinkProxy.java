// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSinkProxy.java

package javax.media;


// Referenced classes of package javax.media:
//            MediaProxy, MediaLocator

public interface DataSinkProxy
    extends MediaProxy
{

    public abstract String getContentType(MediaLocator medialocator);
}
