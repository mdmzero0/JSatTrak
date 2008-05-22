// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MpegParser.java

package com.ibm.media.parser.video;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.*;
import com.sun.media.format.WavAudioFormat;
import com.sun.media.parser.BasicPullParser;
import com.sun.media.util.LoopThread;
import com.sun.media.util.jdk12;
import java.awt.Dimension;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

// Referenced classes of package com.ibm.media.parser.video:
//            MpegBufferThread, BadDataException, jdk12CreateThreadAction

public class MpegParser extends BasicPullParser
{
    private class SystemHeader
    {

        void resetSystemHeader()
        {
            headerLen = 0;
            rateBound = 0;
            audioBound = 0;
            fixedFlag = 0;
            CSPSFlag = 0;
            audioLockFlag = 0;
            videoLockFlag = 0;
            videoBound = 0;
            reserved = 0;
            allAudioSTDFlag = false;
            allVideoSTDFlag = false;
            for(int i = 0; i < 48; i++)
            {
                streamFlags[i] = false;
                STDBufBoundScale[i] = 0;
                STDBufSizeBound[i] = 0;
            }

        }

        void printFields()
        {
            System.out.println("headerLen " + headerLen);
            System.out.println("rateBound " + rateBound);
            System.out.println("audioBound " + audioBound);
            System.out.println("fixedFlag " + fixedFlag);
            System.out.println("CSPSFlag " + CSPSFlag);
            System.out.println("audioLockFlag " + audioLockFlag);
            System.out.println("videoLockFlag " + videoLockFlag);
            System.out.println("videoBound " + videoBound);
            System.out.println("reserved " + reserved);
            System.out.println("allAudioSTDFlag " + allAudioSTDFlag);
            System.out.println("allVideoSTDFlag " + allVideoSTDFlag);
            for(int i = 0; i < 48; i++)
                if(streamFlags[i])
                    System.out.println("[" + i + "]  STDBufBoundScale " + STDBufBoundScale[i] + "     STDBufSizeBound " + STDBufSizeBound[i]);

        }

        int headerLen;
        int rateBound;
        int audioBound;
        int fixedFlag;
        int CSPSFlag;
        int audioLockFlag;
        int videoLockFlag;
        int videoBound;
        int reserved;
        boolean allAudioSTDFlag;
        boolean allVideoSTDFlag;
        boolean streamFlags[];
        int STDBufBoundScale[];
        int STDBufSizeBound[];

        SystemHeader()
        {
            headerLen = 0;
            rateBound = 0;
            audioBound = 0;
            fixedFlag = 0;
            CSPSFlag = 0;
            audioLockFlag = 0;
            videoLockFlag = 0;
            videoBound = 0;
            reserved = 0;
            allAudioSTDFlag = false;
            allVideoSTDFlag = false;
            streamFlags = new boolean[48];
            STDBufBoundScale = new int[48];
            STDBufSizeBound = new int[48];
            for(int i = 0; i < 48; i++)
            {
                streamFlags[i] = false;
                STDBufBoundScale[i] = 0;
                STDBufSizeBound[i] = 0;
            }

        }
    }

    private class MediaTrack
        implements Track
    {

        public void setTrackListener(TrackListener l)
        {
            listener = l;
        }

        public Format getFormat()
        {
            return format;
        }

        public void setEnabled(boolean t)
        {
            enabled = t;
        }

        public boolean isEnabled()
        {
            return enabled;
        }

        public Time getDuration()
        {
            return trackInfo.duration;
        }

        public Time getStartTime()
        {
            if(streamType == 3)
                return new Time((double)startPTS / 90000D);
            else
                return new Time(AVstartTimeNs);
        }

        public void readFrame(Buffer buffer)
        {
            if(buffer == null)
                return;
            if(!enabled)
            {
                buffer.setDiscard(true);
                return;
            }
            if(streamType == 3)
                systemStreamReadFrame(buffer);
            else
                AudioVideoOnlyReadFrame(buffer);
            buffer.setFormat(format);
            buffer.setSequenceNumber(++sequenceNumber);
            if(format instanceof AudioFormat)
            {
                long tmp = buffer.getTimeStamp();
                buffer.setTimeStamp(lastAudioNs);
                lastAudioNs = tmp;
            }
        }

        private void AudioVideoOnlyReadFrame(Buffer buffer)
        {
            if(sysPausedFlag || parserErrorFlag)
            {
                buffer.setLength(0);
                buffer.setDiscard(true);
            }
            int size = trackInfo.readFrameSize;
            Object obj = buffer.getData();
            byte data[];
            if(obj == null || !(obj instanceof byte[]) || ((byte[])obj).length < size)
            {
                data = new byte[size];
                buffer.setData(data);
            } else
            {
                data = (byte[])obj;
            }
            int read1 = 0;
            int read2 = size;
            int actualBytesRead = 0;
            int counter = 0;
            if(initTmpBufLen > 0)
            {
                read1 = initTmpBufLen <= size ? initTmpBufLen : size;
                System.arraycopy(initTmpStreamBuf, 0, data, 0, read1);
                initTmpBufLen-= = read1;
                read2 -= read1;
                counter = read1;
            }
            if(trackInfo.trackType == 1)
                buffer.setTimeStamp(convBytesToTimeAV(getLocation() - (long)read1));
            if(read2 > 0 && !EOMflag)
                try
                {
                    actualBytesRead = parser.readBytes(stream, data, read1, read2);
                    if(actualBytesRead == -2)
                    {
                        if(read1 == 0)
                        {
                            buffer.setDiscard(true);
                            return;
                        }
                    } else
                    {
                        counter += actualBytesRead;
                    }
                }
                catch(IOException e)
                {
                    updateEOMState();
                    EOMflag = true;
                    if(AVlastTimeNs == 0L)
                    {
                        AVcurrentTimeNs = convBytesToTimeAV(getLocation());
                        AVlastTimeNs = AVcurrentTimeNs;
                    }
                }
            if(EOMflag)
                if(read1 > 0)
                {
                    buffer.setLength(read1);
                    buffer.setOffset(0);
                } else
                {
                    buffer.setLength(0);
                    buffer.setEOM(true);
                }
            buffer.setOffset(0);
            buffer.setLength(counter);
        }

        private void systemStreamReadFrame(Buffer buffer)
        {
            trackInfo.copyFromInnerBuffer(buffer);
            if(sysPausedFlag || parserErrorFlag)
                return;
            for(int i = 0; i < numTracks; i++)
                if(tracks[i] != null && !tracks[i].isEnabled())
                {
                    TrackList AtrackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                    AtrackInfo.flushBuffer();
                }

            if(hideAudioTracks)
            {
                for(int i = 0; i < numTracks; i++)
                    if(tracks[i] != null)
                    {
                        TrackList AtrackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                        if(AtrackInfo.trackType == 1)
                            AtrackInfo.flushBuffer();
                    }

            }
            if(hideVideoTracks)
            {
                for(int i = 0; i < numTracks; i++)
                    if(tracks[i] != null)
                    {
                        TrackList AtrackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                        if(AtrackInfo.trackType == 2)
                            AtrackInfo.flushBuffer();
                    }

            }
        }

        public int mapTimeToFrame(Time t)
        {
            return 0;
        }

        public Time mapFrameToTime(int frameNumber)
        {
            return null;
        }

        private TrackList getTrackInfo()
        {
            return trackInfo;
        }

        private TrackList trackInfo;
        private boolean enabled;
        private long sequenceNumber;
        private Format format;
        private TrackListener listener;
        MpegParser parser;


        MediaTrack(TrackList trackInfo)
        {
            sequenceNumber = 0L;
            parser = MpegParser.this;
            this.trackInfo = trackInfo;
            enabled = true;
            format = trackInfo.media.createFormat();
        }
    }

