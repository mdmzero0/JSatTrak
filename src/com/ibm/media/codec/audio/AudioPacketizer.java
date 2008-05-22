// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AudioPacketizer.java

package com.ibm.media.codec.audio;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import javax.media.Buffer;

// Referenced classes of package com.ibm.media.codec.audio:
//            AudioCodec

public abstract class AudioPacketizer extends AudioCodec
{

    public AudioPacketizer()
    {
    }

    public synchronized int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        int inpLength = inputBuffer.getLength();
        int outLength = packetSize;
        byte inpData[] = (byte[])inputBuffer.getData();
        byte outData[] = validateByteArraySize(outputBuffer, outLength);
        if(inpLength + historyLength >= packetSize)
        {
            int copyFromHistory = Math.min(historyLength, packetSize);
            System.arraycopy(history, 0, outData, 0, copyFromHistory);
            int remainingBytes = packetSize - copyFromHistory;
            System.arraycopy(inpData, inputBuffer.getOffset(), outData, historyLength, remainingBytes);
            historyLength -= copyFromHistory;
            inputBuffer.setOffset(inputBuffer.getOffset() + remainingBytes);
            inputBuffer.setLength(inpLength - remainingBytes);
            updateOutput(outputBuffer, super.outputFormat, outLength, 0);
            return 2;
        }
        if(inputBuffer.isEOM())
        {
            System.arraycopy(history, 0, outData, 0, historyLength);
            System.arraycopy(inpData, inputBuffer.getOffset(), outData, historyLength, inpLength);
            updateOutput(outputBuffer, super.outputFormat, inpLength + historyLength, 0);
            historyLength = 0;
            return 0;
        } else
        {
            System.arraycopy(inpData, inputBuffer.getOffset(), history, historyLength, inpLength);
            historyLength += inpLength;
            return 4;
        }
    }

    public void reset()
    {
        historyLength = 0;
    }

    protected byte history[];
    protected int packetSize;
    protected int historyLength;
    protected int sample_count;
}
