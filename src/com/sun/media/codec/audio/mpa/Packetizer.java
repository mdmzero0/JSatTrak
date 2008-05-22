// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Packetizer.java

package com.sun.media.codec.audio.mpa;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.codec.audio.AudioCodec;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.sun.media.codec.audio.mpa:
//            MPAParse, MPAHeader, PacketSizeAdapter

public class Packetizer extends AudioCodec
{

    public Packetizer()
    {
        state = 0;
        pendingData = new byte[0x20000];
        pendingDataSize = 0;
        pendingDataOffset = 0;
        expectingSameInputBuffer = false;
        inputEOM = false;
        setMark = true;
        resetTime = true;
        frameSize = 0;
        frameOffset = 0;
        frameCount = 0L;
        packetSize = 0;
        packetSeq = 0L;
        currentTime = 1L;
        deltaTime = 0L;
        mpaHeader = null;
        mpaParse = new MPAParse();
        packetSize = 1456;
        super.inputFormats = (new AudioFormat[] {
            new AudioFormat("mpeglayer3", 16000D, -1, -1, -1, 1), new AudioFormat("mpeglayer3", 22050D, -1, -1, -1, 1), new AudioFormat("mpeglayer3", 24000D, -1, -1, -1, 1), new AudioFormat("mpeglayer3", 32000D, -1, -1, -1, 1), new AudioFormat("mpeglayer3", 44100D, -1, -1, -1, 1), new AudioFormat("mpeglayer3", 48000D, -1, -1, -1, 1), new AudioFormat("mpegaudio", 16000D, -1, -1, -1, 1), new AudioFormat("mpegaudio", 22050D, -1, -1, -1, 1), new AudioFormat("mpegaudio", 24000D, -1, -1, -1, 1), new AudioFormat("mpegaudio", 32000D, -1, -1, -1, 1), 
            new AudioFormat("mpegaudio", 44100D, -1, -1, -1, 1), new AudioFormat("mpegaudio", 48000D, -1, -1, -1, 1)
        });
        super.outputFormats = (new AudioFormat[] {
            new AudioFormat("mpegaudio/rtp", -1D, -1, -1, -1, 1)
        });
    }

    public String getName()
    {
        return "MPEG Audio Packetizer";
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        if(in == null)
            return (new Format[] {
                new AudioFormat("mpegaudio/rtp")
            });
        if(BasicPlugIn.matches(in, super.inputFormats) == null)
            return new Format[1];
        if(!(in instanceof AudioFormat))
            return (new Format[] {
                new AudioFormat("mpegaudio/rtp")
            });
        else
            return getMatchingOutputFormats(in);
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        super.outputFormats = (new AudioFormat[] {
            new AudioFormat("mpegaudio/rtp", af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels(), af.getEndian(), 1, af.getFrameSizeInBits(), af.getFrameRate(), Format.byteArray)
        });
        return super.outputFormats;
    }

    public void open()
        throws ResourceUnavailableException
    {
        setPacketSize(packetSize);
        reset();
        currentTime = 1L;
        packetSeq = 0L;
        resetTime = true;
    }

    public synchronized void reset()
    {
        super.reset();
        mpaParse.reset();
        resetPendingData();
        state = 0;
        setMark = true;
        expectingSameInputBuffer = false;
        frameSize = 0;
        frameOffset = 0;
        frameCount = 0L;
        resetTime = true;
        deltaTime = 0L;
    }

    public void close()
    {
    }

    public synchronized int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(inputBuffer.isDiscard())
        {
            updateOutput(outputBuffer, super.outputFormat, 0, 0);
            outputBuffer.setDiscard(true);
            return 0;
        }
        try
        {
            int rc = doProcess(inputBuffer, outputBuffer);
            if(rc != 4)
            {
                outputBuffer.setSequenceNumber(packetSeq++);
                outputBuffer.setTimeStamp(currentTime);
            }
            if(inputEOM)
            {
                if(outputBuffer.getLength() == 0)
                {
                    propagateEOM(outputBuffer);
                    outputBuffer.setSequenceNumber(packetSeq++);
                    mpaParse.reset();
                    resetPendingData();
                    state = 0;
                    return 0;
                }
                if(rc == 4)
                {
                    outputBuffer.setSequenceNumber(packetSeq++);
                    outputBuffer.setTimeStamp(currentTime);
                }
                rc = 2;
                expectingSameInputBuffer = true;
            } else
            if(pendingDataSize <= 1738)
            {
                rc &= -3;
                shiftPendingData();
            }
            return rc;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return 1;
    }

