// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaDecoder.java

package com.sun.media.codec.audio.msadpcm;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.codec.audio.AudioCodec;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;

// Referenced classes of package com.sun.media.codec.audio.msadpcm:
//            MsAdpcmState, MsAdpcm

public class JavaDecoder extends AudioCodec
{

    public JavaDecoder()
    {
        super.inputFormats = (new Format[] {
            new AudioFormat("msadpcm")
        });
    }

    public String getName()
    {
        return "msadpcm decoder";
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        if(in == null)
            return (new Format[] {
                new AudioFormat("LINEAR")
            });
        if(BasicPlugIn.matches(in, super.inputFormats) == null)
            return new Format[0];
        if(!(in instanceof AudioFormat))
            return (new Format[] {
                new AudioFormat("LINEAR")
            });
        AudioFormat af = (AudioFormat)in;
        int frameSize = af.getFrameSizeInBits();
        if(frameSize != 2048 && frameSize != 4096 && frameSize != 8192)
            return new Format[0];
        else
            return (new Format[] {
                new AudioFormat("LINEAR", af.getSampleRate(), 16, af.getChannels(), 0, 1)
            });
    }

    public void open()
    {
        state = new MsAdpcmState[2];
        state[0] = new MsAdpcmState();
        state[1] = new MsAdpcmState();
    }

    public void close()
    {
        state[0] = null;
        state[1] = null;
        state = null;
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(!checkInputBuffer(inputBuffer))
            return 1;
        if(isEOM(inputBuffer))
        {
            propagateEOM(outputBuffer);
            return 0;
        } else
        {
            byte inData[] = (byte[])inputBuffer.getData();
            byte outData[] = validateByteArraySize(outputBuffer, inData.length * 4);
            AudioFormat iaf = (AudioFormat)inputBuffer.getFormat();
            int blockAlign = iaf.getFrameSizeInBits() >> 3;
            int channels = iaf.getChannels();
            int outLength = decode(inData, outData, inputBuffer.getLength(), blockAlign, channels);
            updateOutput(outputBuffer, super.outputFormat, outLength, 0);
            return 0;
        }
    }

    private int decode(byte inpData[], byte outData[], int inLength, int blockAlign, int channels)
    {
        int dataBytesInBlock = blockAlign - 7 * channels;
        int outBytes = 4 * channels + (blockAlign - 7 * channels) * 4;
        int readPtr = 0;
        int writePtr = 0;
        int numberOfBlocks = inLength / blockAlign;
        for(int n = 1; n <= numberOfBlocks;)
        {
            MsAdpcm.decodeBlock(inpData, readPtr, outData, writePtr, dataBytesInBlock, state, channels);
            n++;
            writePtr += outBytes;
            readPtr += blockAlign;
        }

        return writePtr;
    }

    private static final boolean DEBUG = true;
    private MsAdpcmState state[];
}
