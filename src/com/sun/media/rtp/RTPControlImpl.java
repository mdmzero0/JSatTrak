// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPControlImpl.java

package com.sun.media.rtp;

import com.sun.media.util.RTPInfo;
import java.awt.Component;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.media.Format;
import javax.media.rtp.*;

// Referenced classes of package com.sun.media.rtp:
//            RecvSSRCInfo, SSRCInfo

public abstract class RTPControlImpl
    implements RTPControl, RTPInfo
{

    public RTPControlImpl()
    {
        cname = null;
        codeclist = null;
        rtptime = 0;
        seqno = 0;
        payload = -1;
        codec = "";
        currentformat = null;
        stream = null;
        codeclist = new Hashtable(5);
    }

    public abstract int getSSRC();

    public abstract String getCNAME();

    public void addFormat(Format info, int payload)
    {
        codeclist.put(new Integer(payload), info);
    }

    public Format getFormat()
    {
        return currentformat;
    }

    public Format getFormat(int payload)
    {
        return (Format)codeclist.get(new Integer(payload));
    }

    public Format[] getFormatList()
    {
        Format infolist[] = new Format[codeclist.size()];
        int i = 0;
        for(Enumeration e = codeclist.elements(); e.hasMoreElements();)
        {
            Format f = (Format)e.nextElement();
            infolist[i++] = (Format)f.clone();
        }

        return infolist;
    }

    public void setRTPInfo(int rtptime, int seqno)
    {
        this.rtptime = rtptime;
        this.seqno = seqno;
    }

    public String toString()
    {
        String s = "\n\tRTPTime is " + rtptime + "\n\tSeqno is " + seqno;
        if(codeclist != null)
            s = s + "\n\tCodecInfo is " + codeclist.toString();
        else
            s = s + "\n\tcodeclist is null";
        return s;
    }

    public ReceptionStats getReceptionStats()
    {
        if(stream == null)
        {
            return null;
        } else
        {
            RecvSSRCInfo recvstream = (RecvSSRCInfo)stream;
            return recvstream.getSourceReceptionStats();
        }
    }

    public GlobalReceptionStats getGlobalStats()
    {
        return null;
    }

    public Component getControlComponent()
    {
        return null;
    }

    String cname;
    Hashtable codeclist;
    int rtptime;
    int seqno;
    int payload;
    String codec;
    Format currentformat;
    SSRCInfo stream;
}
