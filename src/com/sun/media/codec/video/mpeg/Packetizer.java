// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Packetizer.java

package com.sun.media.codec.video.mpeg;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import java.io.PrintStream;
import java.util.Vector;
import javax.media.*;
import javax.media.format.VideoFormat;

public class Packetizer extends BasicCodec
{
    class MPEGSegment
    {

        void copyData(byte dest[], int outoffset)
        {
            copyData(0, length, dest, outoffset);
        }

        void copyData(int off, byte dest[], int outoffset)
        {
            copyData(off, length - off, dest, outoffset);
        }

        void copyData(int off, int len, byte dest[], int outoffset)
        {
            if(off + len > length)
                len = length - off;
            if(endBuffer == null)
            {
                System.arraycopy(startBuffer.getData(), offset + off, dest, outoffset, len);
                return;
            }
            int len1 = startBuffer.getLength() - (offset - startBuffer.getOffset());
            int len2 = length - len1;
            if(off + len <= len1)
            {
                System.arraycopy(startBuffer.getData(), offset + off, dest, outoffset, len);
                return;
            }
            if(off >= len1)
            {
                off -= len1;
                System.arraycopy(endBuffer.getData(), endBuffer.getOffset() + off, dest, outoffset, len);
                return;
            } else
            {
                int l = len1 - off;
                System.arraycopy(startBuffer.getData(), offset + off, dest, outoffset, l);
                len -= l;
                System.arraycopy(endBuffer.getData(), endBuffer.getOffset(), dest, outoffset + l, len);
                return;
            }
        }

        int getLength()
        {
            if(length < 0)
                calculateLength();
            return length;
        }

        private void calculateLength()
        {
            if(length > 0)
                return;
            int off = findNextStart();
            if(off > offset)
            {
                length = off - offset;
                return;
            }
            if(inputEOM)
            {
                length = startBuffer.getLength() - (offset - startBuffer.getOffset());
                return;
            }
            if(endBuffer == null)
            {
                if(inputQueue.isEmpty())
                    return;
                endBuffer = (Buffer)inputQueue.firstElement();
                inputQueue.removeElementAt(0);
            }
            off = findNextStartBetweenBuffers();
            if(off > offset)
            {
                length = off - offset;
                return;
            } else
            {
                off = findNextStartInEndBuffer();
                length = startBuffer.getLength() - (offset - startBuffer.getOffset());
                length += off - endBuffer.getOffset();
                return;
            }
        }

        private int findNextStart()
        {
            byte inData[] = (byte[])startBuffer.getData();
            int off = offset + 4;
            for(int len = startBuffer.getLength() - ((offset + 4) - startBuffer.getOffset()); len > 3; len--)
            {
                if(inData[off] == 0 && inData[off + 1] == 0 && inData[off + 2] == 1 && (inData[off + 3] & 0xff) != 181 && (inData[off + 3] & 0xff) != 178)
                {
                    MPEGSegment ns = new MPEGSegment(inData[off + 3] & 0xff, off, startBuffer);
                    segmentQueue.addElement(ns);
                    return off;
                }
                off++;
            }

            return -1;
        }

        private int findNextStartBetweenBuffers()
        {
            byte inData[] = (byte[])startBuffer.getData();
            byte inData2[] = (byte[])endBuffer.getData();
            int off = (startBuffer.getOffset() + startBuffer.getLength()) - 3;
            if(off <= offset)
                return -1;
            int off2 = endBuffer.getOffset();
            if(inData[off] == 0 && inData[off + 1] == 0 && inData[off + 2] == 1 && (inData2[off2] & 0xff) != 181 && (inData[off2] & 0xff) != 178)
            {
                MPEGSegment ns = new MPEGSegment(inData2[off2] & 0xff, off, startBuffer);
                ns.endBuffer = endBuffer;
                segmentQueue.addElement(ns);
                endBuffer = null;
                return off;
            }
            if(inData[off + 1] == 0 && inData[off + 2] == 0 && inData2[off2] == 1 && (inData2[off2 + 1] & 0xff) != 181 && (inData[off2 + 1] & 0xff) != 178)
            {
                MPEGSegment ns = new MPEGSegment(inData2[off2 + 1] & 0xff, off + 1, startBuffer);
                ns.endBuffer = endBuffer;
                segmentQueue.addElement(ns);
                endBuffer = null;
                return off + 1;
            }
            if(inData[off + 2] == 0 && inData2[off2] == 0 && inData2[off2 + 1] == 1 && (inData2[off2 + 2] & 0xff) != 181 && (inData[off2 + 2] & 0xff) != 178)
            {
                MPEGSegment ns = new MPEGSegment(inData2[off2 + 2] & 0xff, off + 2, startBuffer);
                ns.endBuffer = endBuffer;
                segmentQueue.addElement(ns);
                endBuffer = null;
                return off + 2;
            } else
            {
                return -1;
            }
        }

