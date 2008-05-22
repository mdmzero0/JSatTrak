// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NoDataSourceException.java

package javax.media;


// Referenced classes of package javax.media:
//            MediaException

public class NoDataSourceException extends MediaException
{

    public NoDataSourceException()
    {
    }

    public NoDataSourceException(String reason)
    {
        super(reason);
    }
}
