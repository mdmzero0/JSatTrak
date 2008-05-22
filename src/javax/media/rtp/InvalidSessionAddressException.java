// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   InvalidSessionAddressException.java

package javax.media.rtp;


// Referenced classes of package javax.media.rtp:
//            SessionManagerException

public class InvalidSessionAddressException extends SessionManagerException
{

    public InvalidSessionAddressException()
    {
    }

    public InvalidSessionAddressException(String reason)
    {
        super(reason);
    }
}
