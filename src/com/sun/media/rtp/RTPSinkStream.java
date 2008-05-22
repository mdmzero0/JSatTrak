// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPSinkStream.java

package com.sun.media.rtp;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.rtp.util.RTPMediaThread;
import java.lang.reflect.Method;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.event.LocalPayloadChangeEvent;

// Referenced classes of package com.sun.media.rtp:
//            RTPTransmitter, SendSSRCInfo, SSRCCache, RTPSessionMgr, 
//            FormatInfo, SSRCInfo, RTPEventHandler, RTPRawSender

public class RTPSinkStream
    implements BufferTransferHandler
{

    public RTPSinkStream()
    {
        thread = null;
        current = new Buffer();
        started = false;
        startReq = new Integer(0);
        transmitter = null;
        sender = null;
        info = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        startTime = 0L;
        startPT = -1L;
        mpegBFrame = false;
        mpegPFrame = false;
        bufSizeSet = false;
        audioPT = 0L;
    }

    public void startStream()
    {
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
    }

    protected void setSSRCInfo(SendSSRCInfo info)
    {
        this.info = info;
    }

    protected void setTransmitter(RTPTransmitter t)
    {
        transmitter = t;
        if(transmitter != null)
            sender = transmitter.getSender();
    }

    public void transferData(PushBufferStream stream)
    {
        try
        {
            synchronized(startReq)
            {
                while(!started) 
                {
                    startPT = -1L;
                    startReq.wait();
                }
            }
            stream.read(current);
            if(!current.getFormat().matches(info.myformat))
            {
                int payload = transmitter.cache.sm.formatinfo.getPayload(current.getFormat());
                if(payload == -1)
                    return;
                LocalPayloadChangeEvent evt = new LocalPayloadChangeEvent(transmitter.cache.sm, info, ((SSRCInfo) (info)).payloadType, payload);
                transmitter.cache.eventhandler.postEvent(evt);
                info.payloadType = payload;
                info.myformat = current.getFormat();
            }
            if(info.myformat instanceof VideoFormat)
                transmitVideo();
            else
            if(info.myformat instanceof AudioFormat)
                transmitAudio();
        }
        catch(Exception e) { }
    }

    public void start()
    {
        if(started)
            return;
        started = true;
        synchronized(startReq)
        {
            startReq.notifyAll();
        }
    }

    public void stop()
    {
        started = false;
        startPT = -1L;
        synchronized(startReq)
        {
            startReq.notifyAll();
        }
    }

    protected void close()
    {
        stop();
    }

    private void transmitVideo()
    {
        if(current.isEOM() || current.isDiscard())
        {
            startPT = -1L;
            mpegBFrame = false;
            mpegPFrame = false;
            return;
        }
        if(startPT == -1L)
        {
            startTime = System.currentTimeMillis();
            startPT = current.getTimeStamp() / 0xf4240L;
        }
        if(current.getTimeStamp() > 0L && (current.getFlags() & 0x60) == 0 && (current.getFlags() & 0x800) != 0)
            if(mpegVideo.matches(info.myformat))
            {
                byte payload[] = (byte[])current.getData();
                int offset = current.getOffset();
                int ptype = payload[offset + 2] & 7;
                if(ptype > 2)
                    mpegBFrame = true;
                else
                if(ptype == 2)
                    mpegPFrame = true;
                if(ptype > 2 || ptype == 2 && !mpegBFrame || ptype == 1 && !(mpegBFrame | mpegPFrame))
                    waitForPT(startTime, startPT, current.getTimeStamp() / 0xf4240L);
            } else
            {
                waitForPT(startTime, startPT, current.getTimeStamp() / 0xf4240L);
            }
        transmitter.TransmitPacket(current, info);
    }

    private void transmitAudio()
    {
        if(current.isEOM() || current.isDiscard())
        {
            startPT = -1L;
            return;
        }
        if(startPT == -1L)
        {
            startTime = System.currentTimeMillis();
            startPT = current.getTimeStamp() <= 0L ? 0L : current.getTimeStamp() / 0xf4240L;
            audioPT = startPT;
        }
        if((current.getFlags() & 0x60) == 0)
        {
            if(mpegAudio.matches(current.getFormat()))
                audioPT = current.getTimeStamp() / 0xf4240L;
            else
                audioPT += ((AudioFormat)info.myformat).computeDuration(current.getLength()) / 0xf4240L;
            waitForPT(startTime, startPT, audioPT);
        }
        transmitter.TransmitPacket(current, info);
    }

    private void waitForPT(long start, long startPT, long pt)
    {
        for(long delay = pt - startPT - (System.currentTimeMillis() - start); delay > (long)LEEWAY; delay = pt - startPT - (System.currentTimeMillis() - start))
        {
            if(delay > (long)THRESHOLD)
                delay = THRESHOLD;
            try
            {
                Thread.currentThread();
                Thread.sleep(delay);
                continue;
            }
            catch(Exception e) { }
            break;
        }

    }

    private RTPMediaThread thread;
    Buffer current;
    boolean started;
    Object startReq;
    RTPTransmitter transmitter;
    RTPRawSender sender;
    SendSSRCInfo info;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    static AudioFormat mpegAudio = new AudioFormat("mpegaudio/rtp");
    static VideoFormat mpegVideo = new VideoFormat("mpeg/rtp");
    long startTime;
    long startPT;
    int rate;
    boolean mpegBFrame;
    boolean mpegPFrame;
    boolean bufSizeSet;
    long audioPT;
    static int THRESHOLD = 80;
    static int LEEWAY = 5;

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