    private class TrackList
    {

        void init(byte stype)
        {
            supported = true;
            trackType = stype;
            if(trackType == 2)
                bufQ = new CircularBuffer(15);
            else
                bufQ = new CircularBuffer(10);
        }

        int readyDataBytes()
        {
            return 1;
        }

        void copyStreamDataToInnerBuffer(byte in[], int inSize, int size, long pts)
            throws IOException
        {
            int total = size;
            int len = 0;
            if(inSize > 0)
                total += inSize;
            else
                inSize = 0;
            synchronized(bufQ)
            {
                if(current != null)
                {
                    len = current.getLength();
                    if(len != 0 && len + total >= readFrameSize)
                    {
                        bufQ.writeReport();
                        bufQ.notify();
                        current = null;
                    }
                }
                flushFlag = false;
                byte data[];
                if(current == null)
                {
                    while(!bufQ.canWrite() && !flushFlag) 
                        try
                        {
                            bufQ.wait();
                        }
                        catch(InterruptedException e) { }
                    if(flushFlag)
                        return;
                    current = bufQ.getEmptyBuffer();
                    current.setFlags(0);
                    current.setOffset(0);
                    current.setLength(0);
                    current.setTimeStamp(convPTStoNanoseconds(pts));
                    int bsize = total <= readFrameSize ? readFrameSize : total;
                    data = (byte[])current.getData();
                    if(data == null || data.length < bsize)
                    {
                        data = new byte[bsize];
                        current.setData(data);
                    }
                } else
                {
                    data = (byte[])current.getData();
                }
                len = current.getLength();
                if(inSize > 0)
                    System.arraycopy(in, 0, data, len, inSize);
                parser.readBytes(stream, data, len + inSize, size);
                current.setLength(len + total);
            }
        }

        void copyFromInnerBuffer(Buffer out)
        {
            synchronized(bufQ)
            {
                while(!bufQ.canRead() && !sysPausedFlag && !parserErrorFlag) 
                    try
                    {
                        bufQ.wait();
                    }
                    catch(InterruptedException e) { }
                if(sysPausedFlag || parserErrorFlag)
                {
                    out.setLength(0);
                    out.setDiscard(true);
                    return;
                }
                Buffer buf = bufQ.read();
                byte saved[] = (byte[])out.getData();
                out.copy(buf);
                buf.setData(saved);
                bufQ.readReport();
                bufQ.notify();
            }
        }

        void releaseReadFrame()
        {
            synchronized(bufQ)
            {
                bufQ.notifyAll();
            }
        }

        void generateEOM()
        {
            synchronized(bufQ)
            {
                if(current != null)
                {
                    bufQ.writeReport();
                    bufQ.notify();
                    current = null;
                }
                while(!bufQ.canWrite()) 
                    try
                    {
                        bufQ.wait();
                    }
                    catch(InterruptedException e) { }
                Buffer buf = bufQ.getEmptyBuffer();
                buf.setFlags(1);
                buf.setLength(0);
                bufQ.writeReport();
                bufQ.notify();
            }
        }

        void flushBuffer()
        {
            synchronized(bufQ)
            {
                if(current != null)
                {
                    current.setDiscard(true);
                    bufQ.writeReport();
                    current = null;
                }
                for(; bufQ.canRead(); bufQ.readReport())
                    bufQ.read();

                bufQ.notifyAll();
            }
        }

        public String toString()
        {
            System.out.println("track type " + trackType + "(0 ?, 1 audio, 2 video)");
            System.out.println("start PTS " + startPTS);
            System.out.println("info flag " + infoFlag);
            System.out.println("number of packets " + numPackets);
            System.out.println("maximum packet size " + maxPacketSize);
            System.out.println("supported " + supported);
            System.out.println("duration (?) " + duration);
            return media.toString();
        }

        void saveBufToFile()
        {
        }

        byte trackType;
        Time duration;
        long startPTS;
        boolean infoFlag;
        int numPackets;
        int maxPacketSize;
        int readFrameSize;
        Media media;
        boolean supported;
        boolean flushFlag;
        CircularBuffer bufQ;
        Buffer current;
        MpegParser parser;

        private TrackList()
        {
            trackType = 0;
            duration = Duration.DURATION_UNKNOWN;
            startPTS = 0xffffffffffcd232bL;
            infoFlag = false;
            numPackets = 0;
            maxPacketSize = 0;
            readFrameSize = 0;
            supported = false;
            flushFlag = false;
            bufQ = null;
            current = null;
            parser = MpegParser.this;
        }

    }

    private class Video extends Media
    {

        Format createFormat()
        {
            int size = (int)((double)(width * height) * 1.5D);
            if(format != null)
            {
                return format;
            } else
            {
                format = new VideoFormat("mpeg", new Dimension(width, height), size, byte[].class, pictureRate);
                return format;
            }
        }

        public String toString()
        {
            System.out.println("Video Media: " + format);
            System.out.println("width " + width);
            System.out.println("height " + height);
            System.out.println("pixel aspect ratio " + pelAspectRatio);
            System.out.println("picture rate " + pictureRate);
            System.out.println("bitrate " + bitRate);
            return super.toString();
        }

        int width;
        int height;
        float pelAspectRatio;
        float pictureRate;
        int bitRate;
        VideoFormat format;

        private Video()
        {
            width = 0;
            height = 0;
            pelAspectRatio = 0.0F;
            pictureRate = 0.0F;
            bitRate = 0;
            format = null;
        }

    }

    private class Audio extends Media
    {

        Format createFormat()
        {
            if(format != null)
                return format;
            String encodingString;
            if(layer == 3)
                encodingString = "mpeglayer3";
            else
                encodingString = "mpegaudio";
            int bitsPerSample = 16;
            int frameSizeInBits = (layer != 1 ? '\u0400' : 352) * channels * bitsPerSample;
            int bytesPerSecond = bitRate * 1000 >> 3;
            format = new WavAudioFormat(encodingString, sampleRate, bitsPerSample, channels, frameSizeInBits, bytesPerSecond, 0, 1, -1F, byte[].class, null);
            return format;
        }

        public String toString()
        {
            System.out.println("Audio Media: " + format);
            System.out.println("Number of channels " + channels);
            System.out.println("valid " + valid);
            System.out.println("ID " + ID);
            System.out.println("layer " + layer);
            System.out.println("protection " + protection);
            System.out.println("bitrate " + bitRate);
            System.out.println("sample rate " + sampleRate);
            System.out.println("Mode " + mode + " ext " + modeExt);
            System.out.println("copyright " + copyright);
            System.out.println("original " + original);
            System.out.println("emphasis " + emphasis);
            System.out.println("channels " + channels);
            return super.toString();
        }

        boolean valid;
        int ID;
        int layer;
        int protection;
        int bitRate;
        int sampleRate;
        int mode;
        int modeExt;
        int copyright;
        int original;
        int emphasis;
        int channels;
        AudioFormat format;

