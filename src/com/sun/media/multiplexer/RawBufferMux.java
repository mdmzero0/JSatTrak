// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RawBufferMux.java

package com.sun.media.multiplexer;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.*;
import com.sun.media.controls.MonitorAdapter;
import com.sun.media.protocol.BasicPushBufferDataSource;
import com.sun.media.protocol.BasicSourceStream;
import com.sun.media.util.*;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

public class RawBufferMux extends BasicPlugIn
    implements Multiplexer, Clock
{
    class RawBufferSourceStream extends BasicSourceStream
        implements PushBufferStream, Runnable
    {

        public Format getFormat()
        {
            return format;
        }

        public void setTransferHandler(BufferTransferHandler handler)
        {
            this.handler = handler;
        }

        public void read(Buffer buffer)
            throws IOException
        {
            if(closed)
                throw new IOException("The source stream is closed");
            Buffer current = null;
            synchronized(bufferQ)
            {
                while(!bufferQ.canRead()) 
                    try
                    {
                        bufferQ.wait();
                    }
                    catch(Exception e) { }
                current = bufferQ.read();
            }
            if(current.isEOM())
                synchronized(drainSync)
                {
                    if(draining)
                    {
                        draining = false;
                        drainSync.notifyAll();
                    }
                }
            Object data = buffer.getData();
            Object hdr = buffer.getHeader();
            buffer.copy(current);
            current.setData(data);
            current.setHeader(hdr);
            synchronized(bufferQ)
            {
                hasRead = true;
                bufferQ.readReport();
                bufferQ.notifyAll();
            }
        }

        protected void start()
        {
            synchronized(startReq)
            {
                if(started)
                    return;
                started = true;
                startReq.notifyAll();
            }
            synchronized(bufferQ)
            {
                hasRead = true;
                bufferQ.notifyAll();
            }
        }

        protected void stop()
        {
            synchronized(startReq)
            {
                started = false;
            }
            synchronized(bufferQ)
            {
                bufferQ.notifyAll();
            }
        }

        protected void close()
        {
            closed = true;
            if(streamThread != null)
                try
                {
                    reset();
                    synchronized(startReq)
                    {
                        startReq.notifyAll();
                    }
                }
                catch(Exception e) { }
        }

        protected void reset()
        {
            synchronized(bufferQ)
            {
                for(; bufferQ.canRead(); bufferQ.readReport())
                {
                    Buffer b = bufferQ.read();
                }

                bufferQ.notifyAll();
            }
            synchronized(drainSync)
            {
                if(draining)
                {
                    draining = false;
                    drainSync.notifyAll();
                }
            }
        }

        protected int process(Buffer filled)
        {
            Buffer buffer;
            synchronized(bufferQ)
            {
                if(allowDrop && !bufferQ.canWrite() && bufferQ.canRead())
                {
                    Buffer tmp = bufferQ.peek();
                    if((tmp.getFlags() & 0x20) == 0)
                    {
                        bufferQ.read();
                        bufferQ.readReport();
                    }
                }
                while(!bufferQ.canWrite() && !closed) 
                    try
                    {
                        bufferQ.wait();
                    }
                    catch(Exception e) { }
                if(closed)
                {
                    int i = 0;
                    return i;
                }
                buffer = bufferQ.getEmptyBuffer();
            }
            Object bdata = buffer.getData();
            Object bheader = buffer.getHeader();
            buffer.setData(filled.getData());
            buffer.setHeader(filled.getHeader());
            filled.setData(bdata);
            filled.setHeader(bheader);
            buffer.setLength(filled.getLength());
            buffer.setEOM(filled.isEOM());
            buffer.setFlags(filled.getFlags());
            buffer.setTimeStamp(filled.getTimeStamp());
            buffer.setFormat(filled.getFormat());
            buffer.setOffset(filled.getOffset());
            buffer.setSequenceNumber(filled.getSequenceNumber());
            if(filled.isEOM())
                draining = true;
            synchronized(bufferQ)
            {
                bufferQ.writeReport();
                bufferQ.notifyAll();
            }
            if(filled.isEOM())
                synchronized(drainSync)
                {
                    try
                    {
                        if(draining)
                            drainSync.wait(3000L);
                    }
                    catch(Exception e) { }
                }
            return 0;
        }

        public void run()
        {
            try
            {
                do
                {
                    do
                    {
                        synchronized(startReq)
                        {
                            while(!started && !closed) 
                                startReq.wait();
                        }
                        synchronized(bufferQ)
                        {
                            do
                            {
                                if(!hasRead)
                                    bufferQ.wait(250L);
                                hasRead = false;
                            } while(!bufferQ.canRead() && !closed && started);
                        }
                        if(closed)
                            return;
                    } while(!started || handler == null);
                    handler.transferData(this);
                } while(true);
            }
            catch(InterruptedException e)
            {
                System.err.println("Thread " + e.getMessage());
            }
        }

        Format format;
        CircularBuffer bufferQ;
        boolean started;
        Object startReq;
        BufferTransferHandler handler;
        Thread streamThread;
        boolean closed;
        boolean draining;
        Object drainSync;

        public RawBufferSourceStream(Format fmt)
        {
            format = null;
            started = false;
            startReq = new Integer(0);
            handler = null;
            streamThread = null;
            closed = false;
            draining = false;
            drainSync = new Object();
            super.contentDescriptor = contentDesc;
            format = fmt;
            bufferQ = new CircularBuffer(5);
            if(RawBufferMux.jmfSecurity != null)
            {
                String permission = null;
                try
                {
                    if(RawBufferMux.jmfSecurity.getName().startsWith("jmf-security"))
                    {
                        permission = "thread";
                        RawBufferMux.jmfSecurity.requestPermission(m, cl, args, 16);
                        m[0].invoke(cl[0], args[0]);
                        permission = "thread group";
                        RawBufferMux.jmfSecurity.requestPermission(m, cl, args, 32);
                        m[0].invoke(cl[0], args[0]);
                    } else
                    if(RawBufferMux.jmfSecurity.getName().startsWith("internet"))
                    {
                        PolicyEngine.checkPermission(PermissionID.THREAD);
                        PolicyEngine.assertPermission(PermissionID.THREAD);
                    }
                }
                catch(Throwable e)
                {
                    RawBufferMux.securityPrivelege = false;
                }
            }
            if(RawBufferMux.jmfSecurity != null && RawBufferMux.jmfSecurity.getName().startsWith("jdk12"))
            {
                RawBufferSourceStream rbss = this;
                try
                {
                    Constructor cons = jdk12CreateThreadRunnableAction.cons;
                    streamThread = (MediaThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            com.sun.media.util.MediaThread.class, this
                        })
                    });
                }
                catch(Exception e) { }
            } else
            {
                streamThread = new MediaThread(this, "RawBufferStream Thread");
            }
            if(streamThread != null)
                streamThread.start();
        }
    }

    class RawBufferDataSource extends BasicPushBufferDataSource
    {

        public PushBufferStream[] getStreams()
        {
            return (PushBufferStream[])streams;
        }

        public void start()
            throws IOException
        {
            super.start();
            for(int i = 0; i < streams.length; i++)
                streams[i].start();

        }

        public void stop()
            throws IOException
        {
            super.stop();
            for(int i = 0; i < streams.length; i++)
                streams[i].stop();

        }

        public void connect()
            throws IOException
        {
            super.connect();
            sourceDisconnected = false;
        }

        public void disconnect()
        {
            super.disconnect();
            sourceDisconnected = true;
            for(int i = 0; i < streams.length; i++)
            {
                streams[i].stop();
                streams[i].close();
            }

        }

        private void initialize(Format trackFormats[])
        {
            streams = new RawBufferSourceStream[trackFormats.length];
            for(int i = 0; i < trackFormats.length; i++)
                streams[i] = new RawBufferSourceStream(trackFormats[i]);

        }


        public RawBufferDataSource()
        {
            if(contentDesc == null)
            {
                return;
            } else
            {
                super.contentType = contentDesc.getContentType();
                return;
            }
        }
    }

    class RawMuxTimeBase extends MediaTimeBase
    {

        public long getMediaTime()
        {
            if(masterTrackID >= 0)
                return mediaTime[masterTrackID];
            if(!updated)
                return ticks;
            if(mediaTime.length == 1)
            {
                ticks = mediaTime[0];
            } else
            {
                ticks = mediaTime[0];
                for(int i = 1; i < mediaTime.length; i++)
                    if(mediaTime[i] < ticks)
                        ticks = mediaTime[i];

            }
            updated = false;
            return ticks;
        }

        public void update()
        {
            updated = true;
        }

        long ticks;
        boolean updated;

        RawMuxTimeBase()
        {
            ticks = 0L;
            updated = false;
        }
    }


    public RawBufferMux()
    {
        supported = null;
        contentDesc = null;
        source = null;
        streams = null;
        clock = null;
        timeBase = null;
        masterTrackID = -1;
        sourceDisconnected = false;
        allowDrop = false;
        hasRead = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        numTracks = 0;
        mc = null;
        timeSetSync = new Object();
        started = false;
        systemStartTime = -1L;
        mediaStartTime = -1L;
        supported = new ContentDescriptor[1];
        supported[0] = new ContentDescriptor("raw");
        timeBase = new RawMuxTimeBase();
        clock = new BasicClock();
        try
        {
            clock.setTimeBase(timeBase);
        }
        catch(Exception e) { }
    }

    public String getName()
    {
        return "Raw Buffer Multiplexer";
    }

    public void open()
        throws ResourceUnavailableException
    {
        initializeTracks(trackFormats);
        if(source == null || source.getStreams() == null)
            throw new ResourceUnavailableException("DataSource and SourceStreams were not created succesfully.");
        try
        {
            source.connect();
        }
        catch(IOException e)
        {
            throw new ResourceUnavailableException(e.getMessage());
        }
        int len = 0;
        mediaTime = new long[trackFormats.length];
        mc = new MonitorAdapter[trackFormats.length];
        for(int i = 0; i < trackFormats.length; i++)
        {
            mediaTime[i] = 0L;
            if((trackFormats[i] instanceof VideoFormat) || (trackFormats[i] instanceof AudioFormat))
            {
                mc[i] = new MonitorAdapter(trackFormats[i], this);
                if(mc[i] != null)
                    len++;
            }
        }

        int j = 0;
        super.controls = new Control[len];
        for(int i = 0; i < mc.length; i++)
            if(mc[i] != null)
                super.controls[j++] = mc[i];

    }

    public void close()
    {
        if(source != null)
        {
            try
            {
                source.stop();
                source.disconnect();
            }
            catch(IOException e) { }
            source = null;
        }
        for(int i = 0; i < mc.length; i++)
            if(mc[i] != null)
                mc[i].close();

    }

    public void reset()
    {
        for(int i = 0; i < streams.length; i++)
        {
            streams[i].reset();
            if(mc[i] != null)
                mc[i].reset();
        }

    }

    public ContentDescriptor[] getSupportedOutputContentDescriptors(Format fmt[])
    {
        return supported;
    }

    public Format[] getSupportedInputFormats()
    {
        return (new Format[] {
            new AudioFormat(null), new VideoFormat(null)
        });
    }

    public DataSource getDataOutput()
    {
        return source;
    }

    public int setNumTracks(int nTracks)
    {
        numTracks = nTracks;
        trackFormats = new Format[nTracks];
        for(int i = 0; i < nTracks; i++)
            trackFormats[i] = null;

        return nTracks;
    }

    public Format setInputFormat(Format input, int trackID)
    {
        if(trackID < numTracks)
            trackFormats[trackID] = input;
        for(int i = 0; i < numTracks; i++)
            if(trackFormats[i] == null)
                return input;

        return input;
    }

    public boolean initializeTracks(Format trackFormats[])
    {
        if(source.getStreams() != null)
        {
            throw new Error("initializeTracks has been called previously. ");
        } else
        {
            source.initialize(trackFormats);
            streams = (RawBufferSourceStream[])source.getStreams();
            return true;
        }
    }

    public int process(Buffer buffer, int trackID)
    {
        if((buffer.getFlags() & 0x1000) != 0)
            buffer.setFlags(buffer.getFlags() & 0xffffefff | 0x100);
        if(mc[trackID] != null && mc[trackID].isEnabled())
            mc[trackID].process(buffer);
        if(streams == null || buffer == null || trackID >= streams.length)
        {
            return 1;
        } else
        {
            updateTime(buffer, trackID);
            return streams[trackID].process(buffer);
        }
    }

    protected void updateTime(Buffer buf, int trackID)
    {
        if(buf.getFormat() instanceof AudioFormat)
        {
            if(mpegAudio.matches(buf.getFormat()))
            {
                if(buf.getTimeStamp() < 0L)
                {
                    if(systemStartTime >= 0L)
                        mediaTime[trackID] = ((mediaStartTime + System.currentTimeMillis()) - systemStartTime) * 0xf4240L;
                } else
                {
                    mediaTime[trackID] = buf.getTimeStamp();
                }
            } else
            {
                long t = ((AudioFormat)buf.getFormat()).computeDuration(buf.getLength());
                if(t >= 0L)
                    mediaTime[trackID] += t;
                else
                    mediaTime[trackID] = buf.getTimeStamp();
            }
        } else
        if(buf.getTimeStamp() < 0L)
        {
            if(systemStartTime >= 0L)
                mediaTime[trackID] = ((mediaStartTime + System.currentTimeMillis()) - systemStartTime) * 0xf4240L;
        } else
        {
            mediaTime[trackID] = buf.getTimeStamp();
        }
        timeBase.update();
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor outputContentDescriptor)
    {
        if(BasicPlugIn.matches(outputContentDescriptor, supported) == null)
        {
            return null;
        } else
        {
            contentDesc = outputContentDescriptor;
            source = new RawBufferDataSource();
            return contentDesc;
        }
    }

    public void setTimeBase(TimeBase master)
        throws IncompatibleTimeBaseException
    {
        if(master != timeBase)
            throw new IncompatibleTimeBaseException();
        else
            return;
    }

    public void syncStart(Time at)
    {
        synchronized(timeSetSync)
        {
            if(started)
                return;
            started = true;
            clock.syncStart(at);
            timeBase.mediaStarted();
            systemStartTime = System.currentTimeMillis();
            mediaStartTime = getMediaNanoseconds() / 0xf4240L;
        }
    }

    public void stop()
    {
        synchronized(timeSetSync)
        {
            if(!started)
                return;
            started = false;
            clock.stop();
            timeBase.mediaStopped();
        }
    }

    public void setStopTime(Time stopTime)
    {
        clock.setStopTime(stopTime);
    }

    public Time getStopTime()
    {
        return clock.getStopTime();
    }

    public void setMediaTime(Time now)
    {
        synchronized(timeSetSync)
        {
            clock.setMediaTime(now);
            for(int i = 0; i < mediaTime.length; i++)
                mediaTime[i] = now.getNanoseconds();

            timeBase.update();
            systemStartTime = System.currentTimeMillis();
            mediaStartTime = now.getNanoseconds() / 0xf4240L;
        }
    }

    public Time getMediaTime()
    {
        return clock.getMediaTime();
    }

    public long getMediaNanoseconds()
    {
        return clock.getMediaNanoseconds();
    }

    public Time getSyncTime()
    {
        return clock.getSyncTime();
    }

    public TimeBase getTimeBase()
    {
        return clock.getTimeBase();
    }

    public Time mapToTimeBase(Time t)
        throws ClockStoppedException
    {
        return clock.mapToTimeBase(t);
    }

    public float getRate()
    {
        return clock.getRate();
    }

    public float setRate(float factor)
    {
        if(factor == clock.getRate())
            return factor;
        else
            return clock.setRate(1.0F);
    }

    protected ContentDescriptor supported[];
    protected ContentDescriptor contentDesc;
    protected RawBufferDataSource source;
    protected RawBufferSourceStream streams[];
    protected BasicClock clock;
    protected RawMuxTimeBase timeBase;
    protected long mediaTime[];
    protected int masterTrackID;
    boolean sourceDisconnected;
    boolean allowDrop;
    boolean hasRead;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    protected int numTracks;
    protected Format trackFormats[];
    protected MonitorAdapter mc[];
    static AudioFormat mpegAudio = new AudioFormat("mpegaudio/rtp");
    Object timeSetSync;
    boolean started;
    long systemStartTime;
    long mediaStartTime;

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
