// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPDePacketizer.java

package com.sun.media.codec.video.jpeg;

import java.awt.Dimension;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.codec.video.jpeg:
//            RTPDePacketizer

class JPEGFrame
{

    public JPEGFrame(RTPDePacketizer depacketizer, Buffer buffer, byte frameBuffer[])
    {
        rtptimestamp = -1L;
        dataLength = 0;
        hdrOffset = 0;
        numPkts = 0L;
        lquantOffset = 2 + APP0.length + 2 + 2 + 1;
        cquantOffset = lquantOffset + 64 + 2 + 2 + 1;
        this.depacketizer = depacketizer;
        firstSeq = buffer.getSequenceNumber();
        if(depacketizer.frameBuffer == null)
            if(frameBuffer != null)
                depacketizer.frameBuffer = frameBuffer;
            else
                depacketizer.frameBuffer = new byte[32000];
        rtptimestamp = buffer.getTimeStamp();
        int extraskip = 0;
        if(!hasJFIFHeader(buffer))
            extraskip = generateJFIFHeader(buffer);
        add(buffer, extraskip);
    }

    public void add(Buffer buffer, int extraskip)
    {
        int chunkSize = buffer.getLength() - 8 - extraskip;
        int foff = depacketizer.getFragOffset((byte[])buffer.getData(), buffer.getOffset());
        foff += hdrOffset;
        if(depacketizer.frameBuffer.length >= foff + chunkSize + 2)
        {
            System.arraycopy((byte[])buffer.getData(), buffer.getOffset() + 8 + extraskip, depacketizer.frameBuffer, foff, chunkSize);
            dataLength += chunkSize;
            numPkts++;
        } else
        {
            increaseFrameBuffer(foff + chunkSize + 2);
            add(buffer, extraskip);
        }
    }

    public boolean gotAllPackets(long lastSeq)
    {
        return (lastSeq - firstSeq) + 1L == numPkts;
    }

    public void completeTransfer(Buffer inBuffer, Buffer outBuffer)
    {
        int offset = inBuffer.getOffset();
        byte inBuff[] = (byte[])inBuffer.getData();
        int height = inBuff[offset + 7] & 0xff;
        int width = inBuff[offset + 6] & 0xff;
        depacketizer.quality = inBuff[offset + 5] & 0xff;
        depacketizer.type = inBuff[offset + 4] & 0xff;
        Dimension d = new Dimension(width * 8, height * 8);
        inBuffer.setFormat(new VideoFormat("jpeg", d, 0, inBuffer.getFormat().getDataType(), -1F));
        if(depacketizer.frameBuffer[dataLength - 2] != -1 || depacketizer.frameBuffer[dataLength - 1] != -39)
        {
            depacketizer.frameBuffer[dataLength++] = -1;
            depacketizer.frameBuffer[dataLength++] = -39;
        }
        outBuffer.setData(depacketizer.frameBuffer);
        outBuffer.setSequenceNumber(depacketizer.sequenceNumber++);
        outBuffer.setLength(dataLength);
        depacketizer.frameBuffer = null;
    }

    private void increaseFrameBuffer(int amount)
    {
        byte newFrameBuffer[] = new byte[amount];
        System.arraycopy(depacketizer.frameBuffer, 0, newFrameBuffer, 0, depacketizer.frameBuffer.length);
        depacketizer.frameBuffer = newFrameBuffer;
    }

    private boolean hasJFIFHeader(Buffer buffer)
    {
        byte data[] = (byte[])buffer.getData();
        int offset = buffer.getOffset();
        return (data[offset + 8] & 0xff) == 255 && (data[offset + 9] & 0xff) == 216;
    }

    private int generateJFIFHeader(Buffer buffer)
    {
        int extraskip = 0;
        byte data[] = (byte[])buffer.getData();
        int offset = buffer.getOffset();
        int type = data[offset + 4] & 0xff;
        int quality = data[offset + 5] & 0xff;
        int width = data[offset + 6] & 0xff;
        int height = data[offset + 7] & 0xff;
        if(quality == depacketizer.lastQuality && width == depacketizer.lastWidth && height == depacketizer.lastHeight && type == depacketizer.lastType)
        {
            System.arraycopy(depacketizer.lastJFIFHeader, 0, depacketizer.frameBuffer, 0, depacketizer.lastJFIFHeader.length);
            hdrOffset = depacketizer.lastJFIFHeader.length;
        } else
        {
            hdrOffset = makeHeaders(depacketizer.frameBuffer, 0, type, quality, width, height);
            depacketizer.lastJFIFHeader = new byte[hdrOffset];
            System.arraycopy(depacketizer.frameBuffer, 0, depacketizer.lastJFIFHeader, 0, hdrOffset);
            depacketizer.lastQuality = quality;
            depacketizer.lastType = type;
            depacketizer.lastWidth = width;
            depacketizer.lastHeight = height;
        }
        if(quality >= 100)
        {
            extraskip = 132;
            System.arraycopy(data, offset + 8 + 4, depacketizer.frameBuffer, lquantOffset, 64);
            System.arraycopy(data, offset + 8 + 4 + 64, depacketizer.frameBuffer, cquantOffset, 64);
        }
        dataLength += depacketizer.lastJFIFHeader.length;
        return extraskip;
    }

