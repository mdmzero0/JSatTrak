// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPDePacketizer.java

package com.sun.media.codec.video.mpeg;

import java.awt.Dimension;
import java.io.*;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;

public class RTPDePacketizer
{
    class MPEGFrame
    {

        public void add(Buffer buffer)
        {
            if(buffer == null || buffer.getData() == null || buffer.getLength() < 4)
                return;
            if(compareSequenceNumbers(seqno, buffer.getSequenceNumber()) > 0)
            {
                data.addElement(buffer);
                seqno = buffer.getSequenceNumber();
            } else
            {
                long sq = buffer.getSequenceNumber();
                for(int i = 0; i < data.size(); i++)
                {
                    long bsq = ((Buffer)data.elementAt(i)).getSequenceNumber();
                    if(compareSequenceNumbers(bsq, sq) < 0)
                    {
                        data.insertElementAt(buffer, i);
                        break;
                    }
                    if(sq == bsq)
                        return;
                }

            }
            datalength += buffer.getLength() - 4;
        }

        public Vector getData()
        {
            return data;
        }

        public Buffer getFirst()
        {
            if(data.size() > 0)
                return (Buffer)data.firstElement();
            else
                return null;
        }

        public int getLength()
        {
            return datalength;
        }

        public long rtptimestamp;
        public long seqno;
        private int datalength;
        private Vector data;



        public MPEGFrame(Buffer buffer)
        {
            rtptimestamp = -1L;
            seqno = -1L;
            datalength = 0;
            data = new Vector();
            rtptimestamp = buffer.getTimeStamp();
        }
    }


    RTPDePacketizer()
    {
        discardtimestamp = -1L;
        currentframe = null;
        newframe = true;
        gotSequenceHeader = false;
        sequenceSent = false;
        sequenceHeader = null;
        gopset = false;
        closedGop = 0;
        ref_pic_temp = -1;
        dep_pic_temp = -1;
        sequenceNumber = 0;
        width = 352;
        height = 240;
        frameRate = -1F;
        outFormat = null;
        allowHeadless = false;
        fullFrameOnly = true;
        droppedPFrame = false;
        droppedIFrame = false;
        capture = false;
        captureFile = null;
        if(capture)
            try
            {
                captureFile = new BufferedOutputStream(new FileOutputStream("/tmp/rtpstream.mpg"));
            }
            catch(IOException ioe)
            {
                System.err.println("RTPDePacketizer: unable to open file " + ioe);
                capture = false;
            }
    }

    public void finalize()
    {
        if(capture)
            try
            {
                captureFile.flush();
                captureFile.close();
                System.err.println("RTPDePacketizer: closed file");
            }
            catch(IOException ioe)
            {
                System.err.println("RTPDePacketizer: unable to close file " + ioe);
                capture = false;
            }
    }

