// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicRendererModule.java

package com.sun.media;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.renderer.audio.AudioRenderer;
import com.sun.media.rtp.util.RTPTimeBase;
import com.sun.media.rtp.util.RTPTimeReporter;
import com.sun.media.util.ElapseTime;
import com.sun.media.util.LoopThread;
import com.sun.media.util.jdk12;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.renderer.VideoRenderer;

// Referenced classes of package com.sun.media:
//            BasicSinkModule, BasicInputConnector, RenderThread, PlaybackEngine, 
//            Connector, BasicModule, JMFSecurity, CreateWorkThreadAction, 
//            SimpleGraphBuilder, ModuleListener, InputConnector, Log, 
//            JMD, JMFSecurityManager

public class BasicRendererModule extends BasicSinkModule
    implements RTPTimeReporter
{

    protected BasicRendererModule(Renderer r)
    {
        framesPlayed = 0;
        frameRate = 30F;
        framesWereBehind = false;
        prefetching = false;
        started = false;
        opened = false;
        chunkSize = 0x7fffffff;
        prefetchedAudioDuration = 0L;
        lastDuration = 0L;
        rtpTimeBase = null;
        rtpCNAME = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        prefetchSync = new Object();
        elapseTime = new ElapseTime();
        LEEWAY = 10L;
        lastRendered = 0L;
        failed = false;
        notToDropNext = false;
        storedBuffer = null;
        checkRTP = false;
        noSync = false;
        overMsg = false;
        overflown = 10;
        rate = 1.0F;
        systemErr = 0L;
        rtpErrMsg = false;
        ulawFormat = new AudioFormat("ULAW");
        linearFormat = new AudioFormat("LINEAR");
        setRenderer(r);
        ic = new BasicInputConnector();
        if(r instanceof VideoRenderer)
            ic.setSize(4);
        else
            ic.setSize(1);
        ic.setModule(this);
        registerInputConnector("input", ic);
        setProtocol(1);
    }

    public boolean isThreaded()
    {
        return true;
    }

    public boolean doRealize()
    {
        chunkSize = computeChunkSize(ic.getFormat());
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
                securityPrivelege = false;
            }
        }
        if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            try
            {
                Constructor cons = CreateWorkThreadAction.cons;
                renderThread = (RenderThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.RenderThread.class, com.sun.media.BasicRendererModule.class, this
                    })
                });
            }
            catch(Exception e) { }
        else
            renderThread = new RenderThread(this);
        engine = (PlaybackEngine)getController();
        return true;
    }

    public boolean doPrefetch()
    {
        super.doPrefetch();
        if(!opened)
        {
            try
            {
                renderer.open();
            }
            catch(ResourceUnavailableException e)
            {
                super.prefetchFailed = true;
                return false;
            }
            super.prefetchFailed = false;
            opened = true;
        }
        if(!((PlaybackEngine)super.controller).prefetchEnabled)
        {
            return true;
        } else
        {
            prefetching = true;
            renderThread.start();
            return true;
        }
    }

    public void doFailedPrefetch()
    {
        renderThread.pause();
        renderer.close();
        opened = false;
        prefetching = false;
    }

    public void abortPrefetch()
    {
        renderThread.pause();
        renderer.close();
        prefetching = false;
        opened = false;
    }

    public void doStart()
    {
        super.doStart();
        if(!(renderer instanceof Clock))
            renderer.start();
        super.prerolling = false;
        started = true;
        synchronized(prefetchSync)
        {
            prefetching = false;
            renderThread.start();
        }
    }

    public void doStop()
    {
        started = false;
        prefetching = true;
        super.doStop();
        if(renderer != null && !(renderer instanceof Clock))
            renderer.stop();
    }

    public void doDealloc()
    {
        renderer.close();
    }

    public void doClose()
    {
        renderThread.kill();
        if(renderer != null)
            renderer.close();
        if(rtpTimeBase != null)
        {
            RTPTimeBase.remove(this, rtpCNAME);
            rtpTimeBase = null;
        }
    }

    public void reset()
    {
        super.reset();
        prefetching = false;
    }

    public void triggerReset()
    {
        if(renderer != null)
            renderer.reset();
        synchronized(prefetchSync)
        {
            prefetching = false;
            if(super.resetted)
                renderThread.start();
        }
    }

    public void doneReset()
    {
        renderThread.pause();
    }

    protected boolean reinitRenderer(Format input)
    {
        if(renderer != null && renderer.setInputFormat(input) != null)
            return true;
        if(started)
        {
            renderer.stop();
            renderer.reset();
        }
        renderer.close();
        renderer = null;
        Renderer r;
        if((r = SimpleGraphBuilder.findRenderer(input)) == null)
            return false;
        setRenderer(r);
        if(started)
            renderer.start();
        chunkSize = computeChunkSize(input);
        return true;
    }

    protected void setRenderer(Renderer r)
    {
        renderer = r;
        if(renderer instanceof Clock)
            setClock((Clock)renderer);
    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    protected void process()
    {
    }

    protected boolean doProcess()
    {
        if((started || prefetching) && super.stopTime > -1L && elapseTime.value >= super.stopTime)
        {
            if(renderer instanceof Drainable)
                ((Drainable)renderer).drain();
            doStop();
            if(super.moduleListener != null)
                super.moduleListener.stopAtTime(this);
        }
        Buffer buffer;
        if(storedBuffer != null)
            buffer = storedBuffer;
        else
            buffer = ic.getValidBuffer();
        if(!checkRTP)
            if((buffer.getFlags() & 0x1000) != 0)
            {
                String key = engine.getCNAME();
                if(key != null)
                {
                    rtpTimeBase = RTPTimeBase.find(this, key);
                    rtpCNAME = key;
                    if(ic.getFormat() instanceof AudioFormat)
                    {
                        Log.comment("RTP master time set: " + renderer + "\n");
                        rtpTimeBase.setMaster(this);
                    }
                    checkRTP = true;
                    noSync = false;
                } else
                {
                    noSync = true;
                }
            } else
            {
                checkRTP = true;
            }
        lastTimeStamp = buffer.getTimeStamp();
        if(failed || super.resetted)
        {
            if((buffer.getFlags() & 0x200) != 0)
            {
                super.resetted = false;
                renderThread.pause();
                if(super.moduleListener != null)
                    super.moduleListener.resetted(this);
            }
            storedBuffer = null;
            ic.readReport();
            return true;
        }
        if(PlaybackEngine.DEBUG)
            super.jmd.moduleIn(this, 0, buffer, true);
        boolean rtn = scheduleBuffer(buffer);
        if(storedBuffer == null && buffer.isEOM())
        {
            if(prefetching)
                donePrefetch();
            if((buffer.getFlags() & 0x40) == 0 && buffer.getTimeStamp() > 0L && buffer.getDuration() > 0L && buffer.getFormat() != null && !(buffer.getFormat() instanceof AudioFormat) && !noSync)
                waitForPT(buffer.getTimeStamp() + lastDuration);
            storedBuffer = null;
            ic.readReport();
            if(PlaybackEngine.DEBUG)
                super.jmd.moduleIn(this, 0, buffer, false);
            doStop();
            if(super.moduleListener != null)
                super.moduleListener.mediaEnded(this);
            return true;
        }
        if(storedBuffer == null)
            ic.readReport();
        if(PlaybackEngine.DEBUG)
            super.jmd.moduleIn(this, 0, buffer, false);
        return rtn;
    }

    protected boolean scheduleBuffer(Buffer buf)
    {
        int rc = 0;
        Format format = buf.getFormat();
        if(format == null)
        {
            format = ic.getFormat();
            buf.setFormat(format);
        }
        if(format != ic.getFormat() && !format.equals(ic.getFormat()) && !buf.isDiscard() && !handleFormatChange(format))
            return false;
        if((buf.getFlags() & 0x400) != 0 && super.moduleListener != null)
        {
            super.moduleListener.markedDataArrived(this, buf);
            buf.setFlags(buf.getFlags() & 0xfffffbff);
        }
        if(prefetching || (format instanceof AudioFormat) || buf.getTimeStamp() <= 0L || (buf.getFlags() & 0x60) == 96 || noSync)
        {
            if(!buf.isDiscard())
                rc = processBuffer(buf);
        } else
        {
            long mt = getSyncTime(buf.getTimeStamp());
            long lateBy = mt / 0xf4240L - buf.getTimeStamp() / 0xf4240L - getLatency() / 0xf4240L;
            if(storedBuffer == null && lateBy > 0L)
            {
                if(buf.isDiscard())
                {
                    notToDropNext = true;
                } else
                {
                    if(buf.isEOM())
                        notToDropNext = true;
                    else
                    if(super.moduleListener != null && (format instanceof VideoFormat))
                    {
                        float fb = ((float)lateBy * frameRate) / 1000F;
                        if(fb < 1.0F)
                            fb = 1.0F;
                        super.moduleListener.framesBehind(this, fb, ic);
                        framesWereBehind = true;
                    }
                    if((buf.getFlags() & 0x20) != 0)
                        rc = processBuffer(buf);
                    else
                    if(lateBy < LEEWAY || notToDropNext || buf.getTimeStamp() - lastRendered > 0x3b9aca00L)
                    {
                        rc = processBuffer(buf);
                        lastRendered = buf.getTimeStamp();
                        notToDropNext = false;
                    }
                }
            } else
            {
                if(super.moduleListener != null && framesWereBehind && (format instanceof VideoFormat))
                {
                    super.moduleListener.framesBehind(this, 0.0F, ic);
                    framesWereBehind = false;
                }
                if(!buf.isDiscard())
                {
                    if((buf.getFlags() & 0x40) == 0)
                        waitForPT(buf.getTimeStamp());
                    if(!super.resetted)
                    {
                        rc = processBuffer(buf);
                        lastRendered = buf.getTimeStamp();
                    }
                }
            }
        }
        if((rc & 1) != 0)
            storedBuffer = null;
        else
        if((rc & 2) != 0)
        {
            storedBuffer = buf;
        } else
        {
            storedBuffer = null;
            if(buf.getDuration() >= 0L)
                lastDuration = buf.getDuration();
        }
        return true;
    }

    public int processBuffer(Buffer buffer)
    {
        int remain = buffer.getLength();
        int offset = buffer.getOffset();
        int rc = 0;
        boolean isEOM = false;
        if(renderer instanceof Clock)
        {
            if((buffer.getFlags() & 0x2000) != 0)
                overflown++;
            else
                overflown--;
            if(overflown > 20)
            {
                if(rate < 1.05F)
                {
                    rate += 0.01F;
                    renderer.stop();
                    ((Clock)renderer).setRate(rate);
                    renderer.start();
                    if(!overMsg)
                    {
                        Log.comment("Data buffers overflown.  Adjust rendering speed up to 5 % to compensate");
                        overMsg = true;
                    }
                }
                overflown = 10;
            } else
            if(overflown <= 0)
            {
                if(rate > 1.0F)
                {
                    rate -= 0.01F;
                    renderer.stop();
                    ((Clock)renderer).setRate(rate);
                    renderer.start();
                }
                overflown = 10;
            }
        }
        do
        {
            if(super.stopTime > -1L && elapseTime.value >= super.stopTime)
            {
                if(prefetching)
                    donePrefetch();
                return 2;
            }
            int len;
            if(remain <= chunkSize || super.prerolling)
            {
                if(isEOM)
                {
                    isEOM = false;
                    buffer.setEOM(true);
                }
                len = remain;
            } else
            {
                if(buffer.isEOM())
                {
                    isEOM = true;
                    buffer.setEOM(false);
                }
                len = chunkSize;
            }
            buffer.setLength(len);
            buffer.setOffset(offset);
            if(super.prerolling && !handlePreroll(buffer))
            {
                offset += len;
                remain -= len;
                continue;
            }
            try
            {
                rc = renderer.process(buffer);
            }
            catch(Throwable e)
            {
                Log.dumpStack(e);
                if(super.moduleListener != null)
                    super.moduleListener.internalErrorOccurred(this);
            }
            if((rc & 8) != 0)
            {
                failed = true;
                if(super.moduleListener != null)
                    super.moduleListener.pluginTerminated(this);
                return rc;
            }
            if((rc & 1) != 0)
            {
                buffer.setDiscard(true);
                if(prefetching)
                    donePrefetch();
                return rc;
            }
            if((rc & 2) != 0)
                len -= buffer.getLength();
            offset += len;
            remain -= len;
            if(prefetching && (!(renderer instanceof Prefetchable) || ((Prefetchable)renderer).isPrefetched()))
            {
                isEOM = false;
                buffer.setEOM(false);
                donePrefetch();
                break;
            }
            elapseTime.update(len, buffer.getTimeStamp(), buffer.getFormat());
        } while(remain > 0 && !super.resetted);
        if(isEOM)
            buffer.setEOM(true);
        buffer.setLength(remain);
        buffer.setOffset(offset);
        if(rc == 0)
            framesPlayed++;
        return rc;
    }

    private boolean handleFormatChange(Format format)
    {
        if(!reinitRenderer(format))
        {
            storedBuffer = null;
            failed = true;
            if(super.moduleListener != null)
                super.moduleListener.formatChangedFailure(this, ic.getFormat(), format);
            return false;
        }
        Format oldFormat = ic.getFormat();
        ic.setFormat(format);
        if(super.moduleListener != null)
            super.moduleListener.formatChanged(this, oldFormat, format);
        if(format instanceof VideoFormat)
        {
            float fr = ((VideoFormat)format).getFrameRate();
            if(fr != -1F)
                frameRate = fr;
        }
        return true;
    }

    private void donePrefetch()
    {
        synchronized(prefetchSync)
        {
            if(!started && prefetching)
                renderThread.pause();
            prefetching = false;
        }
        if(super.moduleListener != null)
            super.moduleListener.bufferPrefetched(this);
    }

    public void setPreroll(long wanted, long actual)
    {
        super.setPreroll(wanted, actual);
        elapseTime.setValue(actual);
    }

    protected boolean handlePreroll(Buffer buf)
    {
        if(buf.getFormat() instanceof AudioFormat)
        {
            if(!hasReachAudioPrerollTarget(buf))
                return false;
        } else
        if((buf.getFlags() & 0x60) == 0 && buf.getTimeStamp() >= 0L && buf.getTimeStamp() < getSyncTime(buf.getTimeStamp()))
            return false;
        super.prerolling = false;
        return true;
    }

    private boolean hasReachAudioPrerollTarget(Buffer buf)
    {
        long target = getSyncTime(buf.getTimeStamp());
        elapseTime.update(buf.getLength(), buf.getTimeStamp(), buf.getFormat());
        if(elapseTime.value >= target)
        {
            long remain = ElapseTime.audioTimeToLen(elapseTime.value - target, (AudioFormat)buf.getFormat());
            int offset = (buf.getOffset() + buf.getLength()) - (int)remain;
            if(offset >= 0)
            {
                buf.setOffset(offset);
                buf.setLength((int)remain);
            }
            elapseTime.setValue(target);
            return true;
        } else
        {
            return false;
        }
    }

    private boolean waitForPT(long pt)
    {
        long mt = getSyncTime(pt);
        long lastAheadBy = -1L;
        int beenHere = 0;
        long aheadBy = (pt - mt) / 0xf4240L;
        if(rate != 1.0F)
            aheadBy = (long)((float)aheadBy / rate);
        while(aheadBy > systemErr && !super.resetted) 
        {
            long interval;
            if(aheadBy == lastAheadBy)
            {
                interval = aheadBy + (long)(5 * beenHere);
                if(interval > 33L)
                    interval = 33L;
                else
                    beenHere++;
            } else
            {
                interval = aheadBy;
                beenHere = 0;
            }
            interval = interval <= 125L ? interval : 125L;
            long before = System.currentTimeMillis();
            interval -= systemErr;
            try
            {
                if(interval > 0L)
                {
                    Thread.currentThread();
                    Thread.sleep(interval);
                }
            }
            catch(InterruptedException e) { }
            long slept = System.currentTimeMillis() - before;
            systemErr = ((slept - interval) + systemErr) / 2L;
            if(systemErr < 0L)
                systemErr = 0L;
            else
            if(systemErr > interval)
                systemErr = interval;
            mt = getSyncTime(pt);
            lastAheadBy = aheadBy;
            aheadBy = (pt - mt) / 0xf4240L;
            if(rate != 1.0F)
                aheadBy = (long)((float)aheadBy / rate);
            if(getState() != 600)
                break;
        }
        return true;
    }

    private long getSyncTime(long pts)
    {
        if(rtpTimeBase != null)
        {
            if(rtpTimeBase.getMaster() == getController())
                return pts;
            long ts = rtpTimeBase.getNanoseconds();
            if(ts > pts + 0x77359400L || ts < pts - 0x77359400L)
            {
                if(!rtpErrMsg)
                {
                    Log.comment("Cannot perform RTP sync beyond a difference of: " + (ts - pts) / 0xf4240L + " msecs.\n");
                    rtpErrMsg = true;
                }
                return pts;
            } else
            {
                return ts;
            }
        } else
        {
            return getMediaNanoseconds();
        }
    }

    public long getRTPTime()
    {
        if(ic.getFormat() instanceof AudioFormat)
        {
            if(renderer instanceof AudioRenderer)
                return lastTimeStamp - ((AudioRenderer)renderer).getLatency();
            else
                return lastTimeStamp;
        } else
        {
            return lastTimeStamp;
        }
    }

    public Object[] getControls()
    {
        return renderer.getControls();
    }

    public Object getControl(String s)
    {
        return renderer.getControl(s);
    }

    public void setFormat(Connector connector, Format format)
    {
        renderer.setInputFormat(format);
        if(format instanceof VideoFormat)
        {
            float fr = ((VideoFormat)format).getFrameRate();
            if(fr != -1F)
                frameRate = fr;
        }
    }

    public int getFramesPlayed()
    {
        return framesPlayed;
    }

    public void resetFramesPlayed()
    {
        framesPlayed = 0;
    }

    private int computeChunkSize(Format format)
    {
        if((format instanceof AudioFormat) && (ulawFormat.matches(format) || linearFormat.matches(format)))
        {
            AudioFormat af = (AudioFormat)format;
            int units = (af.getSampleSizeInBits() * af.getChannels()) / 8;
            if(units == 0)
                units = 1;
            int chunks = ((int)af.getSampleRate() * units) / 16;
            return (chunks / units) * units;
        } else
        {
            return 0x7fffffff;
        }
    }

    protected PlaybackEngine engine;
    protected Renderer renderer;
    protected InputConnector ic;
    protected int framesPlayed;
    protected float frameRate;
    protected boolean framesWereBehind;
    protected boolean prefetching;
    protected boolean started;
    private boolean opened;
    private int chunkSize;
    private long prefetchedAudioDuration;
    private long lastDuration;
    private RTPTimeBase rtpTimeBase;
    private String rtpCNAME;
    RenderThread renderThread;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    private Object prefetchSync;
    private ElapseTime elapseTime;
    private long LEEWAY;
    private long lastRendered;
    private boolean failed;
    private boolean notToDropNext;
    private Buffer storedBuffer;
    private boolean checkRTP;
    private boolean noSync;
    final float MAX_RATE = 1.05F;
    final float RATE_INCR = 0.01F;
    final int FLOW_LIMIT = 20;
    boolean overMsg;
    int overflown;
    float rate;
    long systemErr;
    static final long RTP_TIME_MARGIN = 0x77359400L;
    boolean rtpErrMsg;
    long lastTimeStamp;
    static final int MAX_CHUNK_SIZE = 16;
    AudioFormat ulawFormat;
    AudioFormat linearFormat;

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