        private int findNextStartInEndBuffer()
        {
            byte inData[] = (byte[])endBuffer.getData();
            int off = endBuffer.getOffset();
            for(int len = endBuffer.getLength(); len > 3; len--)
            {
                if(inData[off] == 0 && inData[off + 1] == 0 && inData[off + 2] == 1 && (inData[off + 3] & 0xff) != 181 && (inData[off + 3] & 0xff) != 178)
                {
                    MPEGSegment ns = new MPEGSegment(inData[off + 3] & 0xff, off, endBuffer);
                    segmentQueue.addElement(ns);
                    return off;
                }
                off++;
            }

            return -1;
        }

        int startCode;
        int offset;
        int length;
        Buffer startBuffer;
        Buffer endBuffer;

        MPEGSegment(int code, int off, Buffer buf)
        {
            startCode = -1;
            offset = -1;
            length = -1;
            startBuffer = null;
            endBuffer = null;
            startCode = code;
            offset = off;
            startBuffer = buf;
        }
    }


    public Packetizer()
    {
        inputFormat = null;
        outputFormat = null;
        inputEOM = false;
        expectingNewInput = true;
        expectingNewOutput = true;
        resetTime = true;
        resetInProgress = true;
        outputQueue = new Vector();
        inputQueue = new Vector();
        segmentQueue = new Vector();
        sequenceHeader = null;
        frameWidth = 0;
        frameHeight = 0;
        frameRate = 0.0D;
        picNanos = 0L;
        gopTime = 0L;
        startTime = 1L;
        frameTime = 0L;
        frameCount = 0L;
        sequenceNumber = 0;
        super.inputFormats = (new Format[] {
            new VideoFormat("mpeg")
        });
        super.outputFormats = (new Format[] {
            new VideoFormat("mpeg/rtp")
        });
    }

    protected Format getInputFormat()
    {
        return inputFormat;
    }

    protected Format getOutputFormat()
    {
        return outputFormat;
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        if(in == null)
            return super.outputFormats;
        if(BasicPlugIn.matches(in, super.inputFormats) == null)
        {
            return new Format[0];
        } else
        {
            Format out[] = new Format[1];
            out[0] = makeMPEGFormat(in);
            return out;
        }
    }

    public Format setInputFormat(Format input)
    {
        inputFormat = (VideoFormat)input;
        return input;
    }

    public Format setOutputFormat(Format output)
    {
        if(!(output instanceof VideoFormat))
        {
            return null;
        } else
        {
            outputFormat = makeMPEGFormat(output);
            return output;
        }
    }

    private final VideoFormat makeMPEGFormat(Format in)
    {
        VideoFormat vf = (VideoFormat)in;
        return new VideoFormat("mpeg/rtp", vf.getSize(), -1, Format.byteArray, vf.getFrameRate());
    }

    public void open()
        throws ResourceUnavailableException
    {
        if(inputFormat == null || outputFormat == null)
        {
            throw new ResourceUnavailableException("Incorrect formats set on MPEG converter");
        } else
        {
            startTime = 1L;
            frameRate = 0.0D;
            picNanos = 0L;
            sequenceNumber = 0;
            resetTime = true;
            return;
        }
    }

    public synchronized void close()
    {
        reset();
    }

    public void reset()
    {
        super.reset();
        outputQueue.removeAllElements();
        inputQueue.removeAllElements();
        segmentQueue.removeAllElements();
        inputEOM = false;
        expectingNewInput = true;
        expectingNewOutput = true;
        resetInProgress = true;
        resetTime = true;
        sequenceHeader = null;
        frameWidth = 0;
        frameHeight = 0;
        mpegHeader[0] = 0;
        mpegHeader[1] = 0;
        mpegHeader[2] = 0;
        mpegHeader[3] = 0;
        gopTime = 1L;
        frameTime = 0L;
        frameCount = 0L;
        if(debug)
            System.err.println("Packetizer(V): reset completed");
    }