    public int process(Buffer inBuffer, Buffer outBuffer)
    {
        if(inBuffer.getTimeStamp() == discardtimestamp && discardtimestamp != -1L)
            return 4;
        if(!newframe && currentframe != null && inBuffer.getTimeStamp() != currentframe.rtptimestamp)
        {
            if(allowHeadless || firstPacket(inBuffer))
            {
                boolean haveframe = false;
                if(fullFrameOnly)
                    dropFrame();
                else
                    haveframe = constructFrame(outBuffer);
                currentframe = createNewFrame(inBuffer);
                if(haveframe)
                    return 0;
                return (currentframe.getFirst().getFlags() & 0x800) == 0 || !constructFrame(outBuffer) ? 4 : 0;
            }
            discardtimestamp = inBuffer.getTimeStamp();
            if(fullFrameOnly)
            {
                dropFrame();
                dropBufferFrame(inBuffer);
            } else
            if(compareSequenceNumbers(currentframe.seqno, inBuffer.getSequenceNumber()) > 0 && constructFrame(outBuffer))
                return 0;
            return 4;
        }
        if(newframe)
        {
            if(firstPacket(inBuffer))
            {
                newframe = false;
                currentframe = createNewFrame(inBuffer);
                return (currentframe.getFirst().getFlags() & 0x800) == 0 || !constructFrame(outBuffer) ? 4 : 0;
            }
            if(fullFrameOnly)
                dropBufferFrame(inBuffer);
            discardtimestamp = inBuffer.getTimeStamp();
            newframe = true;
            return 4;
        } else
        {
            int ret = addToFrame(inBuffer, outBuffer);
            return ret;
        }
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

    private int addToFrame(Buffer inBuffer, Buffer outBuffer)
    {
        Buffer b = copyInto(inBuffer);
        currentframe.add(b);
        return (b.getFlags() & 0x800) == 0 || !constructFrame(outBuffer) ? 4 : 0;
    }

    private void constructGop(Buffer outBuffer)
    {
        byte dest[] = (byte[])outBuffer.getData();
        int outoffset = outBuffer.getLength();
        if(sequenceHeader != null)
        {
            System.arraycopy(sequenceHeader, 0, dest, outoffset, sequenceHeader.length);
            outBuffer.setLength(outBuffer.getLength() + sequenceHeader.length);
            outoffset += sequenceHeader.length;
            sequenceSent = true;
        }
        dest[outoffset] = 0;
        dest[outoffset + 1] = 0;
        dest[outoffset + 2] = 1;
        dest[outoffset + 3] = -72;
        dest[outoffset + 4] = -128;
        dest[outoffset + 5] = 8;
        dest[outoffset + 6] = 0;
        dest[outoffset + 7] = (byte)(closedGop | 0x20);
        outBuffer.setLength(outBuffer.getLength() + 8);
        ref_pic_temp = 0;
        dep_pic_temp = -1;
    }

    private void constructPicture(Buffer inBuffer, Buffer outBuffer)
    {
        byte payload[] = (byte[])inBuffer.getData();
        int offset = inBuffer.getOffset();
        byte dest[] = (byte[])outBuffer.getData();
        int outoffset = outBuffer.getLength();
        int next = 0;
        dest[outoffset] = 0;
        dest[outoffset + 1] = 0;
        dest[outoffset + 2] = 1;
        dest[outoffset + 3] = 0;
        dest[outoffset + 4] = (byte)((payload[offset] & 3) << 6 | (payload[offset + 1] & 0xfc) >> 2);
        int ptype = payload[offset + 2] & 7;
        int back = (payload[offset + 3] & 0xf0) >> 4;
        int fwd = payload[offset + 3] & 0xf;
        dest[outoffset + 5] = (byte)((payload[offset + 1] & 2) << 6 | ptype << 3);
        dest[outoffset + 6] = 0;
        if(ptype == 1)
        {
            dest[outoffset + 7] = 0;
            outBuffer.setLength(outBuffer.getLength() + 8);
        } else
        {
            next = fwd >> 1;
            dest[outoffset + 7] = (byte)next;
            next = (fwd & 1) << 7;
            if(ptype > 2)
                next |= back << 3;
            dest[outoffset + 8] = (byte)next;
            outBuffer.setLength(outBuffer.getLength() + 9);
        }
    }

    private void constructHeaders(Buffer outBuffer)
    {
        boolean havePicture = false;
        int outoffset = 0;
        byte dest[] = (byte[])outBuffer.getData();
        Buffer src = (Buffer)currentframe.data.elementAt(0);
        byte payload[] = (byte[])src.getData();
        int offset = src.getOffset();
        int tr = (payload[offset] & 3) << 8 | payload[offset + 1] & 0xff;
        int type = payload[offset + 2] & 7;
        if(src.getLength() >= 8 && (payload[offset + 2] & 0x10) == 16 && payload[offset + 4] == 0 && payload[offset + 5] == 0 && payload[offset + 6] == 1)
        {
            int startCode = payload[offset + 7] & 0xff;
            if(startCode == 179)
            {
                sequenceSent = true;
                ref_pic_temp = tr;
                dep_pic_temp = -1;
                return;
            }
            if(startCode == 184)
            {
                if(sequenceHeader != null)
                {
                    System.arraycopy(sequenceHeader, 0, dest, outBuffer.getLength(), sequenceHeader.length);
                    outBuffer.setLength(outBuffer.getLength() + sequenceHeader.length);
                    sequenceSent = true;
                }
                ref_pic_temp = tr;
                dep_pic_temp = -1;
                return;
            }
            if(startCode == 0)
                havePicture = true;
        }
        ref_pic_temp++;
        dep_pic_temp++;
        if(type < 3)
        {
            if(tr < ref_pic_temp)
                constructGop(outBuffer);
            ref_pic_temp = tr;
        } else
        {
            if(tr < dep_pic_temp)
                constructGop(outBuffer);
            dep_pic_temp = tr;
        }
        if(!havePicture)
            constructPicture(src, outBuffer);
    }

    private void dropFrame()
    {
        Buffer src = (Buffer)currentframe.data.firstElement();
        dropBufferFrame(src);
    }

    private void dropBufferFrame(Buffer src)
    {
        int type = ((byte[])src.getData())[src.getOffset() + 2] & 7;
        if(type == 1)
            droppedIFrame = true;
        else
        if(type == 2)
            droppedPFrame = true;
        newframe = true;
        currentframe = null;
    }

    private boolean constructFrame(Buffer outBuffer)
    {
        Buffer src = (Buffer)currentframe.data.lastElement();
        int type = ((byte[])src.getData())[src.getOffset() + 2] & 7;
        if(fullFrameOnly)
        {
            if(type >= 2 && (droppedIFrame || droppedPFrame))
            {
                dropFrame();
                return false;
            }
            if(type == 1)
            {
                droppedIFrame = false;
                droppedPFrame = false;
            }
            if((src.getFlags() & 0x800) == 0)
            {
                dropFrame();
                return false;
            }
            for(int i = currentframe.data.size() - 2; i >= 0; i--)
            {
                Buffer prev = (Buffer)currentframe.data.elementAt(i);
                if(compareSequenceNumbers(prev.getSequenceNumber(), src.getSequenceNumber()) != 1)
                {
                    dropFrame();
                    return false;
                }
                src = prev;
            }

        }
        boolean noslices = true;
        byte dest[] = (byte[])outBuffer.getData();
        if(dest == null || dest.length < currentframe.datalength + sequenceHeader.length + 16)
            dest = new byte[currentframe.datalength + sequenceHeader.length + 16];
        outBuffer.setData(dest);
        outBuffer.setOffset(0);
        outBuffer.setLength(0);
        constructHeaders(outBuffer);
        if(!sequenceSent)
        {
            dropFrame();
            return false;
        }
        int outoffset = outBuffer.getLength();
label0:
        for(int i = 0; i < currentframe.data.size(); i++)
        {
            src = (Buffer)currentframe.data.elementAt(i);
            byte payload[] = (byte[])src.getData();
            int offset = src.getOffset();
            if((payload[offset + 2] & 0x10) != 16)
                continue;
            if((payload[offset + 2] & 8) == 8)
            {
                System.arraycopy(payload, offset + 4, dest, outoffset, src.getLength() - 4);
                outoffset += src.getLength() - 4;
                noslices = false;
                continue;
            }
            long seq = src.getSequenceNumber();
            int j;
            for(j = i + 1; j < currentframe.data.size(); j++)
            {
                Buffer next = (Buffer)currentframe.data.elementAt(j);
                if(compareSequenceNumbers(seq, next.getSequenceNumber()) != 1)
                {
                    if(i == 0)
                    {
                        offset += 4;
                        int len = src.getLength() - 4;
                        int off = offset;
                        for(; len > 4; len--)
                        {
                            if(payload[off + 0] == 0 && payload[off + 1] == 0 && payload[off + 2] == 1 && (payload[off + 3] & 0xff) > 0 && (payload[off + 3] & 0xff) <= 175)
                                break;
                            off++;
                        }

                        if(off != offset)
                        {
                            System.arraycopy(payload, offset, dest, outoffset, off - offset);
                            outoffset += off - offset;
                        }
                    }
                    continue label0;
                }
                seq = next.getSequenceNumber();
                if((((byte[])next.getData())[next.getOffset() + 2] & 8) == 8)
                    break;
            }

            if(j == currentframe.data.size())
                break;
            for(int k = i; k <= j; k++)
            {
                src = (Buffer)currentframe.data.elementAt(k);
                System.arraycopy((byte[])src.getData(), src.getOffset() + 4, dest, outoffset, src.getLength() - 4);
                outoffset += src.getLength() - 4;
            }

            noslices = false;
            i = j;
        }

        if(outFormat == null || outFormat.getSize().width != width || outFormat.getSize().height != height || outFormat.getFrameRate() != frameRate)
        {
            Dimension d = new Dimension(width, height);
            outFormat = new VideoFormat("mpeg", d, -1, Format.byteArray, frameRate);
        }
        outBuffer.setLength(outoffset);
        outBuffer.setFormat(outFormat);
        if(noslices)
            outBuffer.setFlags(2);
        outBuffer.setTimeStamp(currentframe.rtptimestamp);
        outBuffer.setSequenceNumber(sequenceNumber++);
        newframe = true;
        currentframe = null;
        if(noslices)
            return false;
        if(capture)
            try
            {
                captureFile.write((byte[])outBuffer.getData(), outBuffer.getOffset(), outBuffer.getLength());
            }
            catch(IOException ioe)
            {
                System.err.println("RTPDePacketizer: write error for sequence number " + outBuffer.getSequenceNumber() + " : " + ioe);
                capture = false;
            }
        return true;
    }

    private boolean firstPacket(Buffer inBuffer)
    {
        if(inBuffer == null)
            return false;
        byte payload[] = (byte[])inBuffer.getData();
        if(payload == null)
            return false;
        int offset = inBuffer.getOffset();
        int len = inBuffer.getLength();
        if(len < 12)
            return false;
        if(!gotSequenceHeader)
            if((payload[offset + 2] & 0x20) == 32 && payload[offset + 4] == 0 && payload[offset + 5] == 0 && payload[offset + 6] == 1 && (payload[offset + 7] & 0xff) == 179)
            {
                width = (payload[offset + 8] & 0xff) << 4 | (payload[offset + 9] & 0xf0) >> 4;
                height = (payload[offset + 9] & 0xf) << 8 | payload[offset + 10] & 0xff;
                gotSequenceHeader = true;
                offset += 4;
                len -= 4;
                int off = offset;
                for(; len > 8; len--)
                {
                    if(payload[off + 0] == 0 && payload[off + 1] == 0 && payload[off + 2] == 1 && (payload[off + 3] & 0xff) == 184)
                    {
                        gopset = true;
                        closedGop = payload[off + 7] & 0x40;
                        payload[off + 7] = (byte)(payload[off + 7] & 0x20);
                        sequenceHeader = new byte[off - offset];
                        System.arraycopy(payload, offset, sequenceHeader, 0, sequenceHeader.length);
                        return true;
                    }
                    off++;
                }

                return true;
            } else
            {
                return false;
            }
        if((payload[offset + 2] & 0x10) != 16)
            return false;
        offset += 4;
        for(len -= 4; len > 8; len--)
        {
            if(payload[offset + 0] == 0 && payload[offset + 1] == 0 && payload[offset + 2] == 1)
            {
                if(payload[offset + 3] == 0)
                    return true;
                if((payload[offset + 3] & 0xff) == 184)
                {
                    gopset = true;
                    closedGop = payload[offset + 7] & 0x40;
                    payload[offset + 7] = (byte)(payload[offset + 7] | 0x20);
                    return true;
                }
                if((payload[offset + 3] & 0xff) <= 175)
                    return false;
            }
            offset++;
        }

        return false;
    }

    private int compareSequenceNumbers(long p, long c)
    {
        if(c > p)
            return (int)(c - p);
        if(c == p)
            return 0;
        if(p > (long)(MAX_SEQ - 100) && c < 100L)
            return (int)(((long)MAX_SEQ - p) + c + 1L);
        else
            return -1;
    }

    private MPEGFrame createNewFrame(Buffer inBuffer)
    {
        Buffer b = copyInto(inBuffer);
        MPEGFrame newframe = new MPEGFrame(b);
        newframe.add(b);
        return newframe;
    }

    private Buffer copyInto(Buffer src)
    {
        Buffer dest = new Buffer();
        dest.copy(src);
        src.setData(null);
        src.setHeader(null);
        src.setLength(0);
        src.setOffset(0);
        return dest;
    }

    public static float RATE_TABLE[] = {
        0.0F, 23.976F, 24F, 25F, 29.97F, 30F, 50F, 59.94F, 60F
    };
    private static char hexChar[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
        'a', 'b', 'c', 'd', 'e', 'f'
    };
    private long discardtimestamp;
    private MPEGFrame currentframe;
    private boolean newframe;
    private boolean gotSequenceHeader;
    private boolean sequenceSent;
    private byte sequenceHeader[];
    private boolean gopset;
    private int closedGop;
    private int ref_pic_temp;
    private int dep_pic_temp;
    private int sequenceNumber;
    private int width;
    private int height;
    private float frameRate;
    private VideoFormat outFormat;
    private boolean allowHeadless;
    private boolean fullFrameOnly;
    private boolean droppedPFrame;
    private boolean droppedIFrame;
    private boolean capture;
    private OutputStream captureFile;
    private static final boolean debug = false;
    private static int MAX_SEQ = 65535;


}
