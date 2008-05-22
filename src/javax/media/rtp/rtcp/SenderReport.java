// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SenderReport.java

package javax.media.rtp.rtcp;

import javax.media.rtp.RTPStream;

// Referenced classes of package javax.media.rtp.rtcp:
//            Report, Feedback

public interface SenderReport
    extends Report
{

    public abstract RTPStream getStream();

    public abstract long getSenderPacketCount();

    public abstract long getSenderByteCount();

    public abstract long getNTPTimeStampMSW();

    public abstract long getNTPTimeStampLSW();

    public abstract long getRTPTimeStamp();

    public abstract Feedback getSenderFeedback();
}
