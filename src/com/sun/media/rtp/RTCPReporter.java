// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTCPReporter.java

package com.sun.media.rtp;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.*;
import com.sun.media.rtp.util.RTPMediaThread;
import com.sun.media.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Random;

// Referenced classes of package com.sun.media.rtp:
//            RTCPTransmitter, SSRCInfo, SSRCCache

public class RTCPReporter
    implements Runnable
{

    public RTCPReporter(SSRCCache cache, RTCPTransmitter t)
    {
        restart = false;
        closed = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        this.cache = cache;
        setTransmitter(t);
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
                Constructor cons = jdk12CreateThreadRunnableAction.cons;
                reportthread = (RTPMediaThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.rtp.util.RTPMediaThread.class, this
                    })
                });
                reportthread.setName("RTCP Reporter");
                cons = jdk12PriorityAction.cons;
                jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        reportthread, new Integer(MediaThread.getControlPriority())
                    })
                });
            }
            catch(Exception e) { }
        } else
        {
            reportthread = new RTPMediaThread(this, "RTCP Reporter");
            reportthread.useControlPriority();
        }
        reportthread.setDaemon(true);
        reportthread.start();
    }

    public void setTransmitter(RTCPTransmitter t)
    {
        transmit = t;
    }

    public void close(String reason)
    {
        synchronized(reportthread)
        {
            closed = true;
            reportthread.notify();
        }
        releasessrc(reason);
        transmit.close();
    }

    public void releasessrc(String reason)
    {
        transmit.bye(reason);
        transmit.ssrcInfo.setOurs(false);
        transmit.ssrcInfo = null;
    }

    public void run()
    {
        if(jmfSecurity != null)
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    jmfSecurity.requestPermission(m, cl, args, 128);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.NETIO);
                    PolicyEngine.assertPermission(PermissionID.NETIO);
                }
            }
            catch(Throwable e)
            {
                jmfSecurity.permissionFailureNotification(128);
            }
        if(restart)
            restart = false;
        do
        {
            double delay = cache.calcReportInterval(cache.ourssrc.sender, false);
            synchronized(reportthread)
            {
                try
                {
                    reportthread.wait((long)delay);
                }
                catch(InterruptedException e)
                {
                    Log.dumpStack(e);
                }
            }
            if(closed)
                return;
            if(!restart)
                transmit.report();
            else
                restart = false;
        } while(true);
    }

    RTCPTransmitter transmit;
    SSRCCache cache;
    RTPMediaThread reportthread;
    Random myrand;
    boolean restart;
    boolean closed;
    InetAddress host;
    String cname;
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
