// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MPAParse.java

package com.sun.media.codec.audio.mpa;

import java.io.PrintStream;

// Referenced classes of package com.sun.media.codec.audio.mpa:
//            MPAHeader

public class MPAParse
{

    public MPAParse()
    {
        firstFound = false;
        firstId = 0;
        firstLayer = 0;
        firstSamplingRate = 0;
    }

    public String getName()
    {
        return "MPEG Audio Parser";
    }

    public void reset()
    {
        firstFound = false;
        firstId = 0;
        firstLayer = 0;
        firstSamplingRate = 0;
    }

    public int getHeader(MPAHeader header, byte inData[], int inOffset, int inLength)
    {
        int status;
        boolean found;
        int offset;
        int off2;
        int bufend;
        status = MPA_ERR_NOHDR;
        found = false;
        offset = inOffset;
        off2 = 0;
        if(inLength < 4)
            return status;
        bufend = offset + inLength;
          goto _L1
_L6:
        if(++offset + 3 < bufend) goto _L1; else goto _L2
_L2:
        if(!found)
            return status;
          goto _L3
_L1:
        if((inData[offset] & 0xff) != 255 || (inData[offset + 1] & 0xf6) <= 240 || (inData[offset + 2] & 0xf0) == 240 || (inData[offset + 2] & 0xc) == 12 || (inData[offset + 3] & 3) == 2)
            continue; /* Loop/switch isn't completed */
        int id = inData[offset + 1] >> 3 & 1;
        int layer = inData[offset + 1] >> 1 & 3;
        int crc = inData[offset + 1] & 1;
        int bitrateIndex = inData[offset + 2] >> 4 & 0xf;
        int samplingIndex = inData[offset + 2] >> 2 & 3;
        int paddingBit = inData[offset + 2] >> 1 & 1;
        int channelMode = inData[offset + 3] >> 6 & 3;
        int nSamples = SAMPLES_PER_FRAME[id][layer];
        int samplingRate = SAMPLE_TABLE[id][samplingIndex];
        if(bitrateIndex != 0)
        {
            int bitsInFrame = ((id != MPA_MPEG1 ? BITRATE_TABLE2[layer][bitrateIndex] : BITRATE_TABLE1[layer][bitrateIndex]) * 1000 * nSamples) / samplingRate & ~SLOT_BITS_MASK[layer];
            if(paddingBit != 0)
                bitsInFrame += SLOT_BITS_MASK[layer] + 1;
            off2 = offset + (bitsInFrame >> 3);
            if(off2 + 1 < bufend)
            {
                if((inData[off2] & 0xff) == 255 && (inData[off2 + 1] & 0xfe) == (inData[offset + 1] & 0xfe))
                {
                    if(firstFound)
                    {
                        if(id == firstId && layer == firstLayer && samplingRate == firstSamplingRate)
                        {
                            header.headerOffset = offset;
                            found = true;
                            status = MPA_OK;
                        } else
                        {
                            offset++;
                            continue; /* Loop/switch isn't completed */
                        }
                    } else
                    {
                        header.headerOffset = offset;
                        found = true;
                        status = MPA_OK;
                    }
                } else
                if(offset == inOffset && firstFound && id == firstId && layer == firstLayer && samplingRate == firstSamplingRate)
                {
                    if(!found)
                        header.headerOffset = offset;
                    found = true;
                    status = MPA_HDR_DOUBTED;
                } else
                {
                    offset++;
                    continue; /* Loop/switch isn't completed */
                }
            } else
            {
                if(!found)
                    header.headerOffset = offset;
                found = true;
                status = MPA_HDR_DOUBTED;
                offset++;
                continue; /* Loop/switch isn't completed */
            }
        } else
        {
label0:
            {
                int maxLen = MAX_FREE_BITS[layer] >> 3;
                off2 = 48;
                if(offset + off2 + 3 >= bufend)
                {
                    if(!found)
                        header.headerOffset = offset;
                    found = true;
                    status = MPA_HDR_DOUBTED;
                    offset++;
                    continue; /* Loop/switch isn't completed */
                }
                try
                {
                    while((inData[offset + off2] & 0xff) != 255 || (inData[offset + off2 + 1] & 0xfe) != (inData[offset + 1] & 0xfe) || (inData[offset + off2 + 2] & 0xfc) != (inData[offset + 2] & 0xfc) || (inData[offset + off2 + 3] & 3) != 2) 
                    {
                        if(++off2 > maxLen)
                        {
                            offset++;
                            continue; /* Loop/switch isn't completed */
                        }
                        if(offset + off2 + 3 >= bufend)
                        {
                            if(!found)
                                header.headerOffset = offset;
                            found = true;
                            status = MPA_HDR_DOUBTED;
                            offset++;
                            continue; /* Loop/switch isn't completed */
                        }
                    }
                    break label0;
                }
                catch(Exception ex)
                {
                    System.err.println("Exception: off " + offset + " off2 " + off2 + " bufend " + bufend);
                    ex.printStackTrace();
                    return MPA_ERR_NOHDR;
                }
            }
            header.headerOffset = offset;
            found = true;
            status = MPA_OK;
        }
_L3:
        offset = header.headerOffset;
        int id = inData[offset + 1] >> 3 & 1;
        int layer = inData[offset + 1] >> 1 & 3;
        int crc = inData[offset + 1] & 1;
        int bitrateIndex = inData[offset + 2] >> 4 & 0xf;
        int samplingIndex = inData[offset + 2] >> 2 & 3;
        int paddingBit = inData[offset + 2] >> 1 & 1;
        int channelMode = inData[offset + 3] >> 6 & 3;
        header.layer = 4 - layer;
        header.nSamples = SAMPLES_PER_FRAME[id][layer];
        header.samplingRate = SAMPLE_TABLE[id][samplingIndex];
        header.bitRate = id != MPA_MPEG1 ? BITRATE_TABLE2[layer][bitrateIndex] : BITRATE_TABLE1[layer][bitrateIndex];
        header.nChannels = channelMode != MPA_MONO ? 2 : 1;
        if(header.bitRate > 0)
        {
            header.bitsInFrame = (header.bitRate * 1000 * header.nSamples) / header.samplingRate & ~SLOT_BITS_MASK[layer];
            if(paddingBit != 0)
                header.bitsInFrame += SLOT_BITS_MASK[layer] + 1;
        } else
        if(status == MPA_OK)
            header.bitsInFrame = off2 << 3;
        else
            header.bitsInFrame = bufend - offset << 3;
        if(layer == MPA_LAYER3)
        {
            int hoff = crc != 1 ? 6 : 4;
            if(id == MPA_MPEG1)
                header.negOffset = (inData[offset + hoff] & 0xff) << 1 | inData[offset + hoff + 1] >> 7 & 1;
            else
                header.negOffset = inData[offset + hoff] & 0xff;
        } else
        {
            header.negOffset = 0;
        }
        if(!firstFound && status == MPA_OK)
        {
            firstFound = true;
            firstId = id;
            firstLayer = layer;
            firstSamplingRate = header.samplingRate;
        }
        return status;
        if(true) goto _L1; else goto _L4
_L4:
        if(true) goto _L6; else goto _L5
_L5:
    }

