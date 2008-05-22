// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RawBufferParser.java

package com.sun.media.parser;

import com.sun.media.BasicPlugIn;
import com.sun.media.CircularBuffer;
import com.sun.media.protocol.DelegateDataSource;
import com.sun.media.rtp.Depacketizer;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Vector;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media.parser:
//            RawStreamParser, RawParser

public class RawBufferParser extends RawStreamParser
{
    class FrameTrack
        implements Track, BufferTransferHandler
    {

        public Format getFormat()
        {
            return format;
        }

        public void setEnabled(boolean t)
        {
            if(t)
                pbs.setTransferHandler(this);
            else
                pbs.setTransferHandler(null);
            enabled = t;
        }

        public boolean isEnabled()
        {
            return enabled;
        }

        public Time getDuration()
        {
            return parser.getDuration();
        }

        public Time getStartTime()
        {
            return new Time(0L);
        }

        public void setTrackListener(TrackListener l)
        {
            listener = l;
        }

        public void parse()
        {
            try
            {
                synchronized(keyFrameLock)
                {
                    while(!keyFrameFound) 
                        keyFrameLock.wait();
                }
            }
            catch(Exception e) { }
        }

        private Depacketizer findDepacketizer(String name, Format input)
        {
            try
            {
                Class cls = BasicPlugIn.getClassForName(name);
                Object obj = cls.newInstance();
                if(!(obj instanceof Depacketizer))
                    return null;
                Depacketizer dpktizer = (Depacketizer)obj;
                if(dpktizer.setInputFormat(input) == null)
                {
                    return null;
                } else
                {
                    dpktizer.open();
                    return dpktizer;
                }
            }
            catch(Exception e) { }
            catch(Error e) { }
            return null;
        }

        private boolean findKeyFrame(Buffer buf)
        {
            if(!checkDepacketizer)
            {
                Vector pnames = PlugInManager.getPlugInList(buf.getFormat(), null, 6);
                if(pnames.size() != 0)
                    depacketizer = findDepacketizer((String)pnames.elementAt(0), buf.getFormat());
                checkDepacketizer = true;
            }
            Format fmt = buf.getFormat();
            if(fmt == null)
                return false;
            if(fmt.getEncoding() == null)
            {
                synchronized(keyFrameLock)
                {
                    keyFrameFound = true;
                    keyFrameLock.notifyAll();
                }
                return true;
            }
            boolean rtn = true;
            if(RawBufferParser.jpegVideo.matches(fmt))
                rtn = findJPEGKey(buf);
            else
            if(RawBufferParser.h261Video.matches(fmt))
                rtn = findH261Key(buf);
            else
            if(RawBufferParser.h263Video.matches(fmt))
                rtn = findH263Key(buf);
            else
            if(RawBufferParser.h263_1998Video.matches(fmt))
                rtn = findH263_1998Key(buf);
            else
            if(RawBufferParser.mpegVideo.matches(fmt))
                rtn = findMPEGKey(buf);
            else
            if(RawBufferParser.mpegAudio.matches(fmt))
                rtn = findMPAKey(buf);
            else
            if(depacketizer != null)
            {
                fmt = depacketizer.parse(buf);
                if(fmt != null)
                {
                    format = fmt;
                    buf.setFormat(format);
                    depacketizer.close();
                    depacketizer = null;
                } else
                {
                    rtn = false;
                }
            }
            if(rtn)
                synchronized(keyFrameLock)
                {
                    keyFrameFound = true;
                    keyFrameLock.notifyAll();
                }
            return keyFrameFound;
        }

        public boolean findJPEGKey(Buffer b)
        {
            if((b.getFlags() & 0x800) == 0)
            {
                return false;
            } else
            {
                byte data[] = (byte[])b.getData();
                int width = (data[b.getOffset() + 6] & 0xff) * 8;
                int height = (data[b.getOffset() + 7] & 0xff) * 8;
                format = new VideoFormat("jpeg/rtp", new Dimension(width, height), ((VideoFormat)format).getMaxDataLength(), ((VideoFormat)format).getDataType(), ((VideoFormat)format).getFrameRate());
                b.setFormat(format);
                return true;
            }
        }

        public boolean findH261Key(Buffer b)
        {
            byte data[];
            if((data = (byte[])b.getData()) == null)
                return false;
            int offset = b.getOffset();
            int skipBytes = 4;
            if(data[offset + skipBytes] != 0 || data[offset + skipBytes + 1] != 1 || (data[offset + skipBytes + 2] & 0xfc) != 0)
            {
                return false;
            } else
            {
                int s = data[offset + skipBytes + 3] >> 3 & 1;
                int width = h261Widths[s];
                int height = h261Heights[s];
                format = new VideoFormat("h261/rtp", new Dimension(width, height), ((VideoFormat)format).getMaxDataLength(), ((VideoFormat)format).getDataType(), ((VideoFormat)format).getFrameRate());
                b.setFormat(format);
                return true;
            }
        }

        public boolean findH263Key(Buffer b)
        {
            byte data[];
            if((data = (byte[])b.getData()) == null)
                return false;
            int payloadLen = getH263PayloadHeaderLength(data, b.getOffset());
            int offset = b.getOffset();
            if(data[offset + payloadLen] != 0 || data[offset + payloadLen + 1] != 0 || (data[offset + payloadLen + 2] & 0xfc) != 128)
            {
                return false;
            } else
            {
                int s = data[offset + payloadLen + 4] >> 2 & 7;
                int width = h263Widths[s];
                int height = h263Heights[s];
                format = new VideoFormat("h263/rtp", new Dimension(width, height), ((VideoFormat)format).getMaxDataLength(), ((VideoFormat)format).getDataType(), ((VideoFormat)format).getFrameRate());
                b.setFormat(format);
                return true;
            }
        }

        int getH263PayloadHeaderLength(byte input[], int offset)
        {
            int l = 0;
            byte b = input[offset];
            if((b & 0x80) != 0)
            {
                if((b & 0x40) != 0)
                    l = 12;
                else
                    l = 8;
            } else
            {
                l = 4;
            }
            return l;
        }

        public boolean findH263_1998Key(Buffer b)
        {
            int s = -1;
            int picOffset = -1;
            byte data[];
            if((data = (byte[])b.getData()) == null)
                return false;
            int offset = b.getOffset();
            int payloadLen = 2 + ((data[offset] & 1) << 5 | (data[offset + 1] & 0xf8) >> 3);
            if((data[offset] & 2) != 0)
                payloadLen++;
            picOffset = -1;
            if(payloadLen > 5)
            {
                if((data[offset] & 2) == 2 && (data[offset + 3] & 0xfc) == 128)
                    picOffset = offset + 3;
                else
                if((data[offset + 2] & 0xfc) == 128)
                    picOffset = offset + 2;
            } else
            if((data[offset] & 4) == 4 && (data[offset + payloadLen] & 0xfc) == 128)
                picOffset = offset + payloadLen;
            if(picOffset < 0)
                return false;
            s = data[picOffset + 2] >> 2 & 7;
            if(s == 7)
                if((data[picOffset + 3] >> 1 & 7) == 1)
                    s = data[picOffset + 3] << 2 & 4 | data[picOffset + 4] >> 6 & 3;
                else
                    return false;
            if(s < 0)
            {
                return false;
            } else
            {
                int width = h263Widths[s];
                int height = h263Heights[s];
                format = new VideoFormat("h263-1998/rtp", new Dimension(width, height), ((VideoFormat)format).getMaxDataLength(), ((VideoFormat)format).getDataType(), ((VideoFormat)format).getFrameRate());
                b.setFormat(format);
                return true;
            }
        }

        public boolean findMPEGKey(Buffer b)
        {
            byte data[];
            if((data = (byte[])b.getData()) == null)
                return false;
            int off = b.getOffset();
            if(b.getLength() < 12)
                return false;
            if((data[off + 2] & 0x20) != 32)
                return false;
            if(data[off + 4] != 0 || data[off + 5] != 0 || data[off + 6] != 1 || (data[off + 7] & 0xff) != 179)
                return false;
            int frix = data[off + 11] & 0xf;
            if(frix == 0 || frix > 8)
            {
                return false;
            } else
            {
                int width = (data[off + 8] & 0xff) << 4 | (data[off + 9] & 0xf0) >> 4;
                int height = (data[off + 9] & 0xf) << 8 | data[off + 10] & 0xff;
                float frameRate = MPEGRateTbl[frix];
                format = new VideoFormat("mpeg/rtp", new Dimension(width, height), ((VideoFormat)format).getMaxDataLength(), ((VideoFormat)format).getDataType(), frameRate);
                b.setFormat(format);
                return true;
            }
        }

        public boolean findMPAKey(Buffer b)
        {
            byte data[];
            if((data = (byte[])b.getData()) == null)
                return false;
            int off = b.getOffset();
            if(b.getLength() < 8)
                return false;
            if(data[off + 2] != 0 || data[off + 3] != 0)
                return false;
            off += 4;
            if((data[off] & 0xff) != 255 || (data[off + 1] & 0xf6) <= 240 || (data[off + 2] & 0xf0) == 240 || (data[off + 2] & 0xc) == 12 || (data[off + 3] & 3) == 2)
            {
                return false;
            } else
            {
                int id = data[off + 1] >> 3 & 1;
                int six = data[off + 2] >> 2 & 3;
                int channels = (data[off + 3] >> 6 & 3) != 3 ? 2 : 1;
                double sampleRate = RawBufferParser.MPASampleTbl[id][six];
                format = new AudioFormat("mpegaudio/rtp", sampleRate, 16, channels, 0, 1);
                b.setFormat(format);
                return true;
            }
        }

        public void stop()
        {
            synchronized(bufferQ)
            {
                stopped = true;
                bufferQ.notifyAll();
            }
        }

        public void start()
        {
            synchronized(bufferQ)
            {
                stopped = false;
                if(source instanceof CaptureDevice)
                    for(; bufferQ.canRead(); bufferQ.readReport())
                        bufferQ.read();

                bufferQ.notifyAll();
            }
        }

        public void close()
        {
            setEnabled(false);
            synchronized(bufferQ)
            {
                closed = true;
                bufferQ.notifyAll();
            }
        }

        public void reset()
        {
        }

        public void readFrame(Buffer buffer)
        {
            if(stopped)
            {
                buffer.setDiscard(true);
                buffer.setFormat(format);
                return;
            }
            Buffer filled;
            synchronized(bufferQ)
            {
                while(!bufferQ.canRead()) 
                    try
                    {
                        bufferQ.wait();
                        if(stopped)
                        {
                            buffer.setDiscard(true);
                            buffer.setFormat(format);
                            return;
                        }
                    }
                    catch(Exception e) { }
                filled = bufferQ.read();
            }
            Object hdr = buffer.getHeader();
            buffer.copy(filled, true);
            filled.setHeader(hdr);
            format = filled.getFormat();
            synchronized(bufferQ)
            {
                bufferQ.readReport();
                bufferQ.notifyAll();
            }
        }

        public int mapTimeToFrame(Time t)
        {
            return -1;
        }

        public Time mapFrameToTime(int frameNumber)
        {
            return new Time(0L);
        }

        public void transferData(PushBufferStream pbs)
        {
            Buffer buffer;
            synchronized(bufferQ)
            {
                while(!bufferQ.canWrite() && !closed) 
                    try
                    {
                        bufferQ.wait();
                    }
                    catch(Exception e) { }
                if(closed)
                    return;
                buffer = bufferQ.getEmptyBuffer();
            }
            try
            {
                pbs.read(buffer);
            }
            catch(IOException e)
            {
                buffer.setDiscard(true);
            }
            if(!keyFrameFound && !findKeyFrame(buffer))
            {
                synchronized(bufferQ)
                {
                    bufferQ.writeReport();
                    bufferQ.read();
                    bufferQ.readReport();
                }
                return;
            }
            synchronized(bufferQ)
            {
                bufferQ.writeReport();
                bufferQ.notifyAll();
            }
        }

        Demultiplexer parser;
        PushBufferStream pbs;
        boolean enabled;
        CircularBuffer bufferQ;
        Format format;
        TrackListener listener;
        boolean stopped;
        boolean closed;
        boolean keyFrameFound;
        boolean checkDepacketizer;
        Depacketizer depacketizer;
        Object keyFrameLock;

        public FrameTrack(Demultiplexer parser, PushBufferStream pbs, int numOfBufs)
        {
            enabled = true;
            format = null;
            stopped = true;
            closed = false;
            keyFrameFound = false;
            checkDepacketizer = false;
            depacketizer = null;
            keyFrameLock = new Object();
            this.pbs = pbs;
            format = pbs.getFormat();
            if((source instanceof DelegateDataSource) || !isRTPFormat(format))
                keyFrameFound = true;
            bufferQ = new CircularBuffer(numOfBufs);
            pbs.setTransferHandler(this);
        }
    }