    public synchronized int process(Buffer inBuffer, Buffer outBuffer)
    {
        if(outputQueue.size() > 0)
        {
            Buffer qbuf = (Buffer)outputQueue.firstElement();
            outputQueue.removeElementAt(0);
            outBuffer.setData((byte[])qbuf.getData());
            outBuffer.setOffset(qbuf.getOffset());
            outBuffer.setLength(qbuf.getLength());
            outBuffer.setFlags(qbuf.getFlags());
            outBuffer.setTimeStamp(qbuf.getTimeStamp());
            outBuffer.setSequenceNumber(sequenceNumber++);
            outBuffer.setFormat(outputFormat);
            expectingNewOutput = true;
            return 2;
        }
        if(isEOM(inBuffer))
        {
            inputEOM = true;
            if(segmentQueue.isEmpty())
            {
                propagateEOM(outBuffer);
                outBuffer.setSequenceNumber(sequenceNumber++);
                return 0;
            }
        }
        if(inBuffer.isDiscard())
        {
            updateOutput(outBuffer, outputFormat, 0, 0);
            outBuffer.setDiscard(true);
            return 4;
        }
        int retVal = 1;
        try
        {
            retVal = doProcess(inBuffer, outBuffer);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return 1;
        }
        if(outputFormat == null)
            outputFormat = makeMPEGFormat(inBuffer.getFormat());
        if(retVal != 4)
            outBuffer.setSequenceNumber(sequenceNumber++);
        return retVal;
    }

    public String getName()
    {
        return "MPEG Video Packetizer";
    }

    public void finalize()
    {
        close();
    }

    private int doProcess(Buffer inBuffer, Buffer outBuffer)
    {
        if(expectingNewInput)
        {
            if(!inputEOM)
            {
                if(inBuffer.getData() == null)
                    return 4;
                if(resetTime)
                {
                    startTime = inBuffer.getTimeStamp();
                    if(debug)
                        System.err.println("Packetizer(V): new synctime set: " + startTime);
                    if(startTime == 0L)
                        startTime = 1L;
                    resetTime = false;
                }
                inputQueue.addElement(copyInto(inBuffer));
            }
            expectingNewInput = false;
        }
        if(expectingNewOutput)
        {
            byte outData[] = (byte[])outBuffer.getData();
            if(outData == null || outData.length < PACKET_MAX)
            {
                outData = new byte[PACKET_MAX];
                outBuffer.setData(outData);
            }
            System.arraycopy(mpegHeader, 0, outData, 0, 4);
            outBuffer.setOffset(0);
            outBuffer.setLength(4);
            outBuffer.setFlags(0);
            outBuffer.setHeader(null);
            outBuffer.setFormat(outputFormat);
            expectingNewOutput = false;
        }
        if(segmentQueue.isEmpty())
        {
            findFirstStartCode();
            if(segmentQueue.isEmpty())
            {
                expectingNewInput = true;
                return 4;
            }
        }
        for(MPEGSegment mseg = (MPEGSegment)segmentQueue.firstElement(); mseg != null;)
        {
            if(mseg.getLength() < 0)
            {
                expectingNewInput = true;
                return 4;
            }
            int startCode = mseg.startCode;
            int res = 0;
            if(startCode == 179)
                res = doSequenceHeader(mseg, outBuffer);
            else
            if(startCode == 183)
                res = doSequenceEnd(mseg, outBuffer);
            else
            if(startCode == 184)
                res = doGOP(mseg, outBuffer);
            else
            if(startCode == 0)
                res = doPicture(mseg, outBuffer);
            else
            if(startCode >= 1 && startCode <= 175)
                res = doSlice(mseg, outBuffer);
            else
                res = SEGMENT_DONE;
            if(res == SEGMENT_DONE)
            {
                segmentQueue.removeElementAt(0);
                if(segmentQueue.isEmpty())
                {
                    expectingNewInput = true;
                    return outBuffer.getLength() <= 4 ? 4 : 0;
                }
                mseg = (MPEGSegment)segmentQueue.firstElement();
            } else
            {
                if(res == SEGMENT_DONE_BUFFER_FULL)
                {
                    segmentQueue.removeElementAt(0);
                    outBuffer.setFlags(outBuffer.getFlags() | 0x20);
                    return !expectingNewInput ? 2 : 0;
                }
                if(res == SEGMENT_REPEAT)
                {
                    outBuffer.setFlags(outBuffer.getFlags() | 0x20);
                    return !expectingNewInput ? 2 : 0;
                }
            }
        }

        return 1;
    }

    private Buffer copyInto(Buffer src)
    {
        Buffer dest = new Buffer();
        dest.copy(src);
        dest.setFlags(dest.getFlags() | 0x20);
        src.setData(null);
        src.setHeader(null);
        src.setLength(0);
        src.setOffset(0);
        return dest;
    }