    private static int MPA_MAX_BYTES_IN_FRAME = 2024;
    private static int MPA_MIN_BYTES_IN_FRAME = 21;
    private static int MPA_NSAMP = 1152;
    private static int MPA_LAYER1 = 3;
    private static int MPA_LAYER2 = 2;
    private static int MPA_LAYER3 = 1;
    private static int MPA_MPEG1 = 1;
    private static int MPA_MPEG2 = 0;
    private static int MPA_MONO = 3;
    public static int SAMPLE_TABLE[][] = {
        {
            22050, 24000, 16000, 0
        }, {
            44100, 48000, 32000, 0
        }
    };
    public static int BITRATE_TABLE1[][] = {
        {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
            -1, -1, -1, -1, -1, -1
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
    };
    public static int BITRATE_TABLE2[][] = {
        {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
            -1, -1, -1, -1, -1, -1
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
    };
    private static int MAX_FREE_BITS[] = {
        0, 11520, 13824, 5376
    };
    public static int SLOT_BITS_MASK[] = {
        0, 7, 7, 31
    };
    public static int SAMPLES_PER_FRAME[][] = {
        {
            0, 576, 1152, 384
        }, {
            0, 1152, 1152, 384
        }
    };
    public static int MPA_OK = 0;
    public static int MPA_HDR_DOUBTED = 1;
    public static int MPA_ERR_LOWBUFFER = -1;
    public static int MPA_ERR_NULLPTR = -2;
    public static int MPA_ERR_NOHDR = -3;
    private boolean firstFound;
    private int firstId;
    private int firstLayer;
    private int firstSamplingRate;

}
