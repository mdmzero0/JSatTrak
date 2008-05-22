// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MpegVideo.java

package com.ibm.media.codec.video.mpeg;

import com.sun.media.*;
import java.awt.Component;
import java.awt.Dimension;
import java.io.PrintStream;
import javax.media.*;
import javax.media.control.FrameProcessingControl;
import javax.media.control.QualityControl;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;

public class MpegVideo extends BasicCodec
{
    class DC
        implements FrameProcessingControl, QualityControl
    {

        public int getFramesDropped()
        {
            return 0;
        }

        public Component getControlComponent()
        {
            return null;
        }

        public boolean setMinimalProcessing(boolean on)
        {
            setMpegFramesBehind(5);
            return true;
        }

        public void setFramesBehind(float framesBehind)
        {
            setMpegFramesBehind((int)framesBehind);
        }

        public float setQuality(float quality)
        {
            return 1.0F;
        }

        public float getQuality()
        {
            return 1.0F;
        }

        public float getPreferredQuality()
        {
            return 1.0F;
        }

        public boolean isTemporalSpatialTradeoffSupported()
        {
            return false;
        }

        DC()
        {
        }
    }


    private native int videoInitialize(long al[], byte abyte0[], int i, int j);

    private native int initImageParam(long l, byte abyte0[], int i, int j, int ai[], int ai1[]);

    private native int videoDecode(long l, byte abyte0[], int i, int j, int k, Object obj, 
            long l1, int i1, int j1, int ai[], int ai1[], int ai2[], 
            int ai3[], int ai4[], int ai5[]);

    private native int videoTerminate(long l);

    private native int videoReset(long l);

    private native int videoSeek(long l);

    public MpegVideo()
    {
        methodsSync = new Object();
        supportedInFormats = null;
        supportedOutFormats = null;
        inputFormat = null;
        outputFormat = null;
        corruptedFlag = false;
        firstTimeFlag = true;
        inputEOMFlag = false;
        processEOMFlag = false;
        resetFlag = true;
        accumulatedTimeNs = 0L;
        deltaPictureTimeNS = 0.0D;
        dAccumulatedTime = 0.0D;
        pdata = new long[1];
        versionBuf = new byte[70];
        inBytesReq = new int[1];
        frameNumber = -1;
        inBufByte = new byte[0x19000];
        outBufByte = null;
        inDataOffset = 0;
        outBufOffset = 0;
        numDataBytes = 0;
        numFramesBehind = 0;
        dropCount = 0;
        imgWidth = new int[1];
        imgHeight = new int[1];
        imgType = new int[1];
        inBytesRead = new int[1];
        outBufWrote = new int[1];
        controls = null;
        dc = null;
        if(available)
        {
            supportedInFormats = new VideoFormat[2];
            supportedInFormats[0] = new VideoFormat("mpeg");
            supportedInFormats[1] = new VideoFormat("MPGI");
            supportedOutFormats = new VideoFormat[1];
            supportedOutFormats[0] = new YUVFormat(2);
        } else
        {
            supportedInFormats = new VideoFormat[0];
        }
    }

    public String getName()
    {
        return "MPEG-1 Video Decoder";
    }

    public Format[] getSupportedInputFormats()
    {
        return supportedInFormats;
    }

    public Format[] getSupportedOutputFormats(Format format)
    {
        if(format == null)
            return supportedOutFormats;
        if((format instanceof VideoFormat) && BasicPlugIn.matches(format, supportedInFormats) != null)
        {
            VideoFormat inf = (VideoFormat)format;
            Dimension size = inf.getSize();
            if(size == null || size.width == 0 || size.height == 0)
                size = new Dimension(320, 240);
            int area = size.width * size.height;
            YUVFormat outf = new YUVFormat(size, area + (area >> 1), Format.byteArray, inf.getFrameRate(), 2, size.width, size.width >> 1, 0, area, area + (area >> 2));
            Format tempFormats[] = new Format[1];
            tempFormats[0] = outf;
            return tempFormats;
        } else
        {
            return new Format[0];
        }
    }

    public Format setInputFormat(Format in)
    {
        if(!(in instanceof VideoFormat) || BasicPlugIn.matches(in, supportedInFormats) == null)
        {
            return null;
        } else
        {
            inputFormat = (VideoFormat)in;
            return in;
        }
    }

    public Format setOutputFormat(Format out)
    {
        if(out != null && (out instanceof YUVFormat) && ((YUVFormat)out).getYuvType() == 2)
        {
            outputFormat = (VideoFormat)out;
            return out;
        } else
        {
            return null;
        }
    }

