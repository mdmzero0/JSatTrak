// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPSyncBufferMux.java

package com.sun.media.multiplexer;

import com.sun.media.rtp.FormatInfo;
import com.sun.media.rtp.RTPSessionMgr;
import javax.media.Format;
import javax.media.protocol.ContentDescriptor;

// Referenced classes of package com.sun.media.multiplexer:
//            RawSyncBufferMux, RawBufferMux

public class RTPSyncBufferMux extends RawSyncBufferMux
{

    public RTPSyncBufferMux()
    {
        rtpFormats = new FormatInfo();
        super.supported = new ContentDescriptor[1];
        super.supported[0] = new ContentDescriptor("raw.rtp");
        super.monoIncrTime = true;
    }

    public String getName()
    {
        return "RTP Sync Buffer Multiplexer";
    }

    public Format setInputFormat(Format input, int trackID)
    {
        if(!RTPSessionMgr.formatSupported(input))
            return null;
        else
            return super.setInputFormat(input, trackID);
    }

    FormatInfo rtpFormats;
}
