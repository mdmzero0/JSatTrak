// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SSRCCache.java

package com.sun.media.rtp;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.rtp.util.SSRCTable;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.media.format.AudioFormat;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.event.LocalCollisionEvent;
import javax.media.rtp.event.RemoteCollisionEvent;

// Referenced classes of package com.sun.media.rtp:
//            RTPSourceInfoCache, RTPEventHandler, SSRCInfo, RecvSSRCInfo, 
//            PassiveSSRCInfo, SendSSRCInfo, RTPSessionMgr, FormatInfo, 
//            OverallStats, OverallTransStats, TrueRandom, RTPSourceInfo, 
//            RTCPReporter

public class SSRCCache
{

    SSRCCache(RTPSessionMgr sm)
    {
        cache = new SSRCTable();
        stats = null;
        transstats = null;
        clockrate = new int[128];
        sendercount = 0;
        rtcp_bw_fraction = 0.0D;
        rtcp_sender_bw_fraction = 0.0D;
        rtcp_min_time = 5000;
        sessionbandwidth = 0;
        initial = true;
        byestate = false;
        rtcpsent = false;
        avgrtcpsize = 128;
        conflicttable = new Hashtable(5);
        ourssrc = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "thread";
                    jmfSecurity.requestPermission(m, cl, args, 16);
                    m[0].invoke(cl[0], args[0]);
                    permission = "thread group";
                    jmfSecurity.requestPermission(m, cl, args, 32);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.THREAD);
                    PolicyEngine.assertPermission(PermissionID.THREAD);
                }
            }
            catch(Throwable e)
            {
                if(permission.endsWith("group"))
                    jmfSecurity.permissionFailureNotification(32);
                else
                    jmfSecurity.permissionFailureNotification(16);
            }
        }
        stats = sm.defaultstats;
        transstats = sm.transstats;
        sourceInfoCache = new RTPSourceInfoCache();
        sourceInfoCache.setMainCache(sourceInfoCache);
        sourceInfoCache.setSSRCCache(this);
        this.sm = sm;
        eventhandler = new RTPEventHandler(sm);
        setclockrates();
    }

    SSRCCache(RTPSessionMgr sm, RTPSourceInfoCache sic)
    {
        cache = new SSRCTable();
        stats = null;
        transstats = null;
        clockrate = new int[128];
        sendercount = 0;
        rtcp_bw_fraction = 0.0D;
        rtcp_sender_bw_fraction = 0.0D;
        rtcp_min_time = 5000;
        sessionbandwidth = 0;
        initial = true;
        byestate = false;
        rtcpsent = false;
        avgrtcpsize = 128;
        conflicttable = new Hashtable(5);
        ourssrc = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        stats = sm.defaultstats;
        transstats = sm.transstats;
        sourceInfoCache = sic;
        sic.setSSRCCache(this);
        this.sm = sm;
        eventhandler = new RTPEventHandler(sm);
    }

    int aliveCount()
    {
        int tot = 0;
        for(Enumeration e = cache.elements(); e.hasMoreElements();)
        {
            SSRCInfo s = (SSRCInfo)e.nextElement();
            if(s.alive)
                tot++;
        }

        return tot;
    }

    void setclockrates()
    {
        for(int i = 0; i < 16; i++)
            clockrate[i] = 8000;

        clockrate[6] = 16000;
        clockrate[10] = 44100;
        clockrate[11] = 44100;
        clockrate[14] = 0x15f90;
        clockrate[16] = 11025;
        clockrate[17] = 22050;
        clockrate[18] = 44100;
        for(int i = 24; i < 34; i++)
            clockrate[i] = 0x15f90;

        for(int i = 96; i < 128; i++)
        {
            javax.media.Format fmt = sm.formatinfo.get(i);
            if(fmt != null && (fmt instanceof AudioFormat))
                clockrate[i] = (int)((AudioFormat)fmt).getSampleRate();
            else
                clockrate[i] = 0x15f90;
        }

    }

    synchronized void destroy()
    {
        cache.removeAll();
        if(eventhandler != null)
            eventhandler.close();
    }

    SSRCInfo lookup(int ssrc)
    {
        return (SSRCInfo)cache.get(ssrc);
    }

    SSRCInfo get(int ssrc, InetAddress address, int port, int mode)
    {
        SSRCInfo info = null;
        boolean localcollision = false;
        synchronized(this)
        {
            if(ourssrc != null && ourssrc.ssrc == ssrc && ourssrc.address != null && !ourssrc.address.equals(address))
            {
                localcollision = true;
                LocalCollision(ssrc);
            }
            info = lookup(ssrc);
            if(info != null)
                synchronized(info)
                {
                    if(info.address == null || !info.alive)
                    {
                        info.address = address;
                        info.port = port;
                    } else
                    if(!info.address.equals(address))
                        if(info.probation > 0)
                        {
                            info.probation = 2;
                            info.address = address;
                            info.port = port;
                        } else
                        {
                            stats.update(4, 1);
                            transstats.remote_coll++;
                            RemoteCollisionEvent evt = new RemoteCollisionEvent(sm, info.ssrc);
                            eventhandler.postEvent(evt);
                            SSRCInfo ssrcinfo5 = null;
                            return ssrcinfo5;
                        }
                }
            if(info != null && mode == 1 && !(info instanceof RecvSSRCInfo))
            {
                if(info.ours)
                {
                    SSRCInfo ssrcinfo1 = null;
                    return ssrcinfo1;
                }
                SSRCInfo newinfo = new RecvSSRCInfo(info);
                info = newinfo;
                cache.put(ssrc, info);
            }
            if(info != null && mode == 2 && !(info instanceof PassiveSSRCInfo))
            {
                if(info.ours)
                {
                    SSRCInfo ssrcinfo2 = null;
                    return ssrcinfo2;
                }
                System.out.println("changing to Passive");
                System.out.println("existing one " + info);
                SSRCInfo newinfo = new PassiveSSRCInfo(info);
                System.out.println("new one is " + newinfo);
                info = newinfo;
                cache.put(ssrc, info);
            }
            if(info == null)
            {
                if(mode == 3)
                {
                    if(ourssrc != null && ourssrc.ssrc == ssrc)
                    {
                        SSRCInfo ssrcinfo3 = ourssrc;
                        return ssrcinfo3;
                    }
                    info = new SendSSRCInfo(this, ssrc);
                    info.initsource((int)TrueRandom.rand());
                }
                if(mode == 1)
                    info = new RecvSSRCInfo(this, ssrc);
                if(mode == 2)
                    info = new PassiveSSRCInfo(this, ssrc);
                if(info == null)
                {
                    SSRCInfo ssrcinfo4 = null;
                    return ssrcinfo4;
                }
                info.address = address;
                info.port = port;
                cache.put(ssrc, info);
            }
            if(info.address == null && info.port == 0)
            {
                info.address = address;
                info.port = port;
            }
            if(localcollision)
            {
                LocalCollisionEvent levt = null;
                if(info instanceof RecvSSRCInfo)
                    levt = new LocalCollisionEvent(sm, (ReceiveStream)info, ourssrc.ssrc);
                else
                    levt = new LocalCollisionEvent(sm, null, ourssrc.ssrc);
                eventhandler.postEvent(levt);
            }
        }
        return info;
    }

    private void changessrc(SSRCInfo info)
    {
        info.setOurs(true);
        if(ourssrc != null)
        {
            info.sourceInfo = sourceInfoCache.get(ourssrc.sourceInfo.getCNAME(), info.ours);
            info.sourceInfo.addSSRC(info);
        }
        info.reporter.releasessrc("Local Collision Detected");
        ourssrc = info;
        info.reporter.restart = true;
    }

    private void LocalCollision(int ssrc)
    {
        int newssrc = 0;
        do
            newssrc = (int)TrueRandom.rand();
        while(lookup(newssrc) != null);
        SSRCInfo newinfo = new PassiveSSRCInfo(ourssrc);
        newinfo.ssrc = newssrc;
        cache.put(newssrc, newinfo);
        changessrc(newinfo);
        ourssrc = newinfo;
        stats.update(3, 1);
        transstats.local_coll++;
    }

    SSRCInfo get(int ssrc, InetAddress address, int port)
    {
        SSRCInfo ssrcinfo;
        synchronized(this)
        {
            SSRCInfo info = lookup(ssrc);
            ssrcinfo = info;
        }
        return ssrcinfo;
    }

    void remove(int ssrc)
    {
        SSRCInfo info = (SSRCInfo)cache.remove(ssrc);
        if(info != null)
            info.delete();
    }

    int getSessionBandwidth()
    {
        if(sessionbandwidth == 0)
            throw new IllegalArgumentException("Session Bandwidth not set");
        else
            return sessionbandwidth;
    }

    double calcReportInterval(boolean sender, boolean recvfromothers)
    {
        rtcp_min_time = 5000;
        double rtcp_bw = rtcp_bw_fraction;
        if(initial)
            rtcp_min_time = rtcp_min_time / 2;
        int n = aliveCount();
        if(sendercount > 0 && (double)sendercount < (double)n * rtcp_sender_bw_fraction)
            if(sender)
            {
                rtcp_bw *= rtcp_sender_bw_fraction;
                n = sendercount;
            } else
            {
                rtcp_bw *= 1.0D - rtcp_sender_bw_fraction;
                n -= sendercount;
            }
        if(recvfromothers && rtcp_bw == 0.0D)
        {
            rtcp_bw = 0.050000000000000003D;
            if(sendercount > 0 && (double)sendercount < (double)n * 0.25D)
                if(sender)
                {
                    rtcp_bw *= 0.25D;
                    n = sendercount;
                } else
                {
                    rtcp_bw *= 0.75D;
                    n -= sendercount;
                }
        }
        double time = 0.0D;
        if(rtcp_bw != 0.0D)
        {
            time = (double)(avgrtcpsize * n) / rtcp_bw;
            if(time < (double)rtcp_min_time)
                time = rtcp_min_time;
        }
        if(recvfromothers)
            return time;
        else
            return time * (Math.random() + 0.5D);
    }

    synchronized void updateavgrtcpsize(int size)
    {
        avgrtcpsize = (int)(0.0625D * (double)size + 0.9375D * (double)avgrtcpsize);
    }

    RTPSourceInfoCache getRTPSICache()
    {
        return sourceInfoCache;
    }

    SSRCTable getMainCache()
    {
        return cache;
    }

    public void reset(int size)
    {
        initial = true;
        sendercount = 0;
        avgrtcpsize = size;
    }

    SSRCTable cache;
    RTPSourceInfoCache sourceInfoCache;
    OverallStats stats;
    OverallTransStats transstats;
    RTPEventHandler eventhandler;
    int clockrate[];
    static final int DATA = 1;
    static final int CONTROL = 2;
    static final int SRCDATA = 3;
    static final int RTCP_MIN_TIME = 5000;
    static final int BYE_THRESHOLD = 50;
    int sendercount;
    double rtcp_bw_fraction;
    double rtcp_sender_bw_fraction;
    int rtcp_min_time;
    private static final int NOTIFYPERIOD = 500;
    int sessionbandwidth;
    boolean initial;
    boolean byestate;
    boolean rtcpsent;
    int avgrtcpsize;
    Hashtable conflicttable;
    SSRCInfo ourssrc;
    public RTPSessionMgr sm;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
    }
}