    private int makeHeaders(byte p[], int offset, int type, int q, int w, int h)
    {
        int lqt[] = new int[64];
        int cqt[] = new int[64];
        w *= 8;
        h *= 8;
        makeQTables(q, lqt, cqt);
        p[offset++] = -1;
        p[offset++] = -40;
        for(int app = 0; app < APP0.length; app++)
            p[offset++] = APP0[app];

        offset = makeQuantHeader(p, offset, lqt, 0);
        offset = makeQuantHeader(p, offset, cqt, 1);
        offset = makeHuffmanHeader(p, offset, lum_dc_codelens, lum_dc_codelens.length, lum_dc_symbols, lum_dc_symbols.length, 0, 0);
        offset = makeHuffmanHeader(p, offset, lum_ac_codelens, lum_ac_codelens.length, lum_ac_symbols, lum_ac_symbols.length, 0, 1);
        offset = makeHuffmanHeader(p, offset, chm_dc_codelens, chm_dc_codelens.length, chm_dc_symbols, chm_dc_symbols.length, 1, 0);
        offset = makeHuffmanHeader(p, offset, chm_ac_codelens, chm_ac_codelens.length, chm_ac_symbols, chm_ac_symbols.length, 1, 1);
        p[offset++] = -1;
        p[offset++] = -64;
        p[offset++] = 0;
        p[offset++] = 17;
        p[offset++] = 8;
        p[offset++] = (byte)(h >> 8 & 0xff);
        p[offset++] = (byte)(h & 0xff);
        p[offset++] = (byte)(w >> 8 & 0xff);
        p[offset++] = (byte)(w & 0xff);
        p[offset++] = 3;
        p[offset++] = 0;
        if(type == 2)
            p[offset++] = 17;
        else
        if(type == 1)
            p[offset++] = 34;
        else
            p[offset++] = 33;
        p[offset++] = 0;
        p[offset++] = 1;
        p[offset++] = 17;
        p[offset++] = 1;
        p[offset++] = 2;
        p[offset++] = 17;
        p[offset++] = 1;
        p[offset++] = -1;
        p[offset++] = -38;
        p[offset++] = 0;
        p[offset++] = 12;
        p[offset++] = 3;
        p[offset++] = 0;
        p[offset++] = 0;
        p[offset++] = 1;
        p[offset++] = 17;
        p[offset++] = 2;
        p[offset++] = 17;
        p[offset++] = 0;
        p[offset++] = 63;
        p[offset++] = 0;
        return offset;
    }

    private int makeQuantHeader(byte p[], int offset, int qt[], int tableNo)
    {
        p[offset++] = -1;
        p[offset++] = -37;
        p[offset++] = 0;
        p[offset++] = 67;
        p[offset++] = (byte)tableNo;
        for(int i = 0; i < 64; i++)
            p[offset++] = (byte)qt[i];

        return offset;
    }

    private int makeHuffmanHeader(byte p[], int offset, int codelens[], int ncodes, int symbols[], int nsymbols, int tableNo, 
            int tableClass)
    {
        p[offset++] = -1;
        p[offset++] = -60;
        p[offset++] = 0;
        p[offset++] = (byte)(3 + ncodes + nsymbols);
        p[offset++] = (byte)(tableClass << 4 | tableNo);
        for(int i = 0; i < ncodes; i++)
            p[offset++] = (byte)codelens[i];

        for(int i = 0; i < nsymbols; i++)
            p[offset++] = (byte)symbols[i];

        return offset;
    }

    private void makeQTables(int q, int lum_q[], int chr_q[])
    {
        int factor = q;
        if(q < 1)
            factor = 1;
        if(q > 99)
            factor = 99;
        if(q < 50)
            q = 5000 / factor;
        else
            q = 200 - factor * 2;
        for(int i = 0; i < 64; i++)
        {
            int lq = (jpeg_luma_quantizer[ZigZag[i]] * q + 50) / 100;
            int cq = (jpeg_chroma_quantizer[ZigZag[i]] * q + 50) / 100;
            if(lq < 1)
                lq = 1;
            else
            if(lq > 255)
                lq = 255;
            lum_q[i] = lq;
            if(cq < 1)
                cq = 1;
            else
            if(cq > 255)
                cq = 255;
            chr_q[i] = cq;
        }

    }

