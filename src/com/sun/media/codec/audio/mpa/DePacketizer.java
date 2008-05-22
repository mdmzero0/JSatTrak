// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DePacketizer.java

package com.sun.media.codec.audio.mpa;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.codec.audio.AudioCodec;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;

// Referenced classes of package com.sun.media.codec.audio.mpa:
//            MPAParse, MPAHeader

public class DePacketizer extends AudioCodec
{

    public DePacketizer()
    {
        bufferContinued = false;
        frameContinued = false;
        frameSize = 0;
        frameBegin = 0;
        frameOffset = 0;
        frameTimeStamp = 0L;
        bufTimeStamp = 0L;
        prevSeq = -1L;
        outSeq = 0L;
        mpaParse = new MPAParse();
        mpaHeader = new MPAHeader();
        super.inputFormats = (new Format[] {
            new AudioFormat("mpegaudio/rtp")
        });
    }

    public String getName()
    {
        return "MPEG Audio DePacketizer";
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        if(in == null)
            return defaultSupportedOutputFormats;
        if(BasicPlugIn.matches(in, super.inputFormats) == null)
            return new Format[1];
        if(!(in instanceof AudioFormat))
            return defaultSupportedOutputFormats;
        if(super.outputFormat != null)
        {
            return (new Format[] {
                super.outputFormat
            });
        } else
        {
            AudioFormat af = (AudioFormat)in;
            AudioFormat of = new AudioFormat("mpegaudio", af.getSampleRate() != -1D ? af.getSampleRate() : 44100D, af.getSampleSizeInBits() != -1 ? af.getSampleSizeInBits() : 16, af.getChannels() != -1 ? af.getChannels() : 2);
            return (new Format[] {
                of
            });
        }
    }

    public void open()
    {
    }