    public RawBufferParser()
    {
        started = false;
    }

    public String getName()
    {
        return "Raw video/audio buffer stream parser";
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        if(!(source instanceof PushBufferDataSource))
            throw new IncompatibleSourceException("DataSource not supported: " + source);
        super.streams = ((PushBufferDataSource)source).getStreams();
        if(super.streams == null)
            throw new IOException("Got a null stream from the DataSource");
        if(super.streams.length == 0)
            throw new IOException("Got a empty stream array from the DataSource");
        if(!supports(super.streams))
        {
            throw new IncompatibleSourceException("DataSource not supported: " + source);
        } else
        {
            super.source = source;
            super.streams = super.streams;
            return;
        }
    }

    protected boolean supports(SourceStream streams[])
    {
        return streams[0] != null && (streams[0] instanceof PushBufferStream);
    }

    public void open()
    {
        if(super.tracks != null)
            return;
        super.tracks = new Track[super.streams.length];
        for(int i = 0; i < super.streams.length; i++)
            super.tracks[i] = new FrameTrack(this, (PushBufferStream)super.streams[i], 1);

    }

    public void close()
    {
        if(super.source != null)
        {
            try
            {
                super.source.stop();
                for(int i = 0; i < super.tracks.length; i++)
                {
                    ((FrameTrack)super.tracks[i]).stop();
                    ((FrameTrack)super.tracks[i]).close();
                }

                super.source.disconnect();
            }
            catch(Exception e) { }
            super.source = null;
        }
        started = false;
    }