    protected String toHex(byte inData[], int inOffset)
    {
        String hex = new String();
        for(int i = 0; i < 4; i++)
        {
            hex = hex + hexChar[inData[inOffset + i] >> 4 & 0xf];
            hex = hex + hexChar[inData[inOffset + i] & 0xf];
        }

        return hex;
    }

    private int doSequenceHeader(MPEGSegment sh, Buffer outBuffer)
    {
        sequenceHeader = new byte[sh.getLength()];
        sh.copyData(sequenceHeader, 0);
        frameWidth = (sequenceHeader[4] & 0xff) << 4 | (sequenceHeader[5] & 0xf0) >> 4;
        frameHeight = (sequenceHeader[5] & 0xf) << 8 | sequenceHeader[6] & 0xff;
        int frix = sequenceHeader[7] & 0xf;
        if(frix > 0 && frix <= 8)
            frameRate = RATE_TABLE[frix];
        picNanos = (long)(1000000000D / frameRate);
        return SEGMENT_DONE;
    }

    private int copySequenceHeader(Buffer outBuffer)
    {
        if(sequenceHeader == null)
        {
            return 0;
        } else
        {
            System.arraycopy(sequenceHeader, 0, outBuffer.getData(), outBuffer.getLength(), sequenceHeader.length);
            outBuffer.setLength(outBuffer.getLength() + sequenceHeader.length);
            return sequenceHeader.length;
        }
    }

    private int doSequenceEnd(MPEGSegment se, Buffer outBuffer)
    {
        return SEGMENT_DONE;
    }

    private int doGOP(MPEGSegment gop, Buffer outBuffer)
    {
        if(frameCount == 0L)
            gopTime = 1L + startTime;
        else
            gopTime = frameCount * picNanos + startTime;
        copySequenceHeader(outBuffer);
        gop.copyData((byte[])outBuffer.getData(), outBuffer.getLength());
        outBuffer.setLength(outBuffer.getLength() + gop.getLength());
        return SEGMENT_DONE;
    }

    private int doPicture(MPEGSegment ph, Buffer outBuffer)
    {
        byte pic[] = new byte[ph.getLength()];
        ph.copyData(pic, 0);
        int cnt = (pic[4] & 0xff) << 2 | (pic[5] & 0xc0) >> 6;
        int type = (pic[5] & 0x38) >> 3;
        mpegHeader[0] = (byte)(cnt >> 8 & 2);
        mpegHeader[1] = (byte)cnt;
        mpegHeader[2] = (byte)type;
        if(type == 1)
        {
            mpegHeader[3] = 0;
        } else
        {
            int next = (pic[7] & 7) << 1 | (pic[8] & 0x80) >> 7;
            if(type > 2)
                next |= (pic[8] & 0x78) << 1;
            mpegHeader[3] = (byte)next;
        }
        resetInProgress = false;
        byte outData[] = (byte[])outBuffer.getData();
        System.arraycopy(mpegHeader, 0, outData, 0, 4);
        if(outBuffer.getLength() > 8 && outData[4] == 0 && outData[5] == 0 && outData[6] == 1 && (outData[7] & 0xff) == 179)
            outData[2] |= 0x20;
        ph.copyData((byte[])outBuffer.getData(), outBuffer.getLength());
        outBuffer.setLength(outBuffer.getLength() + ph.getLength());
        outBuffer.setFlags(outBuffer.getFlags() | 0x10);
        frameCount++;
        frameTime = gopTime + (long)cnt * picNanos;
        outBuffer.setTimeStamp(frameTime);
        outBuffer.setFormat(outputFormat);
        return SEGMENT_DONE;
    }