    public void close()
    {
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        try
        {
            return doProcess(inputBuffer, outputBuffer);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return 1;
    }

    public int doProcess(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(!checkInputBuffer(inputBuffer))
            return 1;
        if(isEOM(inputBuffer))
        {
            propagateEOM(outputBuffer);
            mpaParse.reset();
            return 0;
        }
        byte inData[] = (byte[])inputBuffer.getData();
        int inOffset = inputBuffer.getOffset();
        int inLength = inputBuffer.getLength();
        int packetOffset = ((inData[inOffset + 2] & 0xff) << 8) + (inData[inOffset + 3] & 0xff);
        inOffset += 4;
        inLength -= 4;
        if(packetOffset > 0)
        {
            if(!frameContinued)
                return 4;
            if(inputBuffer.getTimeStamp() != frameTimeStamp)
            {
                dropFrame(outputBuffer);
                return 4;
            }
            if(getSequenceDiff(prevSeq, inputBuffer.getSequenceNumber()) != 1)
            {
                dropFrame(outputBuffer);
                return 4;
            }
            prevSeq = inputBuffer.getSequenceNumber();
            if(!copyBuffer(inData, inOffset, inLength, outputBuffer, frameBegin + frameOffset))
            {
                dropFrame(outputBuffer);
                return 4;
            }
            frameOffset += inLength;
            if(frameOffset < frameSize)
                return 4;
            frameContinued = false;
            frameOffset = 0;
            if(mpaHeader.layer == 3 && frameBegin == 0)
            {
                return 4;
            } else
            {
                outputBuffer.setTimeStamp(bufTimeStamp);
                outputBuffer.setFlags(outputBuffer.getFlags() | 0x20);
                bufferContinued = false;
                return 0;
            }
        }
        if(frameContinued)
            dropFrame(outputBuffer);
        frameContinued = false;
        prevSeq = inputBuffer.getSequenceNumber();
        frameTimeStamp = inputBuffer.getTimeStamp();
        int rc = mpaParse.getHeader(mpaHeader, inData, inOffset, inLength);
        DePacketizer _tmp = this;
        if(rc != MPAParse.MPA_OK)
        {
            DePacketizer _tmp1 = this;
            if(rc != MPAParse.MPA_HDR_DOUBTED)
                return 1;
        }
        String encoding = mpaHeader.layer != 3 ? "mpegaudio" : "mpeglayer3";
        AudioFormat af = (AudioFormat)super.outputFormat;
        if(af == null || !encoding.equalsIgnoreCase(af.getEncoding()) || af.getSampleRate() != (double)mpaHeader.samplingRate || af.getChannels() != mpaHeader.nChannels)
            super.outputFormat = new AudioFormat(encoding, mpaHeader.samplingRate, 16, mpaHeader.nChannels, 1, 1);
        frameSize = mpaHeader.bitsInFrame >> 3;
        if(frameSize > inLength)
        {
            if(!bufferContinued)
            {
                outputBuffer.setLength(0);
                outputBuffer.setOffset(0);
                bufTimeStamp = frameTimeStamp;
                outputBuffer.setFormat(super.outputFormat);
                outputBuffer.setSequenceNumber(outSeq++);
            }
            bufferContinued = true;
            frameContinued = true;
            frameBegin = outputBuffer.getLength();
            frameOffset = inLength;
            copyBuffer(inData, inOffset, inLength, outputBuffer, outputBuffer.getLength());
            return 4;
        }
        if(mpaHeader.layer == 3 && inLength < frameSize * 2 - 2)
        {
            if(!bufferContinued)
            {
                outputBuffer.setLength(0);
                outputBuffer.setOffset(0);
                bufTimeStamp = frameTimeStamp;
                byte outData[] = (byte[])outputBuffer.getData();
                if(outData == null || outData.length < OUT_BUF_SIZE)
                {
                    outData = new byte[OUT_BUF_SIZE];
                    outputBuffer.setData(outData);
                }
            }
            if(!copyBuffer(inData, inOffset, inLength, outputBuffer, outputBuffer.getLength()))
            {
                outputBuffer.setFormat(super.outputFormat);
                outputBuffer.setSequenceNumber(outSeq++);
                outputBuffer.setTimeStamp(bufTimeStamp);
                outputBuffer.setFlags(outputBuffer.getFlags() | 0x20);
                bufferContinued = false;
                return 2;
            }
            if(outputBuffer.getLength() + frameSize + 4 > OUT_BUF_SIZE)
            {
                outputBuffer.setFormat(super.outputFormat);
                outputBuffer.setSequenceNumber(outSeq++);
                outputBuffer.setTimeStamp(bufTimeStamp);
                outputBuffer.setFlags(outputBuffer.getFlags() | 0x20);
                bufferContinued = false;
                return 0;
            } else
            {
                bufferContinued = true;
                return 4;
            }
        } else
        {
            Object outData = outputBuffer.getData();
            outputBuffer.setData(inputBuffer.getData());
            inputBuffer.setData(outData);
            outputBuffer.setLength(inLength);
            outputBuffer.setFormat(super.outputFormat);
            outputBuffer.setOffset(inOffset);
            outputBuffer.setSequenceNumber(outSeq++);
            outputBuffer.setTimeStamp(frameTimeStamp);
            outputBuffer.setFlags(outputBuffer.getFlags() | 0x20);
            return 0;
        }
    }

    private boolean copyBuffer(byte inData[], int inOff, int inLen, Buffer outputBuffer, int outOff)
    {
        byte outData[] = (byte[])outputBuffer.getData();
        if(outData == null || outOff + inLen > outData.length)
        {
            if(outOff + inLen > OUT_BUF_SIZE)
                return false;
            byte newData[] = new byte[OUT_BUF_SIZE];
            if(outOff > 0)
                System.arraycopy(outData, 0, newData, 0, outData.length);
            outData = newData;
            outputBuffer.setData(outData);
        }
        System.arraycopy(inData, inOff, outData, outOff, inLen);
        outputBuffer.setLength(outputBuffer.getLength() + inLen);
        return true;
    }

    private int getSequenceDiff(long p, long c)
    {
        if(c > p)
            return (int)(c - p);
        if(c == p)
            return 0;
        if(p > (long)(MAX_SEQ - 100) && c < 100L)
            return (int)(((long)MAX_SEQ - p) + c + 1L);
        else
            return (int)(c - p);
    }

    private void dropFrame(Buffer outputBuffer)
    {
        outputBuffer.setLength(frameBegin - outputBuffer.getOffset());
        frameBegin = outputBuffer.getLength() + outputBuffer.getOffset();
        frameSize = 0;
        frameOffset = 0;
        frameContinued = false;
    }

    private static int OUT_BUF_SIZE = 4096;
    private static int MAX_SEQ = 65535;
    private static Format defaultSupportedOutputFormats[] = {
        new AudioFormat("mpegaudio", 44100D, 16, -1, 1, 1), new AudioFormat("mpegaudio", 48000D, 16, -1, 1, 1), new AudioFormat("mpegaudio", 32000D, 16, -1, 1, 1), new AudioFormat("mpegaudio", 22050D, 16, -1, 1, 1), new AudioFormat("mpegaudio", 24000D, 16, -1, 1, 1), new AudioFormat("mpegaudio", 16000D, 16, -1, 1, 1), new AudioFormat("mpegaudio", 11025D, 16, -1, 1, 1), new AudioFormat("mpegaudio", 12000D, 16, -1, 1, 1), new AudioFormat("mpegaudio", 8000D, 16, -1, 1, 1), new AudioFormat("mpeglayer3", 44100D, 16, -1, 1, 1), 
        new AudioFormat("mpeglayer3", 48000D, 16, -1, 1, 1), new AudioFormat("mpeglayer3", 32000D, 16, -1, 1, 1), new AudioFormat("mpeglayer3", 22050D, 16, -1, 1, 1), new AudioFormat("mpeglayer3", 24000D, 16, -1, 1, 1), new AudioFormat("mpeglayer3", 16000D, 16, -1, 1, 1), new AudioFormat("mpeglayer3", 11025D, 16, -1, 1, 1), new AudioFormat("mpeglayer3", 12000D, 16, -1, 1, 1), new AudioFormat("mpeglayer3", 8000D, 16, -1, 1, 1)
    };
    private boolean bufferContinued;
    private boolean frameContinued;
    private int frameSize;
    private int frameBegin;
    private int frameOffset;
    private long frameTimeStamp;
    private long bufTimeStamp;
    private long prevSeq;
    private long outSeq;
    private MPAParse mpaParse;
    private MPAHeader mpaHeader;
    private static final boolean debug = false;

}
