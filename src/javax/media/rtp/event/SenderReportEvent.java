// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SenderReportEvent.java

package javax.media.rtp.event;

import javax.media.rtp.SessionManager;
import javax.media.rtp.rtcp.SenderReport;

// Referenced classes of package javax.media.rtp.event:
//            RemoteEvent

public class SenderReportEvent extends RemoteEvent
{

    public SenderReportEvent(SessionManager from, SenderReport report)
    {
        super(from);
        this.report = report;
    }

    public SenderReport getReport()
    {
        return report;
    }

    private SenderReport report;
}