    public long rtptimestamp;
    public int dataLength;
    private RTPDePacketizer depacketizer;
    private int hdrOffset;
    private long firstSeq;
    private long numPkts;
    final int FRAME_BUFFER_INITIAL_SIZE = 32000;
    int lquantOffset;
    int cquantOffset;
    static final byte APP0[] = {
        -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 
        1, 0, 0, 1, 0, 1, 0, 0
    };
    static int lum_dc_codelens[] = {
        0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 
        0, 0, 0, 0, 0, 0
    };
    static int lum_dc_symbols[] = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
        10, 11
    };
    static int lum_ac_codelens[] = {
        0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 
        4, 4, 0, 0, 1, 125
    };
    static int lum_ac_symbols[] = {
        1, 2, 3, 0, 4, 17, 5, 18, 33, 49, 
        65, 6, 19, 81, 97, 7, 34, 113, 20, 50, 
        129, 145, 161, 8, 35, 66, 177, 193, 21, 82, 
        209, 240, 36, 51, 98, 114, 130, 9, 10, 22, 
        23, 24, 25, 26, 37, 38, 39, 40, 41, 42, 
        52, 53, 54, 55, 56, 57, 58, 67, 68, 69, 
        70, 71, 72, 73, 74, 83, 84, 85, 86, 87, 
        88, 89, 90, 99, 100, 101, 102, 103, 104, 105, 
        106, 115, 116, 117, 118, 119, 120, 121, 122, 131, 
        132, 133, 134, 135, 136, 137, 138, 146, 147, 148, 
        149, 150, 151, 152, 153, 154, 162, 163, 164, 165, 
        166, 167, 168, 169, 170, 178, 179, 180, 181, 182, 
        183, 184, 185, 186, 194, 195, 196, 197, 198, 199, 
        200, 201, 202, 210, 211, 212, 213, 214, 215, 216, 
        217, 218, 225, 226, 227, 228, 229, 230, 231, 232, 
        233, 234, 241, 242, 243, 244, 245, 246, 247, 248, 
        249, 250
    };
    static int chm_dc_codelens[] = {
        0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 
        1, 0, 0, 0, 0, 0
    };
    static int chm_dc_symbols[] = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
        10, 11
    };
    static int chm_ac_codelens[] = {
        0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 
        4, 4, 0, 1, 2, 119
    };
    static int chm_ac_symbols[] = {
        0, 1, 2, 3, 17, 4, 5, 33, 49, 6, 
        18, 65, 81, 7, 97, 113, 19, 34, 50, 129, 
        8, 20, 66, 145, 161, 177, 193, 9, 35, 51, 
        82, 240, 21, 98, 114, 209, 10, 22, 36, 52, 
        225, 37, 241, 23, 24, 25, 26, 38, 39, 40, 
        41, 42, 53, 54, 55, 56, 57, 58, 67, 68, 
        69, 70, 71, 72, 73, 74, 83, 84, 85, 86, 
        87, 88, 89, 90, 99, 100, 101, 102, 103, 104, 
        105, 106, 115, 116, 117, 118, 119, 120, 121, 122, 
        130, 131, 132, 133, 134, 135, 136, 137, 138, 146, 
        147, 148, 149, 150, 151, 152, 153, 154, 162, 163, 
        164, 165, 166, 167, 168, 169, 170, 178, 179, 180, 
        181, 182, 183, 184, 185, 186, 194, 195, 196, 197, 
        198, 199, 200, 201, 202, 210, 211, 212, 213, 214, 
        215, 216, 217, 218, 226, 227, 228, 229, 230, 231, 
        232, 233, 234, 242, 243, 244, 245, 246, 247, 248, 
        249, 250
    };
    static int ZigZag[] = {
        0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 
        32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 
        40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 
        21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 
        29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 
        52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 
        47, 55, 62, 63
    };
    static int jpeg_luma_quantizer[] = {
        16, 11, 10, 16, 24, 40, 51, 61, 12, 12, 
        14, 19, 26, 58, 60, 55, 14, 13, 16, 24, 
        40, 57, 69, 56, 14, 17, 22, 29, 51, 87, 
        80, 62, 18, 22, 37, 56, 68, 109, 103, 77, 
        24, 35, 55, 64, 81, 104, 113, 92, 49, 64, 
        78, 87, 103, 121, 120, 101, 72, 92, 95, 98, 
        112, 100, 103, 99
    };
    static int jpeg_chroma_quantizer[] = {
        17, 18, 24, 47, 99, 99, 99, 99, 18, 21, 
        26, 66, 99, 99, 99, 99, 24, 26, 56, 99, 
        99, 99, 99, 99, 47, 66, 99, 99, 99, 99, 
        99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 
        99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 
        99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 
        99, 99, 99, 99
    };

}
