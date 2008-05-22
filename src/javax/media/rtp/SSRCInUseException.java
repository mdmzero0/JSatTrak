// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SSRCInUseException.java

package javax.media.rtp;


// Referenced classes of package javax.media.rtp:
//            SessionManagerException

public class SSRCInUseException extends SessionManagerException
{

    public SSRCInUseException()
    {
    }

    public SSRCInUseException(String reason)
    {
        super(reason);
    }
}
