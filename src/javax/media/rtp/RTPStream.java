// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPStream.java

package javax.media.rtp;

import javax.media.protocol.DataSource;
import javax.media.rtp.rtcp.SenderReport;

// Referenced classes of package javax.media.rtp:
//            Participant

public interface RTPStream
{

    public abstract Participant getParticipant();

    public abstract SenderReport getSenderReport();

    public abstract long getSSRC();

    public abstract DataSource getDataSource();
}
