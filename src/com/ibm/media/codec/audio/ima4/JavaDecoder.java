// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaDecoder.java

package com.ibm.media.codec.audio.ima4;

import com.ibm.media.codec.audio.AudioCodec;
import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.controls.SilenceSuppressionAdapter;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.ima4:
//            IMA4State, IMA4

public class JavaDecoder extends AudioCodec
{

    public JavaDecoder()
    {
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("ima4")
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR")
        });
        super.PLUGIN_NAME = "IMA4 Decoder";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        super.supportedOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", af.getSampleRate(), 16, af.getChannels(), 1, 1)
        });
        return super.supportedOutputFormats;
    }

    public void open()
    {
        ima4state = new IMA4State();
    }

    public void close()
    {
        ima4state = null;
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
            int channels = super.outputFormat.getChannels();
            byte inData[] = (byte[])inputBuffer.getData();
            byte outData[] = validateByteArraySize(outputBuffer, inData.length * 4);
            int outLength = decodeJavaIMA4(inData, outData, inputBuffer.getLength(), outData.length, channels);
            updateOutput(outputBuffer, super.outputFormat, outLength, 0);
            return 0;
        }
    }

    int decodeJavaIMA4(byte inData[], byte outData[], int lenIn, int lenOut, int nChannels)
    {
        switch(nChannels)
        {
        case 1: // '\001'
            return decodeIMA4mono(inData, outData, lenIn, lenOut, 32);

        case 2: // '\002'
            return decodeIMA4stereo(inData, outData, lenIn, lenOut, 32);
        }
        throw new RuntimeException("IMA4: Can only handle 1 or 2 channels\n");
    }

    private int decodeIMA4mono(byte inData[], byte outData[], int lenIn, int lenOut, int blockSize)
    {
        int inCount = 0;
        int outCount = 0;
        for(lenIn = (lenIn / (blockSize + 2)) * (blockSize + 2); inCount < lenIn;)
        {
            int state = inData[inCount++] << 8;
            state |= inData[inCount++] & 0xff;
            int index = state & 0x7f;
            if(index > 88)
                index = 88;
            ima4state.valprev = state & 0xffffff80;
            ima4state.index = index;
            IMA4.decode(inData, inCount, outData, outCount, blockSize << 1, ima4state, 0);
            inCount += blockSize;
            outCount += blockSize << 2;
        }

        return outCount;
    }

    private int decodeIMA4stereo(byte inData[], byte outData[], int lenIn, int lenOut, int blockSize)
    {
        int inCount = 0;
        int outCount = 0;
        for(lenIn = (lenIn / 2 / (blockSize + 2)) * (blockSize + 2) * 2; inCount < lenIn;)
        {
            int stateL = inData[inCount++] << 8;
            stateL |= inData[inCount++] & 0xff;
            int indexL = stateL & 0x7f;
            if(indexL > 88)
                indexL = 88;
            ima4state.valprev = stateL & 0xffffff80;
            ima4state.index = indexL;
            IMA4.decode(inData, inCount, outData, outCount, blockSize << 1, ima4state, 2);
            inCount += blockSize;
            int stateR = inData[inCount++] << 8;
            stateR |= inData[inCount++] & 0xff;
            int indexR = stateR & 0x7f;
            if(indexR > 88)
                indexR = 88;
            ima4state.valprev = stateR & 0xffffff80;
            ima4state.index = indexR;
            IMA4.decode(inData, inCount, outData, outCount + 2, blockSize << 1, ima4state, 2);
            inCount += blockSize;
            outCount += blockSize << 3;
        }

        return outCount;
    }

    public Object[] getControls()
    {
        if(super.controls == null)
        {
            super.controls = new Control[1];
            super.controls[0] = new SilenceSuppressionAdapter(this, false, false);
        }
        return (Object[])super.controls;
    }

    private IMA4State ima4state;
}
