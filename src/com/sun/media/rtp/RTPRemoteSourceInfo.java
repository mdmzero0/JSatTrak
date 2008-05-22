// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPRemoteSourceInfo.java

package com.sun.media.rtp;

import javax.media.rtp.RemoteParticipant;

// Referenced classes of package com.sun.media.rtp:
//            RTPSourceInfo, RTPSourceInfoCache

public class RTPRemoteSourceInfo extends RTPSourceInfo
    implements RemoteParticipant
{

    public RTPRemoteSourceInfo(String cname, RTPSourceInfoCache sic)
    {
        super(cname, sic);
    }
}