    private int doSlice(MPEGSegment slice, Buffer outBuffer)
    {
        byte outData[] = (byte[])outBuffer.getData();
        if(slice.getLength() < PACKET_MAX - outBuffer.getLength())
        {
            slice.copyData(outData, outBuffer.getLength());
            outBuffer.setLength(outBuffer.getLength() + slice.getLength());
            outBuffer.setTimeStamp(frameTime);
            outBuffer.setFormat(outputFormat);
            outData[2] |= 0x18;
            if(segmentQueue.size() > 1)
            {
                MPEGSegment mse = (MPEGSegment)segmentQueue.elementAt(1);
                if(mse.startCode < 1 || mse.startCode > 175)
                {
                    outBuffer.setFlags(outBuffer.getFlags() | 0x800);
                    expectingNewOutput = true;
                    return SEGMENT_DONE_BUFFER_FULL;
                }
            } else
            if(inputEOM)
            {
                outBuffer.setFlags(outBuffer.getFlags() | 0x800);
                expectingNewOutput = true;
                return SEGMENT_DONE_BUFFER_FULL;
            }
            return SEGMENT_DONE;
        }
        if((outData[2] & 0x18) != 0)
        {
            expectingNewOutput = true;
            return SEGMENT_REPEAT;
        }
        int len = PACKET_MAX - outBuffer.getLength();
        slice.copyData(0, len, outData, outBuffer.getLength());
        outBuffer.setLength(outBuffer.getLength() + len);
        outBuffer.setTimeStamp(frameTime);
        outBuffer.setFormat(outputFormat);
        outData[2] |= 0x10;
        int off = len;
        len = slice.getLength() - len;
        Buffer b = null;
        while(len > 0) 
        {
            b = new Buffer();
            outData = new byte[PACKET_MAX];
            b.setData(outData);
            b.setTimeStamp(frameTime);
            b.setHeader(null);
            b.setFormat(outputFormat);
            b.setFlags(outBuffer.getFlags());
            b.setOffset(0);
            System.arraycopy(mpegHeader, 0, outData, 0, 4);
            int l = len;
            if(len > PACKET_MAX - 4)
                l = PACKET_MAX - 4;
            slice.copyData(off, l, (byte[])b.getData(), 4);
            b.setLength(l + 4);
            off += l;
            len -= l;
            if(len <= 0)
                outData[2] |= 8;
            outputQueue.addElement(b);
        }
        if(segmentQueue.size() > 1)
        {
            MPEGSegment mse = (MPEGSegment)segmentQueue.elementAt(1);
            if(mse.startCode < 1 || mse.startCode > 175)
            {
                b.setFlags(b.getFlags() | 0x800);
                expectingNewOutput = true;
                return SEGMENT_DONE_BUFFER_FULL;
            }
        } else
        if(inputEOM)
        {
            b.setFlags(b.getFlags() | 0x800);
            expectingNewOutput = true;
            return SEGMENT_DONE_BUFFER_FULL;
        }
        expectingNewOutput = true;
        return SEGMENT_DONE_BUFFER_FULL;
    }

    private void findFirstStartCode()
    {
        if(inputQueue.isEmpty())
            return;
        Buffer inBuffer = (Buffer)inputQueue.firstElement();
        inputQueue.removeElementAt(0);
        byte inData[] = (byte[])inBuffer.getData();
        int off = inBuffer.getOffset();
        for(int len = inBuffer.getLength(); len > 4; len--)
        {
            if(inData[off] == 0 && inData[off + 1] == 0 && inData[off + 2] == 1 && (inData[off + 3] & 0xff) != 181 && (inData[off + 3] & 0xff) != 178)
                if(resetInProgress)
                {
                    if((inData[off + 3] & 0xff) == 179 || (inData[off + 3] & 0xff) == 184)
                    {
                        MPEGSegment ns = new MPEGSegment(inData[off + 3] & 0xff, off, inBuffer);
                        segmentQueue.addElement(ns);
                        return;
                    }
                } else
                {
                    MPEGSegment ns = new MPEGSegment(inData[off + 3] & 0xff, off, inBuffer);
                    segmentQueue.addElement(ns);
                    return;
                }
            off++;
        }

        expectingNewInput = true;
    }

    public static float RATE_TABLE[] = {
        0.0F, 23.976F, 24F, 25F, 29.97F, 30F, 50F, 59.94F, 60F
    };
    private static char hexChar[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
        'a', 'b', 'c', 'd', 'e', 'f'
    };
    protected static int SEGMENT_DONE = 1;
    protected static int SEGMENT_REPEAT = 2;
    protected static int SEGMENT_DONE_BUFFER_FULL = 3;
    protected static int PACKET_MAX = 1456;
    private static boolean debug = false;
    private VideoFormat inputFormat;
    private VideoFormat outputFormat;
    private boolean inputEOM;
    private boolean expectingNewInput;
    private boolean expectingNewOutput;
    private boolean resetTime;
    private boolean resetInProgress;
    private Vector outputQueue;
    private Vector inputQueue;
    private Vector segmentQueue;
    private byte sequenceHeader[];
    private int frameWidth;
    private int frameHeight;
    private double frameRate;
    private long picNanos;
    private long gopTime;
    private long startTime;
    private long frameTime;
    private long frameCount;
    private int sequenceNumber;
    private byte mpegHeader[] = {
        0, 0, 0, 0
    };




}
