// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReceiverReportEvent.java

package javax.media.rtp.event;

import javax.media.rtp.SessionManager;
import javax.media.rtp.rtcp.ReceiverReport;

// Referenced classes of package javax.media.rtp.event:
//            RemoteEvent

public class ReceiverReportEvent extends RemoteEvent
{

    public ReceiverReportEvent(SessionManager from, ReceiverReport report)
    {
        super(from);
        this.report = report;
    }

    public ReceiverReport getReport()
    {
        return report;
    }

    private ReceiverReport report;
}