    public Track[] getTracks()
    {
        for(int i = 0; i < super.tracks.length; i++)
            ((FrameTrack)super.tracks[i]).parse();

        return super.tracks;
    }

    public void start()
        throws IOException
    {
        for(int i = 0; i < super.tracks.length; i++)
            ((FrameTrack)super.tracks[i]).start();

        super.source.start();
        started = true;
    }

    public void stop()
    {
        try
        {
            super.source.stop();
            for(int i = 0; i < super.tracks.length; i++)
                ((FrameTrack)super.tracks[i]).stop();

        }
        catch(Exception e) { }
        started = false;
    }

    public void reset()
    {
        for(int i = 0; i < super.tracks.length; i++)
            ((FrameTrack)super.tracks[i]).reset();

    }

    boolean isRTPFormat(Format fmt)
    {
        return fmt != null && fmt.getEncoding() != null && (fmt.getEncoding().endsWith("rtp") || fmt.getEncoding().endsWith("RTP"));
    }

    static final String NAMEBUFFER = "Raw video/audio buffer stream parser";
    private boolean started;
    static AudioFormat mpegAudio = new AudioFormat("mpegaudio/rtp");
    static VideoFormat mpegVideo = new VideoFormat("mpeg/rtp");
    static VideoFormat jpegVideo = new VideoFormat("jpeg/rtp");
    static VideoFormat h261Video = new VideoFormat("h261/rtp");
    static VideoFormat h263Video = new VideoFormat("h263/rtp");
    static VideoFormat h263_1998Video = new VideoFormat("h263-1998/rtp");
    final int h261Widths[] = {
        176, 352
    };
    final int h261Heights[] = {
        144, 288
    };
    final int h263Widths[] = {
        0, 128, 176, 352, 704, 1408, 0, 0
    };
    final int h263Heights[] = {
        0, 96, 144, 288, 576, 1152, 0, 0
    };
    final float MPEGRateTbl[] = {
        0.0F, 23.976F, 24F, 25F, 29.97F, 30F, 50F, 59.94F, 60F
    };
    public static int MPASampleTbl[][] = {
        {
            22050, 24000, 16000, 0
        }, {
            44100, 48000, 32000, 0
        }
    };

}