    public void open()
        throws ResourceUnavailableException
    {
        if(!available)
            throw new ResourceUnavailableException("Can't find shared library jmmpegv");
        synchronized(methodsSync)
        {
            if(videoInitialize(pdata, versionBuf, 0, 70) != 0)
                throw new ResourceUnavailableException("MPEG video decoder initialization failed");
            available = false;
            inBytesReq[0] = 0x10000;
        }
        float ftmp = inputFormat.getFrameRate();
        if(ftmp > 0.0F)
            deltaPictureTimeNS = 1000000000D / (double)ftmp;
        else
            deltaPictureTimeNS = 0.0D;
    }

    public void reset()
    {
        synchronized(methodsSync)
        {
            corruptedFlag = false;
            inputEOMFlag = false;
            processEOMFlag = false;
            inBytesReq[0] = 0x10000;
            frameNumber = -1;
            inDataOffset = 0;
            outBufOffset = 0;
            numDataBytes = 0;
            numFramesBehind = 0;
            dropCount = 0;
            int rc = videoSeek(pdata[0]);
            if(rc != 0)
                corruptedFlag = true;
            resetFlag = true;
        }
    }

    public void close()
    {
        synchronized(methodsSync)
        {
            videoTerminate(pdata[0]);
            corruptedFlag = false;
            firstTimeFlag = true;
            inputEOMFlag = false;
            processEOMFlag = false;
            pdata = new long[1];
            inBytesReq[0] = 0x10000;
            frameNumber = -1;
            inDataOffset = 0;
            outBufOffset = 0;
            numDataBytes = 0;
            numFramesBehind = 0;
            dropCount = 0;
            available = true;
        }
    }

    private boolean doInit(Buffer inbuffer, Buffer outbuffer)
    {
        int imgWidth[] = new int[1];
        int imgHeight[] = new int[1];
        Object data = inbuffer.getData();
        int bufLen = inbuffer.getLength();
        int offset = inbuffer.getOffset();
        if(bufLen < 512)
            return false;
        int rc = initImageParam(pdata[0], (byte[])data, offset, bufLen, imgWidth, imgHeight);
        if(rc != 0)
        {
            return false;
        } else
        {
            int frameWriteLen = (int)((double)(imgWidth[0] * imgHeight[0]) * 1.5D);
            outBufByte = new byte[frameWriteLen];
            Dimension size = new Dimension(imgWidth[0], imgHeight[0]);
            int area = size.width * size.height;
            outputFormat = new YUVFormat(size, area + (area >> 1), Format.byteArray, inputFormat.getFrameRate(), 2, size.width, size.width >> 1, 0, area, area + (area >> 2));
            outbuffer.setFormat(outputFormat);
            return true;
        }
    }

    public int process(Buffer inbuffer, Buffer outbuffer)
    {
        imgWidth[0] = 0;
        imgHeight[0] = 0;
        imgType[0] = 0;
        inBytesRead[0] = 0;
        outBufWrote[0] = 0;
        int returnResult = 0;
        int l1;
        synchronized(methodsSync)
        {
            if(!checkInputBuffer(inbuffer))
            {
                int i = 1;
                return i;
            }
            if(processEOMFlag)
            {
                propagateEOM(outbuffer);
                int j = 0;
                return j;
            }
            if(corruptedFlag)
            {
                int k = 1;
                return k;
            }
            if(isEOM(inbuffer))
                inputEOMFlag = true;
            if(firstTimeFlag)
            {
                if(!doInit(inbuffer, outbuffer))
                {
                    int l = 1;
                    return l;
                }
                firstTimeFlag = false;
            }
            if(numDataBytes < inBytesReq[0] && !inputEOMFlag)
            {
                byte data[] = (byte[])inbuffer.getData();
                int bufLen = inbuffer.getLength();
                int offset = inbuffer.getOffset();
                int num2read = 0x19000 - numDataBytes - 1;
                if(numDataBytes > 0 && inDataOffset > 0)
                    System.arraycopy(inBufByte, inDataOffset, inBufByte, 0, numDataBytes);
                inDataOffset = 0;
                if(num2read > bufLen)
                    num2read = bufLen;
                if(num2read > 0)
                    System.arraycopy(data, offset, inBufByte, numDataBytes, num2read);
                if(num2read < bufLen)
                    returnResult |= 2;
                inbuffer.setOffset(offset + num2read);
                inbuffer.setLength(bufLen - num2read);
                if(bufLen == num2read)
                    inbuffer.setOffset(0);
                if(num2read > 0)
                    numDataBytes += num2read;
                if(numDataBytes < inBytesReq[0])
                {
                    int i2 = returnResult | 0 | 4;
                    return i2;
                }
            } else
            if(inputEOMFlag)
            {
                if(processEOMFlag)
                {
                    propagateEOM(outbuffer);
                    int i1 = 0;
                    return i1;
                }
            } else
            {
                returnResult |= 2;
            }
            frameNumber++;
            if((inbuffer.getFlags() & 0x1000) == 0)
            {
                if(resetFlag)
                {
                    accumulatedTimeNs = inbuffer.getTimeStamp();
                    dAccumulatedTime = 1.0D * (double)accumulatedTimeNs;
                    resetFlag = false;
                }
                outbuffer.setTimeStamp(accumulatedTimeNs);
                dAccumulatedTime += deltaPictureTimeNS;
                accumulatedTimeNs = (long)dAccumulatedTime;
            } else
            {
                outbuffer.setTimeStamp(inbuffer.getTimeStamp());
            }
            outbuffer.setSequenceNumber(frameNumber);
            outbuffer.setFlags(outbuffer.getFlags() | 0x20);
            Object outData = validateData(outbuffer, 0, true);
            long outBytes = getNativeData(outData);
            int rc = videoDecode(pdata[0], inBufByte, inDataOffset, numDataBytes, numFramesBehind, outData, outBytes, 0, outputFormat.getMaxDataLength(), imgWidth, imgHeight, imgType, inBytesRead, outBufWrote, inBytesReq);
            if(rc != 0)
            {
                if(rc == -5)
                {
                    propagateEOM(outbuffer);
                    processEOMFlag = true;
                    int j1 = 0;
                    return j1;
                }
                System.out.println("MPEG VIDEO: decode process error  rc:" + rc);
                System.out.flush();
                corruptedFlag = true;
                int k1 = 1;
                return k1;
            }
            updateOutput(outbuffer, outputFormat, outBufWrote[0], 0);
            numDataBytes -= inBytesRead[0];
            inDataOffset += inBytesRead[0];
            if(outBufWrote[0] == 0)
            {
                outbuffer.setDiscard(true);
                returnResult |= 4;
                if(imgType[0] != 0)
                    dropCount++;
            }
            l1 = returnResult | 0;
        }
        return l1;
    }

