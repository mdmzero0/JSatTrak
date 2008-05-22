// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPSourceStream.java

package com.sun.media.rtp;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.*;
import com.sun.media.protocol.BasicSourceStream;
import com.sun.media.protocol.BufferListener;
import com.sun.media.protocol.rtp.DataSource;
import com.sun.media.rtp.util.RTPMediaThread;
import com.sun.media.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media.rtp:
//            BufferControlImpl, RTPRawReceiver

public class RTPSourceStream extends BasicSourceStream
    implements PushBufferStream, Runnable
{
    class PktQue
    {

        public synchronized void reset()
        {
            for(; moreFilled(); returnFree(get()));
            tooMuchBufferingCount = 0;
            notifyAll();
        }

        public int totalPkts()
        {
            return tailFill < headFill ? size - (headFill - tailFill) : tailFill - headFill;
        }

        public int totalFree()
        {
            return tailFree < headFree ? size - (headFree - tailFree) : tailFree - headFree;
        }

        public synchronized void addPkt(Buffer buf)
        {
            long head = -1L;
            long tail = -1L;
            long seq = buf.getSequenceNumber();
            if(moreFilled())
            {
                head = fill[headFill].getSequenceNumber();
                int i = tailFill - 1;
                if(i < 0)
                    i = size - 1;
                tail = fill[i].getSequenceNumber();
            }
            if(head == -1L && tail == -1L)
                append(buf);
            else
            if(seq < head)
                prepend(buf);
            else
            if(head < seq && seq < tail)
                insert(buf);
            else
            if(seq > tail)
                append(buf);
            else
                returnFree(buf);
        }

        public void monitorQueueSize(Buffer buf, RTPRawReceiver rtpr)
        {
            sizePerPkt = (sizePerPkt + buf.getLength()) / 2;
            if(format instanceof VideoFormat)
            {
                if(lastPktSeq + 1L == buf.getSequenceNumber())
                    pktsEst++;
                else
                    pktsEst = 1;
                lastPktSeq = buf.getSequenceNumber();
                if(RTPSourceStream.mpegVideo.matches(format))
                {
                    byte payload[] = (byte[])buf.getData();
                    int offset = buf.getOffset();
                    int ptype = payload[offset + 2] & 7;
                    if(ptype < 3 && (buf.getFlags() & 0x800) != 0)
                    {
                        pktsPerFrame = (pktsPerFrame + pktsEst) / 2;
                        pktsEst = 0;
                    }
                    fps = 30;
                } else
                if((buf.getFlags() & 0x800) != 0)
                {
                    pktsPerFrame = (pktsPerFrame + pktsEst) / 2;
                    pktsEst = 0;
                    framesEst++;
                    long now = System.currentTimeMillis();
                    if(now - lastCheckTime >= 1000L)
                    {
                        lastCheckTime = now;
                        fps = (fps + framesEst) / 2;
                        framesEst = 0;
                        if(fps > 30)
                            fps = 30;
                    }
                }
                int pktsToBuffer;
                if(bc != null)
                {
                    pktsToBuffer = (int)((bc.getBufferLength() * (long)fps) / 1000L);
                    if(pktsToBuffer <= 0)
                        pktsToBuffer = 1;
                    pktsToBuffer = pktsPerFrame * pktsToBuffer;
                    threshold = (int)(((bc.getMinimumThreshold() * (long)fps) / 1000L) * (long)pktsPerFrame);
                    if(threshold <= pktsToBuffer / 2);
                    threshold = pktsToBuffer / 2;
                } else
                {
                    pktsToBuffer = DEFAULT_PKTS_TO_BUFFER;
                }
                if(maxPktsToBuffer > 0)
                    maxPktsToBuffer = (maxPktsToBuffer + pktsToBuffer) / 2;
                else
                    maxPktsToBuffer = pktsToBuffer;
                int tot = totalPkts();
                if(size > MIN_BUF_CHECK && tot < size / 4)
                {
                    if(!prebuffering && tooMuchBufferingCount++ > pktsPerFrame * fps * BUF_CHECK_INTERVAL)
                    {
                        cutByHalf();
                        tooMuchBufferingCount = 0;
                    }
                } else
                if(tot >= size / 2 && size < maxPktsToBuffer)
                {
                    pktsToBuffer = size + size / 2;
                    if(pktsToBuffer > maxPktsToBuffer)
                        pktsToBuffer = maxPktsToBuffer;
                    grow(pktsToBuffer + FUDGE);
                    Log.comment("RTP video buffer size: " + size + " pkts, " + pktsToBuffer * sizePerPkt + " bytes.\n");
                    tooMuchBufferingCount = 0;
                } else
                {
                    tooMuchBufferingCount = 0;
                }
                int sizeToBuffer = (pktsToBuffer * sizePerPkt) / 2;
                if(rtpr != null && sizeToBuffer > sockBufSize)
                {
                    rtpr.setRecvBufSize(sizeToBuffer);
                    if(rtpr.getRecvBufSize() < sizeToBuffer)
                        sockBufSize = 0x7fffffff;
                    else
                        sockBufSize = sizeToBuffer;
                    Log.comment("RTP video socket buffer size: " + rtpr.getRecvBufSize() + " bytes.\n");
                }
            } else
            if(format instanceof AudioFormat)
            {
                if(sizePerPkt <= 0)
                    sizePerPkt = DEFAULT_AUD_PKT_SIZE;
                if(bc != null)
                {
                    int lenPerPkt;
                    if(RTPSourceStream.mpegAudio.matches(format))
                        lenPerPkt = sizePerPkt / 4;
                    else
                        lenPerPkt = DEFAULT_MILLISECS_PER_PKT;
                    int pktsToBuffer = (int)(bc.getBufferLength() / (long)lenPerPkt);
                    threshold = (int)(bc.getMinimumThreshold() / (long)lenPerPkt);
                    if(threshold <= pktsToBuffer / 2);
                    threshold = pktsToBuffer / 2;
                    if(pktsToBuffer > size)
                    {
                        grow(pktsToBuffer);
                        Log.comment("RTP audio buffer size: " + size + " pkts, " + pktsToBuffer * sizePerPkt + " bytes.\n");
                    }
                    int sizeToBuffer = (pktsToBuffer * sizePerPkt) / 2;
                    if(rtpr != null && sizeToBuffer > sockBufSize)
                    {
                        rtpr.setRecvBufSize(sizeToBuffer);
                        if(rtpr.getRecvBufSize() < sizeToBuffer)
                            sockBufSize = 0x7fffffff;
                        else
                            sockBufSize = sizeToBuffer;
                        Log.comment("RTP audio socket buffer size: " + rtpr.getRecvBufSize() + " bytes.\n");
                    }
                }
            }
        }

        public synchronized Buffer getPkt()
        {
            while(!moreFilled()) 
                try
                {
                    wait();
                }
                catch(Exception e) { }
            Buffer b = get();
            return b;
        }

        public void dropPkt()
        {
            while(!moreFilled()) 
                try
                {
                    wait();
                }
                catch(Exception e) { }
            if(format instanceof AudioFormat)
                dropFirstPkt();
            else
            if(RTPSourceStream.mpegVideo.matches(format))
                dropMpegPkt();
            else
                dropFirstPkt();
        }

        public synchronized void dropFirstPkt()
        {
            Buffer buf = get();
            lastSeqSent = buf.getSequenceNumber();
            returnFree(buf);
        }

        public synchronized void dropMpegPkt()
        {
            int i = headFill;
            int firstP = -1;
            int firstB = -1;
            Buffer buf;
            while(i != tailFill) 
            {
                buf = fill[i];
                byte payload[] = (byte[])buf.getData();
                int offset = buf.getOffset();
                int ptype = payload[offset + 2] & 7;
                if(ptype > 2)
                {
                    firstB = i;
                    break;
                }
                if(ptype == 2 && firstP == -1)
                    firstP = i;
                if(++i >= size)
                    i = 0;
            }
            if(firstB == -1)
                i = firstP != -1 ? firstP : headFill;
            buf = fill[i];
            if(i == 0)
                lastSeqSent = buf.getSequenceNumber();
            removeAt(i);
        }

        public synchronized long getFirstSeq()
        {
            if(!moreFilled())
                return -1L;
            else
                return fill[headFill].getSequenceNumber();
        }

        private void allocBuffers(int n)
        {
            fill = new Buffer[n];
            free = new Buffer[n];
            for(int i = 0; i < n - 1; i++)
                free[i] = new Buffer();

            size = n;
            headFill = tailFill = 0;
            headFree = 0;
            tailFree = size - 1;
        }

        private synchronized void grow(int newSize)
        {
            Buffer newFill[] = new Buffer[newSize];
            Buffer newFree[] = new Buffer[newSize];
            int totPkts = totalPkts();
            int totFree = totalFree();
            int i = headFill;
            for(int j = 0; i != tailFill; j++)
            {
                newFill[j] = fill[i];
                if(++i >= size)
                    i = 0;
            }

            headFill = 0;
            tailFill = totPkts;
            fill = newFill;
            i = headFree;
            for(int j = 0; i != tailFree; j++)
            {
                newFree[j] = free[i];
                if(++i >= size)
                    i = 0;
            }

            headFree = 0;
            tailFree = totFree;
            for(i = newSize - size; i > 0; i--)
            {
                newFree[tailFree] = new Buffer();
                tailFree++;
            }

            free = newFree;
            size = newSize;
        }

        private synchronized void cutByHalf()
        {
            int newSize = size / 2;
            if(newSize <= 0)
                return;
            Buffer newFill[] = new Buffer[size / 2];
            Buffer newFree[] = new Buffer[size / 2];
            int tot = totalPkts();
            int i;
            for(i = 0; i < newSize && i < tot; i++)
                newFill[i] = get();

            tot = newSize - i - (size - tot - totalFree());
            headFill = 0;
            tailFill = i;
            for(i = 0; i <= tot; i++)
                newFree[i] = new Buffer();

            headFree = 0;
            tailFree = tot;
            fill = newFill;
            free = newFree;
            size = newSize;
        }

        private synchronized Buffer get()
        {
            Buffer b = fill[headFill];
            fill[headFill] = null;
            headFill++;
            if(headFill >= size)
                headFill = 0;
            return b;
        }

        private synchronized void append(Buffer b)
        {
            fill[tailFill] = b;
            tailFill++;
            if(tailFill >= size)
                tailFill = 0;
        }

        private synchronized void prepend(Buffer b)
        {
            headFill--;
            if(headFill < 0)
                headFill = 0;
            fill[headFill] = b;
        }

        private synchronized void insert(Buffer b)
        {
            int i;
            for(i = headFill; i != tailFill;)
            {
                if(fill[i].getSequenceNumber() > b.getSequenceNumber())
                    break;
                if(++i >= size)
                    i = 0;
            }

            if(i != tailFill)
            {
                tailFill++;
                if(tailFill >= size)
                    tailFill = 0;
                int prev;
                int j = prev = tailFill;
                do
                {
                    if(--prev < 0)
                        prev = size - 1;
                    fill[j] = fill[prev];
                    j = prev;
                } while(j != i);
                fill[i] = b;
            }
        }

        private void removeAt(int n)
        {
            Buffer buf = fill[n];
            if(n == headFill)
            {
                headFill++;
                if(headFill >= size)
                    headFill = 0;
            } else
            if(n == tailFill)
            {
                tailFill--;
                if(tailFill < 0)
                    tailFill = size - 1;
            } else
            {
                int prev = n;
                do
                {
                    if(--prev < 0)
                        prev = size - 1;
                    fill[n] = fill[prev];
                    n = prev;
                } while(n != headFill);
                headFill++;
                if(headFill >= size)
                    headFill = 0;
            }
            returnFree(buf);
        }

        private boolean moreFilled()
        {
            return headFill != tailFill;
        }

        public synchronized Buffer getFree()
        {
            Buffer b = free[headFree];
            free[headFree] = null;
            headFree++;
            if(headFree >= size)
                headFree = 0;
            return b;
        }

        private synchronized void returnFree(Buffer b)
        {
            free[tailFree] = b;
            tailFree++;
            if(tailFree >= size)
                tailFree = 0;
        }

        private boolean noMoreFree()
        {
            return headFree == tailFree;
        }

        int FUDGE;
        int DEFAULT_AUD_PKT_SIZE;
        int DEFAULT_MILLISECS_PER_PKT;
        int DEFAULT_PKTS_TO_BUFFER;
        int MIN_BUF_CHECK;
        int BUF_CHECK_INTERVAL;
        int pktsEst;
        int framesEst;
        int fps;
        int pktsPerFrame;
        int sizePerPkt;
        int maxPktsToBuffer;
        int sockBufSize;
        int tooMuchBufferingCount;
        long lastPktSeq;
        long lastCheckTime;
        Buffer fill[];
        Buffer free[];
        int headFill;
        int tailFill;
        int headFree;
        int tailFree;
        protected int size;



        public PktQue(int n)
        {
            FUDGE = 5;
            DEFAULT_AUD_PKT_SIZE = 256;
            DEFAULT_MILLISECS_PER_PKT = 30;
            DEFAULT_PKTS_TO_BUFFER = 30;
            MIN_BUF_CHECK = 10;
            BUF_CHECK_INTERVAL = 7;
            framesEst = 0;
            fps = 15;
            pktsPerFrame = DEFAULT_VIDEO_RATE;
            sizePerPkt = DEFAULT_AUD_PKT_SIZE;
            maxPktsToBuffer = 0;
            sockBufSize = 0;
            tooMuchBufferingCount = 0;
            lastPktSeq = 0L;
            lastCheckTime = 0L;
            allocBuffers(n);
        }
    }


    public RTPSourceStream(DataSource dsource)
    {
        format = null;
        handler = null;
        started = false;
        killed = false;
        replenish = true;
        startReq = new Object();
        thread = null;
        hasRead = false;
        DEFAULT_AUDIO_RATE = 8000;
        DEFAULT_VIDEO_RATE = 15;
        bc = null;
        lastSeqRecv = -1L;
        lastSeqSent = -1L;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        listener = null;
        threshold = 0;
        prebuffering = false;
        prebufferNotice = false;
        bufferWhenStopped = true;
        this.dsource = dsource;
        dsource.setSourceStream(this);
        pktQ = new PktQue(4);
        createThread();
    }

    public void setBufferControl(BufferControl b)
    {
        bc = (BufferControlImpl)b;
        updateBuffer(bc.getBufferLength());
        updateThreshold(bc.getMinimumThreshold());
    }

    public long updateBuffer(long len)
    {
        return len;
    }

    public long updateThreshold(long threshold)
    {
        return threshold;
    }

    public void setBufferListener(BufferListener listener)
    {
        this.listener = listener;
    }

    public void add(Buffer filled, boolean wrapped, RTPRawReceiver rtpr)
    {
        if(!started && !bufferWhenStopped)
            return;
        if(lastSeqRecv - filled.getSequenceNumber() > 256L)
            pktQ.reset();
        lastSeqRecv = filled.getSequenceNumber();
        boolean overflown = false;
        synchronized(pktQ)
        {
            pktQ.monitorQueueSize(filled, rtpr);
            if(pktQ.noMoreFree())
            {
                long head = pktQ.getFirstSeq();
                if(head != -1L && filled.getSequenceNumber() < head)
                    return;
                pktQ.dropPkt();
            }
        }
        if(pktQ.totalFree() <= 1)
            overflown = true;
        Buffer buf = pktQ.getFree();
        byte inData[] = (byte[])filled.getData();
        byte outData[] = (byte[])buf.getData();
        if(outData == null || outData.length < inData.length)
            outData = new byte[inData.length];
        System.arraycopy(inData, filled.getOffset(), outData, filled.getOffset(), filled.getLength());
        buf.copy(filled);
        buf.setData(outData);
        if(overflown)
            buf.setFlags(buf.getFlags() | 0x2000 | 0x20);
        else
            buf.setFlags(buf.getFlags() | 0x20);
        pktQ.addPkt(buf);
        synchronized(pktQ)
        {
            if(started && prebufferNotice && listener != null && pktQ.totalPkts() >= threshold)
            {
                listener.minThresholdReached(dsource);
                prebufferNotice = false;
                prebuffering = false;
                synchronized(startReq)
                {
                    startReq.notifyAll();
                }
            }
            if(replenish && (format instanceof AudioFormat))
            {
                if(pktQ.totalPkts() >= pktQ.size / 2)
                {
                    replenish = false;
                    pktQ.notifyAll();
                }
            } else
            {
                pktQ.notifyAll();
            }
        }
    }

    public void read(Buffer buf)
    {
        if(pktQ.totalPkts() == 0)
        {
            buf.setDiscard(true);
            return;
        }
        Buffer pkt = pktQ.getPkt();
        lastSeqSent = pkt.getSequenceNumber();
        Object data = buf.getData();
        Object hdr = buf.getHeader();
        buf.copy(pkt);
        pkt.setData(data);
        pkt.setHeader(hdr);
        pktQ.returnFree(pkt);
        synchronized(pktQ)
        {
            hasRead = true;
            if(format instanceof AudioFormat)
            {
                if(pktQ.totalPkts() > 0)
                    pktQ.notifyAll();
                else
                    replenish = true;
            } else
            {
                pktQ.notifyAll();
            }
        }
    }

    public void reset()
    {
        pktQ.reset();
        lastSeqSent = -1L;
    }

    public Format getFormat()
    {
        return format;
    }

    protected void setFormat(Format format)
    {
        this.format = format;
    }

    public void setTransferHandler(BufferTransferHandler transferHandler)
    {
        handler = transferHandler;
    }

    void setContentDescriptor(String contentType)
    {
        super.contentDescriptor = new ContentDescriptor(contentType);
    }

    public void setBufferWhenStopped(boolean flag)
    {
        bufferWhenStopped = flag;
    }

    public void prebuffer()
    {
        synchronized(pktQ)
        {
            prebuffering = true;
            prebufferNotice = true;
        }
    }

    public void start()
    {
        synchronized(startReq)
        {
            started = true;
            startReq.notifyAll();
        }
    }

    public void stop()
    {
        synchronized(startReq)
        {
            started = false;
            prebuffering = false;
            if(!bufferWhenStopped)
                reset();
        }
    }

    public void connect()
    {
        killed = false;
        createThread();
    }

    public void close()
    {
        if(killed)
            return;
        stop();
        killed = true;
        synchronized(startReq)
        {
            startReq.notifyAll();
        }
        synchronized(pktQ)
        {
            pktQ.notifyAll();
        }
        thread = null;
        if(bc != null)
            bc.removeSourceStream(this);
    }

    public void run()
    {
        while(true) 
            try
            {
                synchronized(startReq)
                {
                    while((!started || prebuffering) && !killed) 
                        startReq.wait();
                }
                synchronized(pktQ)
                {
                    do
                    {
                        if(!hasRead && !killed)
                            pktQ.wait();
                        hasRead = false;
                    } while(pktQ.totalPkts() <= 0 && !killed);
                }
                if(killed)
                    break;
                if(handler != null)
                    handler.transferData(this);
            }
            catch(InterruptedException e)
            {
                Log.error("Thread " + e.getMessage());
            }
    }

    private void createThread()
    {
        if(thread != null)
            return;
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
                thread = (RTPMediaThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.rtp.util.RTPMediaThread.class, this
                    })
                });
                thread.setName("RTPStream");
                cons = jdk12PriorityAction.cons;
                jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        thread, new Integer(MediaThread.getControlPriority())
                    })
                });
            }
            catch(Exception e) { }
        } else
        {
            thread = new RTPMediaThread(this, "RTPStream");
            thread.useControlPriority();
        }
        thread.start();
    }

    private DataSource dsource;
    private Format format;
    BufferTransferHandler handler;
    boolean started;
    boolean killed;
    boolean replenish;
    PktQue pktQ;
    Object startReq;
    private RTPMediaThread thread;
    private boolean hasRead;
    private int DEFAULT_AUDIO_RATE;
    private int DEFAULT_VIDEO_RATE;
    private BufferControlImpl bc;
    private long lastSeqRecv;
    private long lastSeqSent;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    private static final int NOT_SPECIFIED = -1;
    private BufferListener listener;
    private int threshold;
    private boolean prebuffering;
    private boolean prebufferNotice;
    private boolean bufferWhenStopped;
    static AudioFormat mpegAudio = new AudioFormat("mpegaudio/rtp");
    static VideoFormat mpegVideo = new VideoFormat("mpeg/rtp");

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
