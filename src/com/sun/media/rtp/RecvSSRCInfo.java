// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RecvSSRCInfo.java

package com.sun.media.rtp;

import java.util.Vector;
import javax.media.protocol.DataSource;
import javax.media.rtp.*;
import javax.media.rtp.rtcp.*;

// Referenced classes of package com.sun.media.rtp:
//            SSRCInfo, SSRCCache, RTPSessionMgr

public class RecvSSRCInfo extends SSRCInfo
    implements ReceiveStream, SenderReport
{

    RecvSSRCInfo(SSRCCache cache, int ssrc)
    {
        super(cache, ssrc);
    }

    RecvSSRCInfo(SSRCInfo info)
    {
        super(info);
    }

    public Participant getParticipant()
    {
        SSRCCache cache = getSSRCCache();
        if((super.sourceInfo instanceof LocalParticipant) && cache.sm.IsNonParticipating())
            return null;
        else
            return super.sourceInfo;
    }

    public SenderReport getSenderReport()
    {
        return this;
    }

    public long getSSRC()
    {
        return (long)super.ssrc;
    }

    public DataSource getDataSource()
    {
        return super.dsource;
    }

    public long getSenderPacketCount()
    {
        return super.lastSRpacketcount;
    }

    public long getSenderByteCount()
    {
        return super.lastSRoctetcount;
    }

    public long getNTPTimeStampMSW()
    {
        return super.lastSRntptimestamp >> 32 & 0xffffffffL;
    }

    public long getNTPTimeStampLSW()
    {
        return super.lastSRntptimestamp & 0xffffffffL;
    }

    public long getRTPTimeStamp()
    {
        return super.lastSRrtptimestamp;
    }

    public Feedback getSenderFeedback()
    {
        SSRCCache cache = getSSRCCache();
        Report report = null;
        Vector reports = null;
        Vector feedback = null;
        Feedback reportblk = null;
        try
        {
            LocalParticipant localpartc = cache.sm.getLocalParticipant();
            reports = localpartc.getReports();
            for(int i = 0; i < reports.size(); i++)
            {
                report = (Report)reports.elementAt(i);
                feedback = report.getFeedbackReports();
                for(int j = 0; j < feedback.size(); j++)
                {
                    reportblk = (Feedback)feedback.elementAt(j);
                    long ssrc = reportblk.getSSRC();
                    if(ssrc == getSSRC())
                        return reportblk;
                }

            }

            return null;
        }
        catch(NullPointerException e)
        {
            return null;
        }
    }

    public RTPStream getStream()
    {
        return this;
    }

    public ReceptionStats getSourceReceptionStats()
    {
        return super.stats;
    }
}
