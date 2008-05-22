// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BufferedEncoder.java

package com.ibm.media.codec.audio;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import javax.media.Buffer;

// Referenced classes of package com.ibm.media.codec.audio:
//            AudioCodec

public abstract class BufferedEncoder extends AudioCodec
{

    public BufferedEncoder()
    {
        readBytes = new int[1];
        writeBytes = new int[1];
        frameNumber = new int[1];
        history = new Buffer();
        packetSize = -1;
    }

    public int getPacketSize()
    {
        return packetSize;
    }

    public int setPacketSize(int newPacketSize)
    {
        packetSize = newPacketSize;
        return packetSize;
    }

    public void reset()
    {
        history.setLength(0);
        codecReset();
    }

    protected abstract void codecReset();

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(pendingFrames > 0)
            return 0;
        if(!checkInputBuffer(inputBuffer))
            return 1;
        if(isEOM(inputBuffer))
        {
            propagateEOM(outputBuffer);
            return 0;
        }
        int inpOffset = inputBuffer.getOffset();
        int inpLength = inputBuffer.getLength();
        int outLength = 0;
        int outOffset = 0;
        byte inpData[] = (byte[])inputBuffer.getData();
        byte outData[] = validateByteArraySize(outputBuffer, calculateOutputSize(inpData.length + historySize));
        int historyLength = history.getLength();
        byte historyData[] = validateByteArraySize(history, historySize);
        int framesNumber = calculateFramesNumber(inpData.length + historySize);
        if(regions == null || regions.length < framesNumber + 1)
            regions = new int[framesNumber + 1];
        if(regionsTypes == null || regionsTypes.length < framesNumber)
            regionsTypes = new int[framesNumber];
        if(historyLength != 0)
        {
            int bytesToCopy = historyData.length - historyLength;
            if(bytesToCopy > inpLength)
                bytesToCopy = inpLength;
            System.arraycopy(inpData, inpOffset, historyData, historyLength, bytesToCopy);
            codecProcess(historyData, 0, outData, outOffset, historyLength + bytesToCopy, readBytes, writeBytes, frameNumber, regions, regionsTypes);
            if(readBytes[0] <= 0)
                if(writeBytes[0] <= 0)
                {
                    return 4;
                } else
                {
                    updateOutput(outputBuffer, super.outputFormat, writeBytes[0], 0);
                    return 0;
                }
            outOffset += writeBytes[0];
            outLength += writeBytes[0];
            inpOffset += readBytes[0] - historyLength;
            inpLength += historyLength - readBytes[0];
        }
        codecProcess(inpData, inpOffset, outData, outOffset, inpLength, readBytes, writeBytes, frameNumber, regions, regionsTypes);
        outLength += writeBytes[0];
        inpOffset += readBytes[0];
        inpLength -= readBytes[0];
        System.arraycopy(inpData, inpOffset, historyData, 0, inpLength);
        history.setLength(inpLength);
        updateOutput(outputBuffer, super.outputFormat, outLength, 0);
        return 0;
    }

    protected abstract int calculateOutputSize(int i);

    protected abstract int calculateFramesNumber(int i);

    protected abstract boolean codecProcess(byte abyte0[], int i, byte abyte1[], int j, int k, int ai[], int ai1[], 
            int ai2[], int ai3[], int ai4[]);

    protected int readBytes[];
    protected int writeBytes[];
    protected int frameNumber[];
    protected Buffer history;
    protected int regions[];
    protected int regionsTypes[];
    protected int pendingFrames;
    protected int packetSize;
    protected int historySize;
}
