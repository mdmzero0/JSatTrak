// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPSourceInfo.java

package com.sun.media.rtp;

import java.util.Vector;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPStream;
import javax.media.rtp.rtcp.SourceDescription;

// Referenced classes of package com.sun.media.rtp:
//            SSRCInfo, RTPSourceInfoCache, SSRCCache, RTPSessionMgr

public abstract class RTPSourceInfo
    implements Participant
{

    RTPSourceInfo(String cname, RTPSourceInfoCache sic)
    {
        this.cname = new SourceDescription(1, cname, 0, false);
        this.sic = sic;
        ssrc = new SSRCInfo[0];
    }

    SourceDescription getCNAMESDES()
    {
        return cname;
    }

    public String getCNAME()
    {
        return cname.getDescription();
    }

    public Vector getStreams()
    {
        Vector recvstreams = new Vector();
        for(int i = 0; i < ssrc.length; i++)
            if(ssrc[i].isActive())
                recvstreams.addElement(ssrc[i]);

        recvstreams.trimToSize();
        return recvstreams;
    }

    RTPStream getSSRCStream(long filterssrc)
    {
        for(int i = 0; i < ssrc.length; i++)
            if((ssrc[i] instanceof RTPStream) && ssrc[i].ssrc == (int)filterssrc)
                return (RTPStream)ssrc[i];

        return null;
    }

    public Vector getReports()
    {
        Vector reportlist = new Vector();
        for(int i = 0; i < ssrc.length; i++)
            reportlist.addElement(ssrc[i]);

        reportlist.trimToSize();
        return reportlist;
    }

    public Vector getSourceDescription()
    {
        Vector sdeslist = null;
        if(ssrc.length == 0)
        {
            sdeslist = new Vector(0);
            return sdeslist;
        } else
        {
            sdeslist = ssrc[0].getSourceDescription();
            return sdeslist;
        }
    }

    synchronized void addSSRC(SSRCInfo ssrcinfo)
    {
        for(int i = 0; i < ssrc.length; i++)
            if(ssrc[i] == ssrcinfo)
                return;

        System.arraycopy(ssrc, 0, ssrc = new SSRCInfo[ssrc.length + 1], 0, ssrc.length - 1);
        ssrc[ssrc.length - 1] = ssrcinfo;
    }

    synchronized void removeSSRC(SSRCInfo ssrcinfo)
    {
        if(ssrcinfo.dsource != null)
            sic.ssrccache.sm.removeDataSource(ssrcinfo.dsource);
        for(int i = 0; i < ssrc.length; i++)
        {
            if(ssrc[i] != ssrcinfo)
                continue;
            ssrc[i] = ssrc[ssrc.length - 1];
            System.arraycopy(ssrc, 0, ssrc = new SSRCInfo[ssrc.length - 1], 0, ssrc.length);
            break;
        }

        if(ssrc.length == 0)
            sic.remove(cname.getDescription());
    }

    int getStreamCount()
    {
        return ssrc.length;
    }

    RTPSourceInfoCache sic;
    private SSRCInfo ssrc[];
    private SourceDescription cname;
}