    protected int doProcess(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(!checkInputBuffer(inputBuffer))
            return 1;
        inputEOM = false;
        if(isEOM(inputBuffer))
        {
            if(pendingDataSize == 0)
            {
                propagateEOM(outputBuffer);
                mpaParse.reset();
                resetPendingData();
                state = 0;
                return 0;
            }
            inputEOM = true;
        }
        byte inData[] = (byte[])inputBuffer.getData();
        int inOffset = inputBuffer.getOffset();
        int inLength = inputBuffer.getLength();
        if(!expectingSameInputBuffer)
        {
            if(inLength > 0 && inData != null)
            {
                if(resetTime)
                {
                    currentTime = inputBuffer.getTimeStamp();
                    if(currentTime == 0L)
                        currentTime = 1L;
                    resetTime = false;
                }
                if(inLength > pendingData.length - pendingDataSize)
                {
                    mpaParse.reset();
                    resetPendingData();
                }
                System.arraycopy(inData, inOffset, pendingData, pendingDataOffset + pendingDataSize, inLength);
                pendingDataSize += inLength;
            } else
            if(!inputEOM)
                return 4;
            expectingSameInputBuffer = true;
        }
        if(mpaHeader == null)
            mpaHeader = getMPAHeader(pendingData, pendingDataOffset, pendingDataSize);
        switch(state)
        {
        case 0: // '\0'
            return newFrameNewBuffer(outputBuffer);

        case 1: // '\001'
            return continueFrameInBuffer(outputBuffer);

        case 2: // '\002'
            return continueFrameNewBuffer(outputBuffer);

        case 3: // '\003'
            return newFrameInBuffer(outputBuffer);
        }
        return 1;
    }

    protected int continueFrameNewBuffer(Buffer outputBuffer)
    {
        int copyLen = Math.min(pendingDataSize, frameSize - frameOffset);
        checkMPAHeader(pendingData, pendingDataOffset, pendingDataSize);
        if(mpaHeader != null && mpaHeader.headerOffset == pendingDataOffset)
        {
            state = 0;
            return newFrameNewBuffer(outputBuffer);
        }
        copyLen = Math.min(copyLen, packetSize - 4);
        setStartOfBuffer(outputBuffer, frameOffset);
        if(!copyBuffer(pendingData, pendingDataOffset, copyLen, outputBuffer))
        {
            state = 0;
            return 1;
        }
        frameOffset += copyLen;
        outputBuffer.setTimeStamp(currentTime);
        outputBuffer.setFormat(super.outputFormat);
        if(copyLen < pendingDataSize)
        {
            pendingDataOffset += copyLen;
            pendingDataSize -= copyLen;
            if(frameOffset >= frameSize || mpaHeader != null && mpaHeader.headerOffset == pendingDataOffset)
                state = 0;
            return 2;
        }
        pendingDataOffset += copyLen;
        pendingDataSize -= copyLen;
        shiftPendingData();
        if(!isOutputBufferFull(outputBuffer))
        {
            state = 1;
            return 4;
        } else
        {
            return 0;
        }
    }

    protected int continueFrameInBuffer(Buffer outputBuffer)
    {
        checkMPAHeader(pendingData, pendingDataOffset, pendingDataSize);
        int copyLen = Math.min(pendingDataSize, frameSize - frameOffset);
        if(mpaHeader != null && mpaHeader.headerOffset == pendingDataOffset)
        {
            state = 3;
            return newFrameInBuffer(outputBuffer);
        }
        copyLen = Math.min(copyLen, packetSize - outputBuffer.getLength());
        if(!copyBuffer(pendingData, pendingDataOffset, copyLen, outputBuffer))
        {
            state = 0;
            return 1;
        }
        frameOffset += copyLen;
        outputBuffer.setTimeStamp(currentTime);
        outputBuffer.setFormat(super.outputFormat);
        if(copyLen < pendingDataSize)
        {
            pendingDataOffset += copyLen;
            pendingDataSize -= copyLen;
            if(mpaHeader != null && mpaHeader.headerOffset == pendingDataOffset)
            {
                state = 3;
                return newFrameInBuffer(outputBuffer);
            } else
            {
                state = 2;
                return 2;
            }
        }
        pendingDataOffset += copyLen;
        pendingDataSize -= copyLen;
        shiftPendingData();
        if(!isOutputBufferFull(outputBuffer))
        {
            state = 1;
            return 4;
        } else
        {
            return 0;
        }
    }

