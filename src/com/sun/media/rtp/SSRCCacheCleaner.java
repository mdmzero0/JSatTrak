// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SSRCCacheCleaner.java

package com.sun.media.rtp;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.rtp.util.RTPMediaThread;
import com.sun.media.rtp.util.SSRCTable;
import com.sun.media.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.event.*;

// Referenced classes of package com.sun.media.rtp:
//            SSRCInfo, RecvSSRCInfo, PassiveSSRCInfo, SSRCCache, 
//            StreamSynch, RTPSourceInfo, RTPEventHandler

public class SSRCCacheCleaner
    implements Runnable
{

    public SSRCCacheCleaner(SSRCCache cache, StreamSynch streamSynch)
    {
        timeToClean = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        killed = false;
        this.cache = cache;
        this.streamSynch = streamSynch;
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
        if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
        {
            try
            {
                Constructor conswithname = jdk12CreateThreadRunnableAction.conswithname;
                thread = (RTPMediaThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    conswithname.newInstance(new Object[] {
                        com.sun.media.rtp.util.RTPMediaThread.class, this, "SSRC Cache Cleaner"
                    })
                });
                Constructor pcons = jdk12PriorityAction.cons;
                jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    pcons.newInstance(new Object[] {
                        thread, new Integer(MediaThread.getControlPriority())
                    })
                });
            }
            catch(Exception e) { }
        } else
        {
            thread = new RTPMediaThread(this, "SSRC Cache Cleaner");
            thread.useControlPriority();
        }
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop()
    {
        killed = true;
        notifyAll();
    }

    public synchronized void run()
    {
        do
        {
            while(!timeToClean && !killed) 
                wait();
            if(killed)
                return;
            cleannow();
            timeToClean = false;
        } while(true);
        Exception e;
        e;
        e.printStackTrace();
        return;
    }

    public synchronized void setClean()
    {
        timeToClean = true;
        notifyAll();
    }

    public synchronized void cleannow()
    {
        long time = System.currentTimeMillis();
        if(cache.ourssrc == null)
            return;
        double reportInterval = cache.calcReportInterval(cache.ourssrc.sender, true);
        for(Enumeration enu = cache.cache.elements(); enu.hasMoreElements();)
        {
            SSRCInfo info = (SSRCInfo)enu.nextElement();
            if(!info.ours)
                if(info.byeReceived)
                {
                    if(time - info.byeTime < 1000L)
                    {
                        try
                        {
                            Thread.sleep((1000L - time) + info.byeTime);
                        }
                        catch(InterruptedException e) { }
                        time = System.currentTimeMillis();
                    }
                    info.byeTime = 0L;
                    info.byeReceived = false;
                    cache.remove(info.ssrc);
                    streamSynch.remove(info.ssrc);
                    boolean byepart = false;
                    RTPSourceInfo sourceInfo = info.sourceInfo;
                    if(sourceInfo != null && sourceInfo.getStreamCount() == 0)
                        byepart = true;
                    ByeEvent evtbye = null;
                    if(info instanceof RecvSSRCInfo)
                        evtbye = new ByeEvent(cache.sm, info.sourceInfo, (ReceiveStream)info, info.byereason, byepart);
                    if(info instanceof PassiveSSRCInfo)
                        evtbye = new ByeEvent(cache.sm, info.sourceInfo, null, info.byereason, byepart);
                    cache.eventhandler.postEvent(evtbye);
                } else
                if((double)info.lastHeardFrom + reportInterval <= (double)time)
                {
                    InactiveReceiveStreamEvent event = null;
                    if(!info.inactivesent)
                    {
                        boolean laststream = false;
                        RTPSourceInfo si = info.sourceInfo;
                        if(si != null && si.getStreamCount() == 1)
                            laststream = true;
                        if(info instanceof ReceiveStream)
                        {
                            event = new InactiveReceiveStreamEvent(cache.sm, info.sourceInfo, (ReceiveStream)info, laststream);
                        } else
                        {
                            reportInterval *= 5D;
                            if((double)info.lastHeardFrom + reportInterval <= (double)time)
                                event = new InactiveReceiveStreamEvent(cache.sm, info.sourceInfo, null, laststream);
                        }
                        if(event != null)
                        {
                            cache.eventhandler.postEvent(event);
                            info.quiet = true;
                            info.inactivesent = true;
                            info.setAlive(false);
                        }
                    } else
                    if(info.lastHeardFrom + 0x1b7740L <= time)
                    {
                        TimeoutEvent evt = null;
                        cache.remove(info.ssrc);
                        boolean byepart = false;
                        RTPSourceInfo sourceInfo = info.sourceInfo;
                        if(sourceInfo != null && sourceInfo.getStreamCount() == 0)
                            byepart = true;
                        if(info instanceof ReceiveStream)
                            evt = new TimeoutEvent(cache.sm, info.sourceInfo, (ReceiveStream)info, byepart);
                        else
                            evt = new TimeoutEvent(cache.sm, info.sourceInfo, null, byepart);
                        cache.eventhandler.postEvent(evt);
                    }
                }
        }

    }

    private SSRCCache cache;
    private RTPMediaThread thread;
    private static final int DEATHTIME = 0x1b7740;
    private static final int TIMEOUT_MULTIPLIER = 5;
    boolean timeToClean;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    private boolean killed;
    private StreamSynch streamSynch;

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
