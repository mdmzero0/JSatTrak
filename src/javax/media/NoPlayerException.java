// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NoPlayerException.java

package javax.media;


// Referenced classes of package javax.media:
//            MediaException

public class NoPlayerException extends MediaException
{

    public NoPlayerException()
    {
    }

    public NoPlayerException(String reason)
    {
        super(reason);
    }
}
