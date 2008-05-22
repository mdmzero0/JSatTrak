// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SourceRTPPacket.java

package com.sun.media.rtp;

import com.sun.media.rtp.util.RTPPacket;

// Referenced classes of package com.sun.media.rtp:
//            SSRCInfo

public class SourceRTPPacket
{

    public SourceRTPPacket(RTPPacket p, SSRCInfo ssrcinfo)
    {
        this.p = p;
        this.ssrcinfo = ssrcinfo;
    }

    RTPPacket p;
    SSRCInfo ssrcinfo;
}