    protected int newFrameNewBuffer(Buffer outputBuffer)
    {
        if(mpaHeader == null)
        {
            shiftPendingData();
            return 4;
        }
        if(mpaHeader.headerOffset != pendingDataOffset)
        {
            pendingDataSize += pendingDataOffset - mpaHeader.headerOffset;
            pendingDataOffset = mpaHeader.headerOffset;
        }
        frameSize = mpaHeader.bitsInFrame >> 3;
        String encoding = "mpegaudio/rtp";
        AudioFormat af = (AudioFormat)super.outputFormat;
        if(af == null || af.getEncoding() != encoding || af.getSampleRate() != (double)mpaHeader.samplingRate || af.getChannels() != mpaHeader.nChannels)
        {
            int endian = 1;
            if(af != null)
                endian = af.getEndian();
            super.outputFormat = new AudioFormat(encoding, mpaHeader.samplingRate, 16, mpaHeader.nChannels, endian, 1);
        }
        setStartOfBuffer(outputBuffer, 0);
        int copyLen = Math.min(mpaHeader.bitsInFrame >> 3, pendingDataSize);
        copyLen = Math.min(copyLen, packetSize - 4);
        if(!copyBuffer(pendingData, pendingDataOffset, copyLen, outputBuffer))
        {
            state = 0;
            return 1;
        }
        frameOffset = copyLen;
        frameCount++;
        currentTime += deltaTime;
        deltaTime = ((long)mpaHeader.nSamples * 1000L * 0xf4240L) / (long)mpaHeader.samplingRate;
        outputBuffer.setFormat(super.outputFormat);
        outputBuffer.setTimeStamp(currentTime);
        if(copyLen < pendingDataSize)
        {
            pendingDataOffset += copyLen;
            pendingDataSize -= copyLen;
            if(copyLen == mpaHeader.bitsInFrame >> 3)
            {
                state = 3;
                mpaHeader = getMPAHeader(pendingData, pendingDataOffset, pendingDataSize);
                return newFrameInBuffer(outputBuffer);
            } else
            {
                state = 2;
                mpaHeader = null;
                return 2;
            }
        }
        pendingDataOffset += copyLen;
        pendingDataSize -= copyLen;
        if(copyLen == mpaHeader.bitsInFrame >> 3)
        {
            shiftPendingData();
            if(isOutputBufferFull(outputBuffer))
            {
                state = 0;
                return 0;
            } else
            {
                state = 3;
                return 4;
            }
        }
        if(!isOutputBufferFull(outputBuffer))
        {
            state = 1;
            shiftPendingData();
            return 4;
        } else
        {
            state = 2;
            shiftPendingData();
            return 0;
        }
    }

    protected int newFrameInBuffer(Buffer outputBuffer)
    {
        if(mpaHeader == null)
        {
            state = 0;
            shiftPendingData();
            return 0;
        }
        if(pendingDataSize <= 1738 && !inputEOM)
        {
            state = 3;
            shiftPendingData();
            return 4;
        }
        if(mpaHeader.headerOffset != pendingDataOffset)
        {
            pendingDataSize += pendingDataOffset - mpaHeader.headerOffset;
            pendingDataOffset = mpaHeader.headerOffset;
        }
        int copyLen = mpaHeader.bitsInFrame >> 3;
        if(copyLen > pendingDataSize)
        {
            state = 3;
            shiftPendingData();
            return 4;
        }
        if(copyLen > packetSize - outputBuffer.getLength())
        {
            state = 0;
            return 2;
        }
        if(!copyBuffer(pendingData, pendingDataOffset, copyLen, outputBuffer))
        {
            state = 0;
            return 2;
        }
        frameCount++;
        deltaTime += ((long)mpaHeader.nSamples * 1000L * 0xf4240L) / (long)mpaHeader.samplingRate;
        pendingDataOffset += copyLen;
        pendingDataSize -= copyLen;
        if(pendingDataSize == 0)
        {
            state = 3;
            shiftPendingData();
            return 4;
        } else
        {
            state = 3;
            mpaHeader = getMPAHeader(pendingData, pendingDataOffset, pendingDataSize);
            return newFrameInBuffer(outputBuffer);
        }
    }

