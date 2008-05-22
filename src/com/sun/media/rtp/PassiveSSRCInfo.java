// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PassiveSSRCInfo.java

package com.sun.media.rtp;

import javax.media.rtp.rtcp.ReceiverReport;

// Referenced classes of package com.sun.media.rtp:
//            SSRCInfo, SSRCCache

public class PassiveSSRCInfo extends SSRCInfo
    implements ReceiverReport
{

    PassiveSSRCInfo(SSRCCache cache, int ssrc)
    {
        super(cache, ssrc);
    }

    PassiveSSRCInfo(SSRCInfo info)
    {
        super(info);
    }
}