    public Object[] getControls()
    {
        if(dc == null)
        {
            dc = new DC();
            controls = new Control[1];
            controls[0] = dc;
        }
        return controls;
    }

    protected void propagateEOM(Buffer outBuffer)
    {
        super.propagateEOM(outBuffer);
        outBuffer.setTimeStamp(accumulatedTimeNs);
        dAccumulatedTime += deltaPictureTimeNS;
        accumulatedTimeNs = (long)dAccumulatedTime;
    }

    void setMpegFramesBehind(int num)
    {
        numFramesBehind = num;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1998, 1999.";
    static final int VERSION_BUF_LEN = 70;
    static final int MPEG_INTERNAL_BUF_SIZE = 0x10000;
    static final int IN_STREAM_BUF_LEN = 0x19000;
    static final int OUT_FRAME_BUF_LEN = 0x41eb0;
    static final int NO_PICTURE = 0;
    static final int I_PICTURE = 1;
    static final int P_PICTURE = 2;
    static final int B_PICTURE = 3;
    static final int D_PICTURE = 4;
    static final int MPEG_NOERROR = 0;
    static final int MPEG_ERROR = -1;
    static final int MPEG_PARAM_ERROR = -2;
    static final int MPEG_BUF_ERROR = -3;
    static final int MPEG_ALLOC_ERROR = -4;
    static final int MPEG_EOS = -5;
    private Object methodsSync;
    protected VideoFormat supportedInFormats[];
    protected VideoFormat supportedOutFormats[];
    protected VideoFormat inputFormat;
    protected VideoFormat outputFormat;
    private boolean corruptedFlag;
    private boolean firstTimeFlag;
    private boolean inputEOMFlag;
    private boolean processEOMFlag;
    private boolean resetFlag;
    private long accumulatedTimeNs;
    private double deltaPictureTimeNS;
    private double dAccumulatedTime;
    private long pdata[];
    private byte versionBuf[];
    private int inBytesReq[];
    private int frameNumber;
    private byte inBufByte[];
    private byte outBufByte[];
    private int inDataOffset;
    private int outBufOffset;
    private int numDataBytes;
    private int numFramesBehind;
    private int dropCount;
    private int imgWidth[];
    private int imgHeight[];
    private int imgType[];
    private int inBytesRead[];
    private int outBufWrote[];
    private static boolean available;
    private Control controls[];
    private DC dc;

    static 
    {
        try
        {
            JMFSecurityManager.loadLibrary("jmutil");
            JMFSecurityManager.loadLibrary("jmmpegv");
            available = true;
        }
        catch(Exception e)
        {
            available = false;
        }
    }
}
