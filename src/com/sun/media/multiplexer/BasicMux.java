// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicMux.java

package com.sun.media.multiplexer;

import com.sun.media.*;
import com.sun.media.controls.MonitorAdapter;
import com.sun.media.datasink.RandomAccess;
import java.awt.Component;
import java.io.IOException;
import javax.media.*;
import javax.media.control.StreamWriterControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

public abstract class BasicMux extends BasicPlugIn
    implements Multiplexer, Clock
{
    class BasicMuxPushStream
        implements PushSourceStream
    {

        public ContentDescriptor getContentDescriptor()
        {
            return cd;
        }

        public long getContentLength()
        {
            return -1L;
        }

        public boolean endOfStream()
        {
            return isEOS();
        }

        synchronized int write(byte data[], int offset, int length)
        {
            if(sth == null)
                return 0;
            if(isLiveData && (sth instanceof Syncable))
                ((Syncable)sth).setSyncEnabled();
            synchronized(writeLock)
            {
                this.data = data;
                dataOff = offset;
                dataLen = length;
                sth.transferData(this);
                while(dataLen > 0) 
                {
                    if(dataLen == length)
                        try
                        {
                            writeLock.wait();
                        }
                        catch(InterruptedException ie) { }
                    if(sth == null)
                        break;
                    if(dataLen > 0 && dataLen != length)
                    {
                        length = dataLen;
                        sth.transferData(this);
                    }
                }
            }
            return length;
        }

        synchronized int seek(int location)
        {
            if(sth != null)
            {
                ((Seekable)sth).seek(location);
                int seekVal = (int)((Seekable)sth).tell();
                return seekVal;
            } else
            {
                return -1;
            }
        }

        public int read(byte buffer[], int offset, int length)
            throws IOException
        {
            int transferred = 0;
            int i;
            synchronized(writeLock)
            {
                if(dataLen == -1)
                {
                    transferred = -1;
                } else
                {
                    if(length >= dataLen)
                        transferred = dataLen;
                    else
                        transferred = length;
                    System.arraycopy(data, dataOff, buffer, offset, transferred);
                    dataLen -= transferred;
                    dataOff += transferred;
                }
                writeLock.notifyAll();
                i = transferred;
            }
            return i;
        }

        public int getMinimumTransferSize()
        {
            return dataLen;
        }

        public void setTransferHandler(SourceTransferHandler sth)
        {
            synchronized(writeLock)
            {
                BasicMux.this.sth = sth;
                if(sth != null && needsSeekable() && !(sth instanceof Seekable))
                    throw new Error("SourceTransferHandler needs to be seekable");
                boolean requireTwoPass = BasicMux.this.requireTwoPass();
                if(requireTwoPass && sth != null && (sth instanceof RandomAccess))
                {
                    RandomAccess st = (RandomAccess)sth;
                    st.setEnabled(true);
                }
                writeLock.notifyAll();
            }
        }

        public Object[] getControls()
        {
            return new Control[0];
        }

        public Object getControl(String s)
        {
            return null;
        }

        private ContentDescriptor cd;
        private byte data[];
        private int dataLen;
        private int dataOff;
        private Integer writeLock;

        public BasicMuxPushStream(ContentDescriptor cd)
        {
            writeLock = new Integer(0);
            this.cd = cd;
        }
    }

    class BasicMuxDataSource extends PushDataSource
    {

        public PushSourceStream[] getStreams()
        {
            if(streams == null)
            {
                streams = new BasicMuxPushStream[1];
                stream = new BasicMuxPushStream(cd);
                streams[0] = stream;
                setStream(stream);
            }
            return streams;
        }

        public String getContentType()
        {
            return cd.getContentType();
        }

        public void connect()
            throws IOException
        {
            if(streams == null)
                getStreams();
            connected = true;
            synchronized(sourceLock)
            {
                sourceLock.notifyAll();
            }
        }

        boolean isConnected()
        {
            return connected;
        }

        boolean isStarted()
        {
            return started;
        }

        public void disconnect()
        {
            connected = false;
        }

        public void start()
            throws IOException
        {
            if(streams == null || !connected)
                throw new IOException("Source not connected yet!");
            started = true;
            synchronized(sourceLock)
            {
                sourceLock.notifyAll();
            }
        }

        public void stop()
        {
            started = false;
        }

        public Object[] getControls()
        {
            return new Control[0];
        }

        public Object getControl(String s)
        {
            return null;
        }

        public Time getDuration()
        {
            return Duration.DURATION_UNKNOWN;
        }

        private BasicMux mux;
        private ContentDescriptor cd;
        private BasicMuxPushStream streams[];
        private BasicMuxPushStream stream;
        private boolean connected;
        private boolean started;

        public BasicMuxDataSource(BasicMux mux, ContentDescriptor cd)
        {
            connected = false;
            started = false;
            this.cd = cd;
            this.mux = mux;
        }
    }

    class BasicMuxTimeBase extends MediaTimeBase
    {

        public long getMediaTime()
        {
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

        BasicMuxTimeBase()
        {
            ticks = 0L;
            updated = false;
        }
    }

    class SWC
        implements StreamWriterControl, Owned
    {

        public boolean setStreamSizeLimit(long limit)
        {
            bmx.fileSizeLimit = limit;
            return streamSizeLimitSupported;
        }

        public long getStreamSize()
        {
            return bmx.getStreamSize();
        }

        public Object getOwner()
        {
            return bmx;
        }

        public Component getControlComponent()
        {
            return null;
        }

        private BasicMux bmx;

        public SWC(BasicMux bmx)
        {
            this.bmx = bmx;
        }
    }


    public BasicMux()
    {
        numTracks = 0;
        flushing = false;
        sourceLock = new Integer(0);
        eos = false;
        firstBuffer = true;
        fileSize = 0;
        filePointer = 0;
        fileSizeLimit = -1L;
        streamSizeLimitSupported = true;
        fileSizeLimitReached = false;
        sth = null;
        isLiveData = false;
        swc = null;
        mc = null;
        timeBase = null;
        startup = new Integer(0);
        readyToStart = false;
        clock = null;
        master = 0;
        mClosed = false;
        dataReady = false;
        startCompensated = false;
        dataLock = new Object();
        masterTime = -1L;
        jpegFmt = new VideoFormat("jpeg");
        mjpgFmt = new VideoFormat("mjpg");
        rgbFmt = new VideoFormat("rgb");
        yuvFmt = new VideoFormat("yuv");
        maxBufSize = 32768;
        buf = new byte[maxBufSize];
        timeSetSync = new Object();
        started = false;
        systemStartTime = System.currentTimeMillis() * 0xf4240L;
        timeBase = new BasicMuxTimeBase();
        clock = new BasicClock();
        try
        {
            clock.setTimeBase(timeBase);
        }
        catch(Exception e) { }
        swc = new SWC(this);
        super.controls = (new Control[] {
            swc
        });
    }

    public void open()
    {
        firstBuffer = true;
        firstBuffers = new Buffer[inputs.length];
        firstBuffersDone = new boolean[inputs.length];
        nonKeyCount = new int[inputs.length];
        mediaTime = new long[inputs.length];
        for(int i = 0; i < inputs.length; i++)
        {
            firstBuffers[i] = null;
            firstBuffersDone[i] = false;
            nonKeyCount[i] = 0;
            mediaTime[i] = 0L;
        }

        ready = new boolean[inputs.length];
        resetReady();
        int len = 0;
        mc = new MonitorAdapter[inputs.length];
        for(int i = 0; i < inputs.length; i++)
            if((inputs[i] instanceof VideoFormat) || (inputs[i] instanceof AudioFormat))
            {
                mc[i] = new MonitorAdapter(inputs[i], this);
                if(mc[i] != null)
                    len++;
            }

        int j = 0;
        super.controls = new Control[len + 1];
        for(int i = 0; i < mc.length; i++)
            if(mc[i] != null)
                super.controls[j++] = mc[i];

        super.controls[j] = swc;
    }

    public void close()
    {
        if(sth != null)
        {
            writeFooter();
            write(null, 0, -1);
        }
        for(int i = 0; i < mc.length; i++)
            if(mc[i] != null)
                mc[i].close();

        synchronized(dataLock)
        {
            mClosed = true;
            dataLock.notifyAll();
        }
    }

    public void reset()
    {
        for(int i = 0; i < mediaTime.length; i++)
        {
            mediaTime[i] = 0L;
            if(mc[i] != null)
                mc[i].reset();
        }

        timeBase.update();
        resetReady();
        synchronized(sourceLock)
        {
            flushing = true;
            sourceLock.notifyAll();
        }
    }

    private void resetReady()
    {
        for(int i = 0; i < ready.length; i++)
            ready[i] = false;

        readyToStart = false;
        synchronized(startup)
        {
            startup.notifyAll();
        }
    }

    private boolean checkReady()
    {
        if(readyToStart)
            return true;
        for(int i = 0; i < ready.length; i++)
            if(!ready[i])
                return false;

        readyToStart = true;
        return true;
    }

    public Format[] getSupportedInputFormats()
    {
        return supportedInputs;
    }

    public ContentDescriptor[] getSupportedOutputContentDescriptors(Format inputs[])
    {
        return supportedOutputs;
    }

    public int setNumTracks(int numTracks)
    {
        this.numTracks = numTracks;
        if(inputs == null)
        {
            inputs = new Format[numTracks];
        } else
        {
            Format newInputs[] = new Format[numTracks];
            for(int i = 0; i < inputs.length; i++)
                newInputs[i] = inputs[i];

            inputs = newInputs;
        }
        return numTracks;
    }

    public Format setInputFormat(Format format, int trackID)
    {
        inputs[trackID] = format;
        return format;
    }

    public int process(Buffer buffer, int trackID)
    {
        if(buffer.isDiscard())
            return 0;
        if(!isLiveData && (buffer.getFlags() & 0x8000) > 0)
            isLiveData = true;
        while(source == null || !source.isConnected() || !source.isStarted()) 
            synchronized(sourceLock)
            {
                try
                {
                    sourceLock.wait(500L);
                }
                catch(InterruptedException ie) { }
                if(flushing)
                {
                    flushing = false;
                    buffer.setLength(0);
                    int i = 0;
                    return i;
                }
            }
        synchronized(this)
        {
            if(firstBuffer)
            {
                writeHeader();
                firstBuffer = false;
            }
        }
        if(numTracks > 1)
        {
            if((buffer.getFlags() & 0x1000) != 0 && buffer.getTimeStamp() <= 0L)
                return 0;
            if(!startCompensated && !compensateStart(buffer, trackID))
                return 0;
        }
        updateClock(buffer, trackID);
        if(mc[trackID] != null && mc[trackID].isEnabled())
            mc[trackID].process(buffer);
        int processResult = doProcess(buffer, trackID);
        if(fileSizeLimitReached)
            processResult |= 8;
        return processResult;
    }

    private boolean compensateStart(Buffer buffer, int trackID)
    {
        boolean flag8;
        synchronized(dataLock)
        {
            if(dataReady)
            {
                if(!firstBuffersDone[trackID])
                {
                    if(buffer.getTimeStamp() < masterTime)
                    {
                        boolean flag = false;
                        return flag;
                    }
                    if(buffer.getFormat() instanceof VideoFormat)
                    {
                        Format fmt = buffer.getFormat();
                        boolean isKey = jpegFmt.matches(fmt) || mjpgFmt.matches(fmt) || rgbFmt.matches(fmt) || yuvFmt.matches(fmt);
                        if(isKey || (buffer.getFlags() & 0x10) != 0 || nonKeyCount[trackID]++ > 30)
                        {
                            buffer.setTimeStamp(masterTime);
                            firstBuffersDone[trackID] = true;
                        } else
                        {
                            boolean flag5 = false;
                            return flag5;
                        }
                    } else
                    {
                        buffer.setTimeStamp(masterTime);
                        firstBuffersDone[trackID] = true;
                    }
                    for(int i = 0; i < firstBuffersDone.length; i++)
                        if(!firstBuffersDone[i])
                        {
                            boolean flag3 = true;
                            return flag3;
                        }

                    startCompensated = true;
                    boolean flag4 = true;
                    return flag4;
                }
                boolean flag1 = true;
                return flag1;
            }
            if(buffer.getTimeStamp() < 0L)
            {
                startCompensated = true;
                dataReady = true;
                dataLock.notifyAll();
                boolean flag2 = true;
                return flag2;
            }
            firstBuffers[trackID] = buffer;
            boolean done = true;
            for(int i = 0; i < firstBuffers.length; i++)
                if(firstBuffers[i] == null)
                    done = false;

            if(!done)
            {
                while(!dataReady && !mClosed) 
                    try
                    {
                        dataLock.wait();
                    }
                    catch(Exception e) { }
                if(mClosed || firstBuffers[trackID] == null)
                {
                    boolean flag6 = false;
                    return flag6;
                }
                boolean flag7 = true;
                return flag7;
            }
            masterTime = firstBuffers[0].getTimeStamp();
            for(int i = 0; i < firstBuffers.length; i++)
            {
                if(firstBuffers[i].getFormat() instanceof AudioFormat)
                {
                    masterTime = firstBuffers[i].getTimeStamp();
                    break;
                }
                if(firstBuffers[i].getTimeStamp() < masterTime)
                    masterTime = firstBuffers[i].getTimeStamp();
            }

            startCompensated = true;
            for(int i = 0; i < firstBuffers.length; i++)
                if(firstBuffers[i].getTimeStamp() >= masterTime)
                {
                    firstBuffers[i].setTimeStamp(masterTime);
                    firstBuffersDone[i] = true;
                } else
                {
                    firstBuffers[i] = null;
                    startCompensated = false;
                }

            synchronized(dataLock)
            {
                dataReady = true;
                dataLock.notifyAll();
            }
            flag8 = firstBuffers[trackID] != null;
        }
        return flag8;
    }

    private void updateClock(Buffer buffer, int trackID)
    {
        if(!readyToStart && numTracks > 1)
            synchronized(startup)
            {
                ready[trackID] = true;
                if(checkReady())
                    startup.notifyAll();
                else
                    try
                    {
                        while(!readyToStart) 
                            startup.wait(1000L);
                    }
                    catch(Exception e) { }
            }
        long timestamp = buffer.getTimeStamp();
        if(timestamp <= 0L && (buffer.getFormat() instanceof AudioFormat))
        {
            timestamp = mediaTime[trackID];
            mediaTime[trackID] += getDuration(buffer);
        } else
        if(timestamp <= 0L)
            mediaTime[trackID] = System.currentTimeMillis() * 0xf4240L - systemStartTime;
        else
            mediaTime[trackID] = timestamp;
        timeBase.update();
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor outputCD)
    {
        if(BasicPlugIn.matches(outputCD, supportedOutputs) == null)
        {
            return null;
        } else
        {
            this.outputCD = outputCD;
            return outputCD;
        }
    }

    public DataSource getDataOutput()
    {
        if(source == null)
        {
            source = new BasicMuxDataSource(this, outputCD);
            synchronized(sourceLock)
            {
                sourceLock.notifyAll();
            }
        }
        return source;
    }

    protected int doProcess(Buffer buffer, int trackID)
    {
        byte data[] = (byte[])buffer.getData();
        int dataLen = buffer.getLength();
        if(!buffer.isEOM())
            write(data, buffer.getOffset(), dataLen);
        return 0;
    }

    protected int write(byte data[], int offset, int length)
    {
        if(source == null || !source.isConnected())
            return length;
        if(length > 0)
        {
            filePointer += length;
            if(filePointer > fileSize)
                fileSize = filePointer;
            if(fileSizeLimit > 0L && (long)fileSize >= fileSizeLimit)
                fileSizeLimitReached = true;
        }
        return stream.write(data, offset, length);
    }

    void setStream(BasicMuxPushStream ps)
    {
        stream = ps;
    }

    long getStreamSize()
    {
        return (long)fileSize;
    }

    boolean needsSeekable()
    {
        return false;
    }

    protected int seek(int location)
    {
        if(source == null || !source.isConnected())
        {
            return location;
        } else
        {
            filePointer = stream.seek(location);
            return filePointer;
        }
    }

    boolean isEOS()
    {
        return eos;
    }

    protected void writeHeader()
    {
    }

    protected void writeFooter()
    {
    }

    protected void bufClear()
    {
        bufOffset = 0;
        bufLength = 0;
    }

    protected void bufSkip(int size)
    {
        bufOffset += size;
        bufLength += size;
        filePointer += size;
    }

    protected void bufWriteBytes(String s)
    {
        byte bytes[] = s.getBytes();
        bufWriteBytes(bytes);
    }

    protected void bufWriteBytes(byte bytes[])
    {
        System.arraycopy(bytes, 0, buf, bufOffset, bytes.length);
        bufOffset += bytes.length;
        bufLength += bytes.length;
        filePointer += bytes.length;
    }

    protected void bufWriteInt(int value)
    {
        buf[bufOffset + 0] = (byte)(value >> 24 & 0xff);
        buf[bufOffset + 1] = (byte)(value >> 16 & 0xff);
        buf[bufOffset + 2] = (byte)(value >> 8 & 0xff);
        buf[bufOffset + 3] = (byte)(value >> 0 & 0xff);
        bufOffset += 4;
        bufLength += 4;
        filePointer += 4;
    }

    protected void bufWriteIntLittleEndian(int value)
    {
        buf[bufOffset + 3] = (byte)(value >>> 24 & 0xff);
        buf[bufOffset + 2] = (byte)(value >>> 16 & 0xff);
        buf[bufOffset + 1] = (byte)(value >>> 8 & 0xff);
        buf[bufOffset + 0] = (byte)(value >>> 0 & 0xff);
        bufOffset += 4;
        bufLength += 4;
        filePointer += 4;
    }

    protected void bufWriteShort(short value)
    {
        buf[bufOffset + 0] = (byte)(value >> 8 & 0xff);
        buf[bufOffset + 1] = (byte)(value >> 0 & 0xff);
        bufOffset += 2;
        bufLength += 2;
        filePointer += 2;
    }

    protected void bufWriteShortLittleEndian(short value)
    {
        buf[bufOffset + 1] = (byte)(value >> 8 & 0xff);
        buf[bufOffset + 0] = (byte)(value >> 0 & 0xff);
        bufOffset += 2;
        bufLength += 2;
        filePointer += 2;
    }

    protected void bufWriteByte(byte value)
    {
        buf[bufOffset] = value;
        bufOffset++;
        bufLength++;
        filePointer++;
    }

    protected void bufFlush()
    {
        filePointer -= bufLength;
        write(buf, 0, bufLength);
    }

    private long getDuration(Buffer buffer)
    {
        AudioFormat format = (AudioFormat)buffer.getFormat();
        long duration = format.computeDuration(buffer.getLength());
        if(duration < 0L)
            return 0L;
        else
            return duration;
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
            systemStartTime = System.currentTimeMillis() * 0xf4240L;
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

    public boolean requireTwoPass()
    {
        return false;
    }

    protected Format supportedInputs[];
    protected ContentDescriptor supportedOutputs[];
    protected int numTracks;
    protected Format inputs[];
    protected BasicMuxDataSource source;
    protected BasicMuxPushStream stream;
    protected ContentDescriptor outputCD;
    protected boolean flushing;
    protected Integer sourceLock;
    protected boolean eos;
    protected boolean firstBuffer;
    protected int fileSize;
    protected int filePointer;
    protected long fileSizeLimit;
    protected boolean streamSizeLimitSupported;
    protected boolean fileSizeLimitReached;
    protected SourceTransferHandler sth;
    protected boolean isLiveData;
    protected StreamWriterControl swc;
    protected MonitorAdapter mc[];
    protected BasicMuxTimeBase timeBase;
    Object startup;
    boolean readyToStart;
    long mediaTime[];
    boolean ready[];
    protected BasicClock clock;
    int master;
    boolean mClosed;
    boolean dataReady;
    boolean startCompensated;
    Object dataLock;
    Buffer firstBuffers[];
    boolean firstBuffersDone[];
    int nonKeyCount[];
    long masterTime;
    VideoFormat jpegFmt;
    VideoFormat mjpgFmt;
    VideoFormat rgbFmt;
    VideoFormat yuvFmt;
    protected int maxBufSize;
    protected byte buf[];
    protected int bufOffset;
    protected int bufLength;
    Object timeSetSync;
    boolean started;
    long systemStartTime;
}