        private Audio()
        {
            valid = false;
            ID = 0;
            layer = 0;
            protection = 0;
            bitRate = 0;
            sampleRate = 0;
            mode = 0;
            modeExt = 0;
            copyright = 0;
            original = 0;
            emphasis = 0;
            channels = 0;
            format = null;
        }

    }

    private abstract class Media
    {

        abstract Format createFormat();

        private Media()
        {
        }

    }


    public MpegParser()
    {
        saveOutputFlag = false;
        AoutName = "Audio.mpg";
        VoutName = "Video.mpg";
        throwOutputFlag = false;
        hideAudioTracks = false;
        hideVideoTracks = false;
        stream = null;
        trackList = new TrackList[MAX_TRACKS_SUPPORTED];
        tracks = null;
        videoTracks = null;
        audioTracks = null;
        videoCount = 0;
        audioCount = 0;
        numSupportedTracks = 0;
        numTracks = 0;
        numPackets = 0;
        streamType = 0;
        streamContentLength = 0L;
        sysHeader = new SystemHeader();
        sysHeaderSeen = false;
        EOMflag = false;
        parserErrorFlag = false;
        durationInitialized = false;
        sysPausedFlag = false;
        seekableStreamFlag = false;
        randomAccessStreamFlag = true;
        mSecurity = new Method[1];
        clSecurity = new Class[1];
        argsSecurity = new Object[1][0];
        startLocation = 0L;
        durationNs = Duration.DURATION_UNKNOWN;
        lastSetPositionTime = new Time(0L);
        startPTS = 0xffffffffffcd232bL;
        currentPTS = 0xffffffffffcd232bL;
        endPTS = 0xffffffffffcd232bL;
        AVstartTimeNs = 0L;
        AVcurrentTimeNs = 0L;
        AVlastTimeNs = 0L;
        lastAudioNs = 0L;
        mpThread = null;
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        super.setSource(source);
        stream = (PullSourceStream)super.streams[0];
        streamContentLength = stream.getContentLength();
        seekableStreamFlag = super.streams[0] instanceof Seekable;
        if(!seekableStreamFlag)
        {
            throw new IncompatibleSourceException("Mpeg Stream is not Seekable");
        } else
        {
            randomAccessStreamFlag = seekableStreamFlag && ((Seekable)super.streams[0]).isRandomAccess();
            return;
        }
    }

    public ContentDescriptor[] getSupportedInputContentDescriptors()
    {
        return supportedFormat;
    }

    public void start()
        throws IOException
    {
        super.start();
        sysPausedFlag = false;
        if(mpThread != null)
            mpThread.start();
    }

    public void stop()
    {
        super.stop();
        sysPausedFlag = true;
        if(mpThread != null)
            mpThread.pause();
        for(int i = 0; i < numTracks; i++)
            if(tracks[i] != null && tracks[i].isEnabled())
            {
                TrackList info = ((MediaTrack)tracks[i]).getTrackInfo();
                info.releaseReadFrame();
            }

    }

    public void close()
    {
        stop();
        flushInnerBuffers();
        super.close();
        if(mpThread != null)
            mpThread.kill();
    }