    protected void shiftPendingData()
    {
        if(pendingDataOffset != 0 && pendingDataSize > 0)
            System.arraycopy(pendingData, pendingDataOffset, pendingData, 0, pendingDataSize);
        pendingDataOffset = 0;
        expectingSameInputBuffer = false;
        mpaHeader = null;
    }

    protected void resetPendingData()
    {
        pendingDataSize = 0;
        pendingDataOffset = 0;
        expectingSameInputBuffer = false;
        mpaHeader = null;
    }

    protected void checkMPAHeader(byte inData[], int inOffset, int inLength)
    {
        int off = (inOffset + frameSize) - frameOffset;
        if(mpaHeader == null || mpaHeader.headerOffset == off)
        {
            return;
        } else
        {
            int len = inLength - (frameSize - frameOffset);
            mpaHeader = getMPAHeader(inData, off, len);
            return;
        }
    }

    protected MPAHeader getMPAHeader(byte inData[], int inOffset, int inLength)
    {
        MPAHeader header = new MPAHeader();
        int rc = mpaParse.getHeader(header, inData, inOffset, inLength);
        Packetizer _tmp = this;
        if(rc == MPAParse.MPA_OK)
            return header;
        if(inputEOM)
        {
            Packetizer _tmp1 = this;
            if(rc == MPAParse.MPA_HDR_DOUBTED)
                return header;
        }
        return null;
    }

    public Object[] getControls()
    {
        if(super.controls == null)
        {
            super.controls = new Control[1];
            super.controls[0] = new PacketSizeAdapter(this, packetSize, true);
        }
        return (Object[])super.controls;
    }

    public synchronized void setPacketSize(int newPacketSize)
    {
        packetSize = newPacketSize;
    }

    private boolean isOutputBufferFull(Buffer outputBuffer)
    {
        return packetSize <= outputBuffer.getLength() + 40;
    }

    private void setStartOfBuffer(Buffer outputBuffer, int frameOff)
    {
        byte outData[] = (byte[])outputBuffer.getData();
        if(outData == null || packetSize > outData.length)
        {
            outData = new byte[packetSize];
            outputBuffer.setData(outData);
        }
        outData[0] = 0;
        outData[1] = 0;
        outData[2] = (byte)(frameOff >> 8);
        outData[3] = (byte)frameOff;
        outputBuffer.setOffset(0);
        outputBuffer.setLength(4);
        if(setMark)
        {
            outputBuffer.setFlags(2048);
            setMark = false;
        } else
        {
            outputBuffer.setFlags(0);
        }
    }

    private boolean copyBuffer(byte inData[], int inOff, int inLen, Buffer outputBuffer)
    {
        byte outData[] = (byte[])outputBuffer.getData();
        int outOff = outputBuffer.getLength();
        if(outOff + inLen > outData.length)
        {
            if(outOff + inLen > packetSize)
                return false;
            byte newData[] = new byte[packetSize];
            if(outOff > 0)
                System.arraycopy(outData, 0, newData, 0, outOff);
            outData = newData;
            outputBuffer.setData(outData);
        }
        System.arraycopy(inData, inOff, outData, outOff, inLen);
        outputBuffer.setLength(outOff + inLen);
        return true;
    }

    private static final int NEW_FRAME = 0;
    private static final int CONT_FRAME = 1;
    private static final int CONT_BUFFER = 2;
    private static final int FILL_BUFFER = 3;
    private int state;
    public static final int MAX_MPA_FRAMESIZE = 1729;
    public static final int MAX_FRAMESIZE = 1456;
    public static final int MIN_FRAMESIZE = 110;
    public static final int DEFAULT_FRAMESIZE = 1456;
    private static final boolean debug = false;
    private byte pendingData[];
    private int pendingDataSize;
    private int pendingDataOffset;
    private boolean expectingSameInputBuffer;
    private boolean inputEOM;
    private boolean setMark;
    private boolean resetTime;
    private int frameSize;
    private int frameOffset;
    private long frameCount;
    private int packetSize;
    private long packetSeq;
    private long currentTime;
    private long deltaTime;
    private MPAHeader mpaHeader;
    private MPAParse mpaParse;
}
