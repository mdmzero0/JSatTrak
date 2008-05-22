// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPLocalSourceInfo.java

package com.sun.media.rtp;

import javax.media.rtp.LocalParticipant;
import javax.media.rtp.rtcp.SourceDescription;

// Referenced classes of package com.sun.media.rtp:
//            RTPSourceInfo, RTPSourceInfoCache, SSRCCache, SSRCInfo

public class RTPLocalSourceInfo extends RTPSourceInfo
    implements LocalParticipant
{

    public RTPLocalSourceInfo(String cname, RTPSourceInfoCache sic)
    {
        super(cname, sic);
    }

    public void setSourceDescription(SourceDescription sdeslist[])
    {
        super.sic.ssrccache.ourssrc.setSourceDescription(sdeslist);
    }
}