    public Track[] getTracks()
        throws IOException, BadHeaderException
    {
        if(streamType == 3)
        {
            if(hideAudioTracks && videoTracks != null)
                return videoTracks;
            if(hideVideoTracks && audioTracks != null)
                return audioTracks;
        }
        if(tracks != null)
            return tracks;
        try
        {
            initTmpBufLen = 0x186a0;
            initTmpStreamBuf = new byte[initTmpBufLen];
            initTmpBufLen = detectStreamType(initTmpStreamBuf);
            switch(streamType)
            {
            case 1: // '\001'
            case 2: // '\002'
                initTrackAudioVideoOnly();
                break;

            case 3: // '\003'
                initTrackSystemStream();
                break;

            case 0: // '\0'
            default:
                throw new BadHeaderException("Couldn't detect stream type");
            }
            initDuration();
            if(saveOutputFlag)
            {
                aout = new FileOutputStream(AoutName);
                vout = new FileOutputStream(VoutName);
            }
            if(streamType == 3)
            {
                if(jmfSecurity != null)
                {
                    String permission = null;
                    try
                    {
                        if(jmfSecurity.getName().startsWith("jmf-security"))
                        {
                            permission = "thread";
                            jmfSecurity.requestPermission(mSecurity, clSecurity, argsSecurity, 16);
                            mSecurity[0].invoke(clSecurity[0], argsSecurity[0]);
                            permission = "thread group";
                            jmfSecurity.requestPermission(mSecurity, clSecurity, argsSecurity, 32);
                            mSecurity[0].invoke(clSecurity[0], argsSecurity[0]);
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
                        Constructor cons = jdk12CreateThreadAction.cons;
                        mpThread = (MpegBufferThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                            cons.newInstance(new Object[] {
                                com.ibm.media.parser.video.MpegBufferThread.class
                            })
                        });
                    }
                    catch(Exception e)
                    {
                        System.err.println("MpegParser: Caught Exception " + e);
                    }
                else
                    mpThread = new MpegBufferThread();
                if(mpThread != null)
                {
                    mpThread.setParser(this);
                    mpThread.start();
                }
                if(saveOutputFlag || throwOutputFlag)
                    try
                    {
                        Thread.sleep(30000L);
                    }
                    catch(InterruptedException e) { }
            }
            if(streamType == 3)
            {
                if(hideAudioTracks)
                    return videoTracks;
                if(hideVideoTracks)
                    return audioTracks;
            }
            return tracks;
        }
        catch(BadDataException e)
        {
            parserErrorFlag = true;
            throw new BadHeaderException("Bad data");
        }
        catch(BadHeaderException e)
        {
            parserErrorFlag = true;
            throw e;
        }
        catch(IOException e)
        {
            updateEOMState();
            EOMflag = true;
            throw e;
        }
    }

    private boolean isValidMp3Header(int code)
    {
        return (code >>> 21 & 0x7ff) == 2047 && (code >>> 19 & 3) != 1 && (code >>> 17 & 3) != 0 && (code >>> 12 & 0xf) != 0 && (code >>> 12 & 0xf) != 15 && (code >>> 10 & 3) != 3 && (code & 3) != 2;
    }

    private int detectStreamType(byte streamBuf[])
        throws IOException
    {
        int i = 0;
        int videoCount = 0;
        int audioCount = 0;
        boolean found = false;
        if(streamType != 0)
            return 0;
        try
        {
            readBytes(stream, streamBuf, 4);
            while(!found && i < streamBuf.length - 5) 
            {
                int code = (streamBuf[i] & 0xff) << 24 | (streamBuf[i + 1] & 0xff) << 16 | (streamBuf[i + 2] & 0xff) << 8 | streamBuf[i + 3] & 0xff;
                switch(code)
                {
                case 442: 
                    i++;
                    readBytes(stream, streamBuf, i + 3, 1);
                    if((streamBuf[i + 3] & 0xfffffff1) == 33)
                    {
                        streamType = 3;
                        found = true;
                    }
                    continue;

                case 435: 
                    if(i == 0)
                    {
                        streamType = 2;
                        found = true;
                    }
                    // fall through

                case 256: 
                case 440: 
                    videoCount++;
                    break;

                default:
                    if((code & 0xfff00000) == 0xfff00000 && (code & 0x60000) != 0 && isValidMp3Header(code))
                    {
                        audioCount++;
                        streamType = 1;
                        found = true;
                        startLocation = i;
                    }
                    break;
                }
                i++;
                readBytes(stream, streamBuf, i + 3, 1);
            }
        }
        catch(IOException e)
        {
            if(streamType == 0)
                if(videoCount > 0)
                    streamType = 2;
                else
                if(audioCount > 0)
                    streamType = 1;
            updateEOMState();
            EOMflag = true;
            throw e;
        }
        if(streamType == 0)
            if(videoCount > 4)
                streamType = 2;
            else
            if(audioCount > 20)
                streamType = 1;
        if(seekableStreamFlag && streamType == 1)
        {
            int duration = -1;
            Seekable s = (Seekable)stream;
            long currentPos = s.tell();
            s.seek(startLocation);
            int frameHeader = readInt(stream);
            int h_id = frameHeader >>> 19 & 3;
            int h_layer = frameHeader >>> 17 & 3;
            int h_bitrate = frameHeader >>> 12 & 0xf;
            int h_samplerate = frameHeader >>> 10 & 3;
            int h_padding = frameHeader >>> 9 & 1;
            int h_mode = frameHeader >>> 6 & 3;
            int bitrate = bitrates[h_id][h_layer][h_bitrate];
            int offset = (h_id & 1) != 1 ? ((int) (h_mode == 3 ? 13 : 21)) : ((int) (h_mode == 3 ? 21 : 36));
            s.seek(offset);
            String hdr = readString(stream);
            if(hdr.equals("Xing"))
            {
                int flags = readInt(stream);
                int frames = readInt(stream);
                int bytes = readInt(stream);
                int samplerate = samplerates[h_id][h_samplerate];
                int frameSize = (0x23280 * bitrate) / samplerate + h_padding;
                duration = (frameSize * frames) / (bitrate * 125);
                if(duration > 0)
                {
                    durationInitialized = true;
                    durationNs = new Time(duration);
                }
            }
            s.seek(currentPos);
        }
        return i + 4;
    }

    private void initTrackAudioVideoOnly()
        throws IOException, BadHeaderException, BadDataException
    {
        int itmp = 0;
        numTracks = 1;
        tracks = new Track[1];
        trackList[0] = new TrackList();
        int possibleLen = streamType != 1 ? 0x30d40 : 0x186a0;
        if(initTmpBufLen < possibleLen)
        {
            if(possibleLen > initTmpStreamBuf.length)
            {
                byte tmpBuf2[] = new byte[possibleLen];
                System.arraycopy(initTmpStreamBuf, 0, tmpBuf2, 0, initTmpBufLen);
                initTmpStreamBuf = tmpBuf2;
            }
            try
            {
                itmp = readBytes(stream, initTmpStreamBuf, initTmpBufLen, possibleLen - initTmpBufLen);
            }
            catch(IOException e)
            {
                updateEOMState();
                EOMflag = true;
            }
            initTmpBufLen += itmp;
        }
        TrackList trackInfo = trackList[0];
        do
        {
            extractStreamInfo(initTmpStreamBuf, 0, initTmpBufLen, true);
            if(trackInfo.infoFlag)
                break;
            try
            {
                itmp = readBytes(stream, initTmpStreamBuf, possibleLen);
            }
            catch(IOException e)
            {
                updateEOMState();
                EOMflag = true;
                break;
            }
            initTmpBufLen = itmp;
        } while(!trackInfo.infoFlag);
        if(!trackInfo.infoFlag)
        {
            numTracks = 0;
            tracks = null;
            throw new BadHeaderException("Sorry, No tracks found");
        } else
        {
            ((Seekable)stream).seek(0L);
            initTmpBufLen = 0;
            EOMflag = false;
            return;
        }
    }

    private void initTrackSystemStream()
        throws IOException, BadHeaderException, BadDataException
    {
        tracks = new Track[MAX_TRACKS_SUPPORTED];
        for(int i = 0; i < tracks.length; i++)
            tracks[i] = null;

        for(int i = 0; i < trackList.length; i++)
            trackList[i] = null;

        mpegSystemParseBitstream(false, 0L, true, 0xffffffffffcd232bL);
        if(numTracks == 0)
            throw new BadHeaderException("Sorry, No tracks found");
        Track tmpTracks[] = new Track[numTracks];
        for(int i = 0; i < numTracks; i++)
            tmpTracks[i] = tracks[i];

        tracks = tmpTracks;
        if(hideAudioTracks)
        {
            int i;
            for(i = 0; i < numTracks; i++)
                if(tracks[i] != null)
                {
                    TrackList trackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                    if(trackInfo.trackType == 2)
                        videoCount++;
                }

            if(videoCount == 0)
                throw new BadHeaderException("Sorry, No video tracks found");
            videoTracks = new Track[videoCount];
            i = 0;
            int v = 0;
            for(; i < numTracks; i++)
                if(tracks[i] != null)
                {
                    TrackList trackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                    if(trackInfo.trackType == 2)
                        videoTracks[v] = tracks[i];
                }

        }
        if(hideVideoTracks)
        {
            int i;
            for(i = 0; i < numTracks; i++)
                if(tracks[i] != null)
                {
                    TrackList trackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                    if(trackInfo.trackType == 1)
                        audioCount++;
                }

            if(audioCount == 0)
                throw new BadHeaderException("Sorry, No video tracks found");
            audioTracks = new Track[audioCount];
            i = 0;
            int v = 0;
            for(; i < numTracks; i++)
                if(tracks[i] != null)
                {
                    TrackList trackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                    if(trackInfo.trackType == 1)
                        audioTracks[v] = tracks[i];
                }

        }
    }

    public String getName()
    {
        return "Parser for MPEG-1 file format";
    }

    private long convPTStoNanoseconds(long val)
    {
        return (val * 0x186a0L) / 9L;
    }

    private long convNanosecondsToPTS(long val)
    {
        return (val * 9L) / 0x186a0L;
    }

    private long convBytesToTimeAV(long bytes)
    {
        if(trackList[0] == null)
            return 0L;
        long time;
        if(streamType == 1)
        {
            if(((Audio)trackList[0].media).bitRate == 0)
            {
                time = 0L;
            } else
            {
                time = (bytes << 3) / (long)((Audio)trackList[0].media).bitRate;
                time *= 0xf4240L;
            }
        } else
        if(((Video)trackList[0].media).bitRate == 0)
        {
            time = 0L;
        } else
        {
            time = (bytes << 3) / (long)((Video)trackList[0].media).bitRate;
            time *= 0x3b9aca00L;
        }
        return time;
    }

    private long convTimeToBytesAV(long time)
    {
        long bytes;
        if(streamType == 1)
        {
            bytes = (time >> 3) * (long)((Audio)trackList[0].media).bitRate;
            bytes /= 0xf4240L;
        } else
        {
            bytes = (time >> 3) * (long)((Video)trackList[0].media).bitRate;
            bytes /= 0x3b9aca00L;
        }
        return bytes;
    }

    public Time getDuration()
    {
        if(durationInitialized)
            return durationNs;
        if(EOMflag)
            durationInitialized = true;
        return durationNs;
    }

    private void initDuration()
    {
        if(streamContentLength != -1L)
            if(streamType == 3)
            {
                if(randomAccessStreamFlag)
                    initDurationSystemSeekableRA();
            } else
            {
                updateDurationAudioVideoOnly();
            }
    }

    private void updateDurationAudioVideoOnly()
    {
        if(durationInitialized)
        {
            return;
        } else
        {
            AVstartTimeNs = 0L;
            AVcurrentTimeNs = 0L;
            AVlastTimeNs = convBytesToTimeAV(streamContentLength);
            durationNs = new Time(AVlastTimeNs - AVstartTimeNs);
            durationInitialized = true;
            return;
        }
    }

    private void initDurationSystemSeekableRA()
    {
        long baseLocation = 0L;
        int saveNumPackets = numPackets;
        boolean saveEOMflag = EOMflag;
        baseLocation = ((Seekable)stream).tell();
        if(startPTS == 0xffffffffffcd232bL)
        {
            EOMflag = false;
            ((Seekable)stream).seek(0L);
            try
            {
                mpegSystemParseBitstream(true, 0x10000L, false, 0xffffffffffcd232bL);
            }
            catch(Exception e) { }
        }
        if(startPTS == 0xffffffffffcd232bL)
            startPTS = 0L;
        long ltmp;
        if(endPTS == 0xffffffffffcd232bL)
        {
            EOMflag = false;
            currentPTS = 0xffffffffffcd232bL;
            ltmp = streamContentLength - 0x20000L;
            if(ltmp < 0L)
                ltmp = 0L;
            ((Seekable)stream).seek(ltmp);
            try
            {
                mpegSystemParseBitstream(true, 0x20000L, false, 0xffffffffffcd232bL);
            }
            catch(Exception e) { }
            endPTS = currentPTS;
        }
        if(endPTS == 0xffffffffffcd232bL)
            endPTS = startPTS;
        ltmp = endPTS - startPTS;
        if(ltmp < 0L)
        {
            ltmp = 0L;
            parserErrorFlag = true;
        }
        durationNs = new Time(convPTStoNanoseconds(ltmp));
        lastSetPositionTime = new Time(convPTStoNanoseconds(startPTS));
        ((Seekable)stream).seek(baseLocation);
        EOMflag = saveEOMflag;
        numPackets = saveNumPackets;
        durationInitialized = true;
    }

    void updateTrackEOM()
    {
        for(int i = 0; i < trackList.length; i++)
            if(trackList[i] != null)
                trackList[i].generateEOM();

    }

    void updateEOMState()
    {
        if(!durationInitialized)
        {
            if(streamContentLength == -1L)
                streamContentLength = getLocation(stream);
            if(streamType == 3)
            {
                if(startPTS == 0xffffffffffcd232bL)
                    startPTS = 0L;
                if(endPTS == 0xffffffffffcd232bL)
                    endPTS = currentPTS;
                if(endPTS == 0xffffffffffcd232bL)
                    endPTS = startPTS;
                long ltmp = endPTS - startPTS;
                if(ltmp < 0L)
                {
                    ltmp = 0L;
                    parserErrorFlag = true;
                }
                durationNs = new Time(convPTStoNanoseconds(ltmp));
                durationInitialized = true;
            } else
            {
                updateDurationAudioVideoOnly();
            }
        }
    }

    public Time getMediaTime()
    {
        Time mtime;
        if(streamType == 3)
        {
            if(currentPTS == 0xffffffffffcd232bL)
                mtime = new Time(0L);
            else
                mtime = new Time(convPTStoNanoseconds(currentPTS - startPTS));
        } else
        {
            AVcurrentTimeNs = convBytesToTimeAV(getLocation(stream));
            mtime = new Time(AVcurrentTimeNs);
        }
        return mtime;
    }

    public Time setPosition(Time where, int rounding)
    {
        Time newTime = null;
        if(!durationInitialized || durationNs == Duration.DURATION_UNKNOWN)
            return new Time(0L);
        Time preWhere = new Time(where.getNanoseconds() - 0x1dcd6500L);
        long newTimeNs;
        if(streamType == 3)
        {
            flushInnerBuffers();
            long preWherePTS = convNanosecondsToPTS(preWhere.getNanoseconds());
            preWherePTS += startPTS;
            long wherePTS = convNanosecondsToPTS(where.getNanoseconds());
            wherePTS += startPTS;
            long newPTS = setPositionSystemSeekableRA(preWherePTS, wherePTS);
            newTimeNs = convPTStoNanoseconds(newPTS);
            lastAudioNs = newTimeNs;
        } else
        {
            newTimeNs = setPositionAudioVideoOnly(preWhere.getNanoseconds(), where.getNanoseconds());
            lastAudioNs = newTimeNs;
        }
        newTime = new Time(newTimeNs);
        if(lastSetPositionTime.getNanoseconds() == newTimeNs)
            newTimeNs++;
        lastSetPositionTime = new Time(newTimeNs);
        EOMflag = false;
        parserErrorFlag = false;
        return newTime;
    }

    private long setPositionAudioVideoOnly(long where, long origWhere)
    {
        long newTime;
        if((float)origWhere <= (float)AVstartTimeNs + 5E+008F)
        {
            newTime = AVstartTimeNs;
            ((Seekable)stream).seek(0L);
        } else
        if((float)origWhere >= (float)AVlastTimeNs - 5E+008F)
        {
            newTime = AVlastTimeNs - AVstartTimeNs;
            ((Seekable)stream).seek(streamContentLength);
        } else
        {
            newTime = where;
            long pos = convTimeToBytesAV(where);
            ((Seekable)stream).seek(pos);
        }
        return newTime;
    }

    private long setPositionSystemSeekableRA(long wherePTS, long origWherePTS)
    {
        long newTime = 0xffffffffffcd232bL;
        long lres = -1L;
        long saveStartPTS = startPTS;
        boolean saveEOMflag = EOMflag;
        boolean zeroPosFlag = false;
        if(endPTS == 0xffffffffffcd232bL || startPTS == 0xffffffffffcd232bL)
        {
            newTime = 0L;
            ((Seekable)stream).seek(0L);
        } else
        if((float)origWherePTS <= (float)startPTS + 45000F)
        {
            newTime = 0L;
            ((Seekable)stream).seek(0L);
        } else
        if((float)origWherePTS >= (float)endPTS - 45000F)
        {
            newTime = endPTS - startPTS;
            ((Seekable)stream).seek(streamContentLength);
        } else
        if((float)(endPTS - startPTS) < 45000F)
        {
            newTime = 0L;
            ((Seekable)stream).seek(0L);
        } else
        {
            long pos = (long)((float)streamContentLength * ((float)(wherePTS - startPTS) / (float)(endPTS - startPTS)));
            long step = 20480L;
            pos -= step;
            if(pos < 0L)
                pos = 0L;
            long range = streamContentLength - pos;
            do
            {
                ((Seekable)stream).seek(pos);
                currentPTS = 0xffffffffffcd232bL;
                startPTS = 0xffffffffffcd232bL;
                EOMflag = false;
                try
                {
                    lres = mpegSystemParseBitstream(true, range, false, wherePTS);
                }
                catch(IOException e)
                {
                    lres = -2L;
                    saveEOMflag = true;
                }
                catch(Exception e)
                {
                    lres = -1L;
                }
                if(lres >= 0L)
                {
                    newTime = currentPTS - saveStartPTS;
                    ((Seekable)stream).seek(lres);
                    break;
                }
                if(lres == -2L)
                {
                    newTime = endPTS - saveStartPTS;
                    ((Seekable)stream).seek(streamContentLength);
                    break;
                }
                pos -= step;
                if(pos <= 0L)
                {
                    if(zeroPosFlag)
                    {
                        newTime = 0L;
                        ((Seekable)stream).seek(0L);
                        break;
                    }
                    pos = 0L;
                    zeroPosFlag = true;
                }
                range = 3L * step;
            } while(true);
            startPTS = saveStartPTS;
            EOMflag = saveEOMflag;
        }
        return newTime;
    }

    long mpegSystemParseBitstream(boolean justLooking, long range, boolean justEnough, long newPTS)
        throws IOException, BadHeaderException, BadDataException
    {
        byte buf1[] = new byte[1];
        int code = 0;
        boolean read4 = true;
        boolean packFound = false;
        long baseLocation = getLocation(stream);
        long lastPacketLocation = baseLocation;
        long lastLastPacketLocation = baseLocation;
        long loc = baseLocation + 4L;
        long lastCurrentPTS = 0xffffffffffcd232bL;
        long savePTS = 0xffffffffffcd232bL;
        while(!sysPausedFlag && !EOMflag || justLooking || justEnough) 
        {
            if(justEnough && !needingMore())
                break;
            if(justLooking)
            {
                if(getLocation(stream) - baseLocation > range)
                    break;
                if(newPTS != 0xffffffffffcd232bL)
                {
                    if(newPTS < startPTS)
                        return -1L;
                    if(newPTS <= currentPTS)
                        if(newPTS == currentPTS)
                        {
                            return lastPacketLocation;
                        } else
                        {
                            currentPTS = lastCurrentPTS;
                            return lastLastPacketLocation;
                        }
                }
            }
            if(read4)
            {
                code = readInt(stream, true);
            } else
            {
                readBytes(stream, buf1, 1);
                code = code << 8 & 0xffffff00 | buf1[0] & 0xff;
            }
            switch(code)
            {
            case 442: 
                parsePackHeader();
                read4 = true;
                packFound = true;
                break;

            case 443: 
                parseSystemHeader();
                read4 = true;
                break;

            case 441: 
                EOMflag = true;
                if(endPTS == 0xffffffffffcd232bL)
                    endPTS = currentPTS;
                if(!justLooking || newPTS != 0xffffffffffcd232bL)
                    updateEOMState();
                break;

            default:
                if(code >> 8 == 1 && (!justLooking || packFound & justLooking))
                {
                    if(justLooking && newPTS != 0xffffffffffcd232bL)
                    {
                        loc = getLocation(stream);
                        savePTS = currentPTS;
                    }
                    byte bval = (byte)(code & 0xff);
                    parsePacket(bval, justLooking);
                    read4 = true;
                    if(justLooking && newPTS != 0xffffffffffcd232bL && savePTS != currentPTS)
                    {
                        lastCurrentPTS = savePTS;
                        lastLastPacketLocation = lastPacketLocation;
                        lastPacketLocation = loc - 4L;
                    }
                } else
                {
                    read4 = false;
                }
                break;
            }
        }
        return EOMflag ? -2L : -1L;
    }

    private void parsePackHeader()
        throws IOException, BadDataException
    {
        byte buf1[] = new byte[1];
        readBytes(stream, buf1, 1);
        if((buf1[0] & 0xfffffff0) != 32)
            throw new BadDataException("invalid pack header");
        if((buf1[0] & 1) != 1)
        {
            throw new BadDataException("illegal marker bit");
        } else
        {
            skip(stream, 7);
            return;
        }
    }

    private void parseSystemHeader()
        throws IOException, BadHeaderException
    {
        byte buf1[] = new byte[1];
        int len = readShort(stream, true);
        if(sysHeaderSeen)
        {
            skip(stream, len);
        } else
        {
            sysHeader.resetSystemHeader();
            sysHeader.headerLen = len;
            int itmp = readInt(stream, true);
            len -= 4;
            if((itmp & 0x80000100) != 0x80000100)
                throw new BadHeaderException("illegal marker bits in system header");
            sysHeader.rateBound = (itmp & 0x7ffffe00) >> 9;
            sysHeader.audioBound = (itmp & 0xfc) >> 2;
            sysHeader.fixedFlag = (itmp & 2) >> 1;
            sysHeader.CSPSFlag = itmp & 1;
            readBytes(stream, buf1, 1);
            byte bval = buf1[0];
            len--;
            if((bval & 0x20) != 32)
                throw new BadHeaderException("illegal marker bits in system header");
            sysHeader.audioLockFlag = (bval & 0x80) >> 7;
            sysHeader.videoLockFlag = (bval & 0x40) >> 6;
            sysHeader.videoBound = bval & 0x1f;
            readBytes(stream, buf1, 1);
            len--;
            sysHeader.reserved = buf1[0];
            while(len > 1) 
            {
                readBytes(stream, buf1, 1);
                bval = buf1[0];
                len--;
                if((bval & 0xffffff80) != -128)
                    break;
                if(bval == -72)
                {
                    short stmp = readShort(stream, true);
                    len -= 2;
                    if((stmp & 0xc000) != 49152)
                        throw new BadHeaderException("illegal marker bits in system header");
                    int size = stmp & 0x1fff;
                    sysHeader.allAudioSTDFlag = true;
                    for(int i = 0; i <= 31; i++)
                    {
                        sysHeader.STDBufBoundScale[i] = 0;
                        sysHeader.STDBufSizeBound[i] = size;
                    }

                } else
                if(bval == -71)
                {
                    short stmp = readShort(stream, true);
                    len -= 2;
                    if((stmp & 0xc000) != 49152)
                        throw new BadHeaderException("illegal marker bits in system header");
                    int size = stmp & 0x1fff;
                    sysHeader.allVideoSTDFlag = true;
                    for(int i = 32; i <= 47; i++)
                    {
                        sysHeader.STDBufBoundScale[i] = 1;
                        sysHeader.STDBufSizeBound[i] = size;
                    }

                } else
                {
                    if((bval & 0xff) < 188 || (bval & 0xff) > 255)
                        throw new BadHeaderException("illegal track number in system header");
                    int streamID = getStreamID(bval);
                    if(streamID >= 0 && streamID < 48)
                    {
                        short stmp = readShort(stream, true);
                        len -= 2;
                        if((stmp & 0xc000) != 49152)
                            throw new BadHeaderException("illegal marker bits in system header");
                        int scale = (stmp & 0x2000) >> 13;
                        int size = stmp & 0x1fff;
                        sysHeader.streamFlags[streamID] = true;
                        sysHeader.STDBufBoundScale[streamID] = scale;
                        sysHeader.STDBufSizeBound[streamID] = size;
                    }
                }
            }
            if(len < 0)
                throw new BadHeaderException("illegal system header");
            if(len > 0)
                skip(stream, len);
            sysHeaderSeen = true;
        }
    }

    private void parsePacket(byte bval, boolean justLooking)
        throws IOException, BadDataException
    {
        int count = 0;
        int STDBufSize = 0;
        int STDBufScale = 0;
        int numWrittenToTmpBuf = 0;
        byte tmpBuf[] = null;
        byte buf1[] = new byte[1];
        if((bval & 0xff) < 188 || (bval & 0xff) > 255)
            throw new BadDataException("invalid stream(track) number");
        int streamID = getStreamID(bval);
        int packetLen = readShort(stream, true);
        buf1[0] = bval;
        if((buf1[0] & 0xff) != 191)
        {
            do
            {
                readBytes(stream, buf1, 1);
                count++;
            } while(buf1[0] == -1);
            if((buf1[0] & 0xffffffc0) == 64)
            {
                STDBufScale = (buf1[0] & 0x20) >> 5;
                STDBufSize = (buf1[0] & 0x1f) << 8;
                readBytes(stream, buf1, 1);
                STDBufSize |= buf1[0];
                readBytes(stream, buf1, 1);
                count += 2;
            }
            if((buf1[0] & 0xffffffe0) == 32)
            {
                long pts = (long)(buf1[0] & 0xe) << 29;
                pts = (pts << 31) >> 31;
                if((buf1[0] & 1) != 1)
                    throw new BadDataException("illegal marker bit");
                int itmp = readInt(stream, true);
                count += 4;
                if((itmp & 0x10001) != 0x10001)
                    throw new BadDataException("illegal marker bit");
                int itmp2 = (itmp & 0xfffe0000) >> 2;
                pts |= itmp2 & 0x3fffffff;
                pts |= (itmp & 0xfffe) >> 1;
                currentPTS = pts;
                if(startPTS == 0xffffffffffcd232bL)
                {
                    startPTS = currentPTS;
                    if(startPTS > 0L && (float)startPTS <= 45000F)
                        startPTS = 0L;
                }
                if((buf1[0] & 0xfffffff0) == 48)
                {
                    skip(stream, 5);
                    count += 5;
                }
            } else
            if(buf1[0] != 15)
                throw new BadDataException("invalid packet");
        }
        int dataSize = packetLen - count;
        if(justLooking)
        {
            skip(stream, dataSize);
            return;
        }
        if(streamID < 0 || streamID >= 48)
        {
            skip(stream, dataSize);
        } else
        {
            if(trackList[streamID] == null)
                trackList[streamID] = new TrackList();
            TrackList trackInfo = trackList[streamID];
            if(!trackInfo.infoFlag)
            {
                tmpBuf = new byte[dataSize];
                numWrittenToTmpBuf = extractStreamInfo(tmpBuf, streamID, dataSize, false);
            }
            if(!trackInfo.infoFlag)
            {
                trackList[streamID] = null;
                if(numWrittenToTmpBuf < dataSize)
                    skip(stream, dataSize - numWrittenToTmpBuf);
            } else
            {
                if(startPTS == 0xffffffffffcd232bL)
                    trackInfo.startPTS = currentPTS;
                trackInfo.copyStreamDataToInnerBuffer(tmpBuf, numWrittenToTmpBuf, dataSize - numWrittenToTmpBuf, currentPTS);
                trackInfo.numPackets++;
                if(dataSize > trackInfo.maxPacketSize)
                    trackInfo.maxPacketSize = dataSize;
            }
        }
        numPackets++;
    }

    private int extractStreamInfo(byte tmpBuf[], int streamID, int dataLen, boolean AVOnlyState)
        throws IOException, BadDataException
    {
        byte stype = 0;
        TrackList trackInfo = trackList[streamID];
        if(trackInfo.trackType == 0)
        {
            stype = AVOnlyState ? streamType : ((byte)(streamID >= 32 ? 2 : 1));
            trackInfo.init(stype);
            sysHeader.streamFlags[streamID] = true;
            trackInfo.startPTS = currentPTS;
        }
        int numBytes;
        if(stype == 1)
            numBytes = extractAudioInfo(tmpBuf, trackInfo, dataLen, AVOnlyState);
        else
            numBytes = extractVideoInfo(tmpBuf, trackInfo, dataLen, AVOnlyState);
        if(trackInfo.infoFlag)
            if(AVOnlyState)
            {
                tracks[0] = new MediaTrack(trackInfo);
            } else
            {
                tracks[numTracks] = new MediaTrack(trackInfo);
                numTracks++;
            }
        return numBytes;
    }

    private int extractAudioInfo(byte tmpBuf[], TrackList trackInfo, int dataLen, boolean AVOnlyState)
        throws IOException, BadDataException
    {
        Audio audio = new Audio();
        int samplingFrequencyTable[] = {
            44100, 48000, 32000
        };
        short bitrateIndexTableL2[] = {
            0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 
            192, 224, 256, 320, 384
        };
        short bitrateIndexTableL23Ext[] = {
            0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 
            96, 112, 128, 144, 160
        };
        int numBytes = AVOnlyState ? dataLen : readBytes(stream, tmpBuf, dataLen);
        for(int i = (int)startLocation; i < numBytes - 3; i++)
        {
            if(tmpBuf[i] != -1 || (tmpBuf[i + 1] & 0xfffffff0) != -16)
                continue;
            audio.ID = (tmpBuf[i + 1] & 8) >> 3;
            audio.layer = 4 - ((tmpBuf[i + 1] & 6) >> 1);
            audio.protection = tmpBuf[i + 1] & 1;
            int br = (tmpBuf[i + 2] & 0xf0) >> 4;
            int sr = (tmpBuf[i + 2] & 0xc) >> 2;
            if(sr < 0 || sr >= samplingFrequencyTable.length)
                throw new BadDataException("Non Standard sample rates not supported");
            audio.mode = (tmpBuf[i + 3] & 0xc0) >> 6;
            audio.modeExt = (tmpBuf[i + 3] & 0x30) >> 4;
            audio.channels = audio.mode != 3 ? 2 : 1;
            audio.copyright = (tmpBuf[i + 3] & 8) >> 3;
            audio.original = (tmpBuf[i + 3] & 4) >> 2;
            audio.emphasis = tmpBuf[i + 3] & 3;
            audio.valid = br != 15;
            if(audio.ID == 1)
            {
                audio.sampleRate = samplingFrequencyTable[sr];
                if(audio.layer == 3)
                {
                    if(br < 2)
                        audio.bitRate = bitrateIndexTableL2[br];
                    else
                    if(br == 2)
                        audio.bitRate = 40;
                    else
                        audio.bitRate = bitrateIndexTableL2[br - 1];
                } else
                if(audio.layer == 2)
                    audio.bitRate = bitrateIndexTableL2[br];
                else
                    audio.bitRate = br << 5;
            } else
            {
                audio.sampleRate = samplingFrequencyTable[sr] >> 1;
                if(audio.layer == 3 || audio.layer == 2)
                    audio.bitRate = bitrateIndexTableL23Ext[br];
                else
                if(br < 9)
                    audio.bitRate = bitrateIndexTableL2[br];
                else
                if(br == 9)
                    audio.bitRate = 144;
                else
                if(br == 10)
                    audio.bitRate = bitrateIndexTableL2[br - 1];
                else
                if(br == 11)
                    audio.bitRate = 176;
                else
                    audio.bitRate = bitrateIndexTableL2[br - 2];
            }
            trackInfo.readFrameSize = audio.bitRate * 1000 >> 3;
            trackInfo.infoFlag = true;
            trackInfo.media = audio;
            break;
        }

        return numBytes;
    }

    private int extractVideoInfo(byte tmpBuf[], TrackList trackInfo, int dataLen, boolean AVOnlyState)
        throws IOException, BadDataException
    {
        Video video = new Video();
        float aspectRatioTable[] = {
            0.0F, 1.0F, 0.6735F, 0.7031F, 0.7615F, 0.8055F, 0.8437F, 0.8935F, 0.9375F, 0.9815F, 
            1.0255F, 1.0695F, 1.125F, 1.1575F, 1.2015F, 1.0F
        };
        float pictureRateTable[] = {
            0.0F, 23.976F, 24F, 25F, 29.97F, 30F, 50F, 59.94F, 60F, -1F, 
            -1F, -1F, -1F, -1F, -1F, -1F
        };
        int numBytes = AVOnlyState ? dataLen : readBytes(stream, tmpBuf, dataLen);
        for(int i = 0; i < numBytes - 10; i++)
        {
            int code = tmpBuf[i] << 24 & 0xff000000 | tmpBuf[i + 1] << 16 & 0xff0000 | tmpBuf[i + 2] << 8 & 0xff00 | tmpBuf[i + 3] & 0xff;
            if(code != 435)
                continue;
            video.width = (tmpBuf[i + 4 + 0] & 0xff) << 4;
            video.width |= tmpBuf[i + 4 + 1] >> 4 & 0xf;
            video.height = (tmpBuf[i + 4 + 1] & 0xf) << 8;
            video.height |= tmpBuf[i + 4 + 2] & 0xff;
            int pr = (tmpBuf[i + 4 + 3] & 0xf0) >> 4;
            video.pelAspectRatio = aspectRatioTable[pr];
            pr = tmpBuf[i + 4 + 3] & 0xf;
            video.pictureRate = pictureRateTable[pr];
            pr = (tmpBuf[i + 4 + 4] & 0xff) << 10 | (tmpBuf[i + 4 + 5] & 0xff) << 2 | (tmpBuf[i + 4 + 6] & 0xc0) >> 6;
            video.bitRate = pr * 400;
            if((double)video.pelAspectRatio == 0.0D || (double)video.pictureRate == 0.0D)
                throw new BadDataException("video header corrupted");
            if((double)video.pictureRate < 23D)
            {
                trackInfo.readFrameSize = 0x10000;
            } else
            {
                trackInfo.readFrameSize = video.bitRate >> 3;
                if(trackInfo.readFrameSize > 0x186a0)
                    trackInfo.readFrameSize = 0x186a0;
            }
            trackInfo.infoFlag = true;
            trackInfo.media = video;
            break;
        }

        return numBytes;
    }

    private int getStreamID(byte bval)
    {
        return (bval & 0xff) - 192;
    }

    private long getLocation()
    {
        return getLocation(stream);
    }

    boolean needingMore()
    {
        for(int i = 0; i < numTracks; i++)
            if(tracks[i] != null)
            {
                TrackList trackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                if(trackInfo.bufQ.canRead())
                    return false;
            }

        return true;
    }

    void flushInnerBuffers()
    {
        for(int i = 0; i < numTracks; i++)
            if(tracks[i] != null)
            {
                TrackList trackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                synchronized(trackInfo.bufQ)
                {
                    trackInfo.flushFlag = true;
                    trackInfo.bufQ.notifyAll();
                }
                trackInfo.flushBuffer();
            }

    }

    void saveInnerBuffersToFiles()
    {
        for(int i = 0; i < numTracks; i++)
            if(tracks[i] != null)
            {
                TrackList trackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                trackInfo.saveBufToFile();
            }

    }

    void throwInnerBuffersContents()
    {
        for(int i = 0; i < numTracks; i++)
            if(tracks[i] != null)
            {
                TrackList trackInfo = ((MediaTrack)tracks[i]).getTrackInfo();
                trackInfo.flushBuffer();
            }

    }

    boolean saveOutputFlag;
    String AoutName;
    String VoutName;
    FileOutputStream aout;
    FileOutputStream vout;
    boolean throwOutputFlag;
    boolean hideAudioTracks;
    boolean hideVideoTracks;
    static final long NO_PTS_VAL = 0xffffffffffcd232bL;
    private static final float EPSILON_PTS = 45000F;
    private static final float EPSILON_NS = 5E+008F;
    private static final long PRE_ROLLING_DELTA_NS = 0x1dcd6500L;
    private static final byte UNKNOWN_TYPE = 0;
    private static final byte AUDIO_TYPE = 1;
    private static final byte VIDEO_TYPE = 2;
    private static final byte SYS11172_TYPE = 3;
    private static final int AUDIO_TRACK_BUF_SIZE = 0x186a0;
    private static final int VIDEO_TRACK_BUF_SIZE = 0x30d40;
    private static final int PACK_START_CODE = 442;
    private static final int SYSTEM_HEADER_START_CODE = 443;
    private static final int PACKET_START_CODE_24 = 1;
    private static final int END_CODE = 441;
    private static final int MIN_STREAM_CODE = 188;
    private static final int MAX_STREAM_CODE = 255;
    private static final int PRIVATE_STREAM2_CODE = 191;
    private static final int VIDEO_PICTURE_START_CODE = 256;
    private static final int VIDEO_SEQUENCE_HEADER_CODE = 435;
    private static final int VIDEO_GROUP_START_CODE = 440;
    private static final int MAX_AUDIO_STREAMS = 32;
    private static final int MAX_VIDEO_STREAMS = 16;
    private static final int MAX_NUM_STREAMS = 48;
    private static final int MIN_AUDIO_ID = 0;
    private static final int MAX_AUDIO_ID = 31;
    private static final int MIN_VIDEO_ID = 32;
    private static final int MAX_VIDEO_ID = 47;
    private static int MAX_TRACKS_SUPPORTED = 48;
    private static ContentDescriptor supportedFormat[] = {
        new ContentDescriptor("audio.mpeg"), new ContentDescriptor("video.mpeg"), new ContentDescriptor("audio.mpeg")
    };
    private PullSourceStream stream;
    private TrackList trackList[];
    private Track tracks[];
    private Track videoTracks[];
    private Track audioTracks[];
    private int videoCount;
    private int audioCount;
    private int numSupportedTracks;
    private int numTracks;
    private int numPackets;
    private int initTmpBufLen;
    private byte initTmpStreamBuf[];
    private byte streamType;
    private long streamContentLength;
    private SystemHeader sysHeader;
    private boolean sysHeaderSeen;
    boolean EOMflag;
    boolean parserErrorFlag;
    private boolean durationInitialized;
    private boolean sysPausedFlag;
    private boolean seekableStreamFlag;
    private boolean randomAccessStreamFlag;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method mSecurity[];
    private Class clSecurity[];
    private Object argsSecurity[][];
    private long startLocation;
    private Time durationNs;
    private Time lastSetPositionTime;
    private long startPTS;
    long currentPTS;
    long endPTS;
    private long AVstartTimeNs;
    private long AVcurrentTimeNs;
    private long AVlastTimeNs;
    private long lastAudioNs;
    private MpegBufferThread mpThread;
    static int bitrates[][][] = {
        {
            {
                -1
            }, {
                0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 
                96, 112, 128, 144, 160, -1
            }, {
                0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 
                96, 112, 128, 144, 160, -1
            }, {
                0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 
                160, 176, 192, 224, 256, -1
            }
        }, {
            {
                -1
            }
        }, {
            {
                -1
            }, {
                0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 
                96, 112, 128, 144, 160, -1
            }, {
                0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 
                96, 112, 128, 144, 160, -1
            }, {
                0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 
                160, 176, 192, 224, 256, -1
            }
        }, {
            {
                -1
            }, {
                0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 
                160, 192, 224, 256, 320, -1
            }, {
                0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 
                192, 224, 256, 320, 384, -1
            }, {
                0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 
                320, 352, 384, 416, 448, -1
            }
        }
    };
    static int samplerates[][] = {
        {
            11025, 12000, 8000, -1
        }, {
            -1
        }, {
            22050, 24000, 16000, -1
        }, {
            44100, 48000, 32000, -1
        }
    };

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
