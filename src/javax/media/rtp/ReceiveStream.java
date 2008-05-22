// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReceiveStream.java

package javax.media.rtp;


// Referenced classes of package javax.media.rtp:
//            RTPStream, ReceptionStats

public interface ReceiveStream
    extends RTPStream
{

    public abstract ReceptionStats getSourceReceptionStats();
}
