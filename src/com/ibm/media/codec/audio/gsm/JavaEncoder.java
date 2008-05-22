// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaEncoder.java

package com.ibm.media.codec.audio.gsm;

import com.ibm.media.codec.audio.AudioCodec;
import com.ibm.media.codec.audio.BufferedEncoder;
import com.sun.media.BasicCodec;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.gsm:
//            GsmVadEncoder, GsmEncoder

public class JavaEncoder extends BufferedEncoder
{

    public JavaEncoder()
    {
        sample_count = 160;
        currentSeq = (long)((double)System.currentTimeMillis() * Math.random());
        timestamp = 0L;
        pendingBuffer = null;
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", -1D, 16, 1, 0, 1, -1, -1D, Format.byteArray)
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("gsm")
        });
        super.PLUGIN_NAME = "GSM Encoder";
        super.historySize = 320;
        super.pendingFrames = 0;
        super.packetSize = 33;
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        super.supportedOutputFormats = (new AudioFormat[] {
            new AudioFormat("gsm", af.getSampleRate(), 16, af.getChannels(), -1, -1, 264, -1D, Format.byteArray)
        });
        return super.supportedOutputFormats;
    }

    public void open()
        throws ResourceUnavailableException
    {
        encoder = new GsmVadEncoder();
        encoder.gsm_encoder_reset();
    }

    public void codecReset()
    {
        encoder.gsm_encoder_reset();
    }

    public void close()
    {
        encoder = null;
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        int retVal = super.process(inputBuffer, outputBuffer);
        if(!super.outputFormat.getEncoding().equals("gsm/rtp"))
            return retVal;
        if(super.outputFormat.getEncoding().equals("gsm/rtp"))
        {
            if(retVal == 1)
                return retVal;
            if(isEOM(inputBuffer))
            {
                propagateEOM(outputBuffer);
                return 0;
            }
            if(super.pendingFrames == 0)
                pendingBuffer = (byte[])outputBuffer.getData();
            byte outData[] = new byte[super.packetSize];
            outputBuffer.setData(outData);
            updateOutput(outputBuffer, super.outputFormat, super.packetSize, 0);
            outputBuffer.setSequenceNumber(currentSeq++);
            outputBuffer.setTimeStamp(timestamp);
            timestamp += sample_count;
            System.arraycopy(pendingBuffer, super.regions[super.pendingFrames], outData, 0, super.packetSize);
            if(super.pendingFrames + 1 == super.frameNumber[0])
            {
                super.pendingFrames = 0;
                pendingBuffer = null;
                return 0;
            } else
            {
                super.pendingFrames++;
                return 2;
            }
        } else
        {
            return retVal;
        }
    }

    protected int calculateOutputSize(int inputSize)
    {
        return calculateFramesNumber(inputSize) * 33;
    }

    protected int calculateFramesNumber(int inputSize)
    {
        return inputSize / 320;
    }

    protected boolean codecProcess(byte inpData[], int readPtr, byte outData[], int writePtr, int inpLength, int readBytes[], int writeBytes[], 
            int frameNumber[], int regions[], int regionsTypes[])
    {
        int inCount = 0;
        int outCount = 0;
        int channels = super.inputFormat.getChannels();
        boolean isStereo = channels == 2;
        int frames = inpLength / 320;
        regions[0] = writePtr;
        for(int frameCounter = 0; frameCounter < frames; frameCounter++)
        {
            encoder.gsm_encode_frame(inpData, readPtr, outData, writePtr);
            readPtr += 320;
            inCount += 320;
            outCount += 33;
            writePtr += 33;
            regions[frameCounter + 1] = writePtr;
            regionsTypes[frameCounter] = 0;
        }

        readBytes[0] = inCount;
        writeBytes[0] = outCount;
        frameNumber[0] = frames;
        return true;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1999.";
    protected GsmEncoder encoder;
    private int sample_count;
    private long currentSeq;
    private long timestamp;
    byte pendingBuffer[];
}
