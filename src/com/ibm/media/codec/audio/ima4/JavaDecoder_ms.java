// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaDecoder_ms.java

package com.ibm.media.codec.audio.ima4;

import com.ibm.media.codec.audio.AudioCodec;
import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.controls.SilenceSuppressionAdapter;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.ima4:
//            IMA4State, IMA4

public class JavaDecoder_ms extends AudioCodec
{

    public JavaDecoder_ms()
    {
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("ima4/ms")
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR")
        });
        super.PLUGIN_NAME = "IMA4 MS Decoder";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        int fs = af.getFrameSizeInBits();
        int channels = af.getChannels();
        if(fs % (32 * channels) != 0)
        {
            return new Format[0];
        } else
        {
            super.supportedOutputFormats = (new AudioFormat[] {
                new AudioFormat("LINEAR", af.getSampleRate(), 16, af.getChannels(), 1, 1)
            });
            return super.supportedOutputFormats;
        }
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
            int blockSize = super.inputFormat.getFrameSizeInBits() >> 3;
            int outLength = decodeJavaMSIMA4(inData, outData, inputBuffer.getLength(), outData.length, channels, blockSize);
            updateOutput(outputBuffer, super.outputFormat, outLength, 0);
            return 0;
        }
    }

    private int decodeJavaMSIMA4(byte inBuffer[], byte outBuffer[], int lenIn, int lenOut, int nChannels, int blockSize)
    {
        switch(nChannels)
        {
        case 1: // '\001'
            return decodeMSIMA4mono(inBuffer, outBuffer, lenIn, lenOut, blockSize);

        case 2: // '\002'
            return decodeMSIMA4stereo(inBuffer, outBuffer, lenIn, lenOut, blockSize);
        }
        throw new RuntimeException("MSIMA4: Can only handle 1 or 2 channels\n");
    }

    private int decodeMSIMA4mono(byte inBuffer[], byte outBuffer[], int lenIn, int lenOut, int blockSize)
    {
        int inCount = 0;
        int outCount = 0;
        for(lenIn = (lenIn / blockSize) * blockSize; inCount < lenIn;)
        {
            int prevVal = inBuffer[inCount++] & 0xff;
            prevVal |= inBuffer[inCount++] << 8;
            int index = inBuffer[inCount++] & 0xff;
            if(index > 88)
                index = 88;
            inCount++;
            outBuffer[outCount++] = (byte)(prevVal >> 8);
            outBuffer[outCount++] = (byte)prevVal;
            ima4state.valprev = prevVal;
            ima4state.index = index;
            IMA4.decode(inBuffer, inCount, outBuffer, outCount, blockSize - 4 << 1, ima4state, 0);
            inCount += blockSize - 4;
            outCount += blockSize - 4 << 2;
        }

        return outCount;
    }

    private int decodeMSIMA4stereo(byte inBuffer[], byte outBuffer[], int lenIn, int lenOut, int blockSize)
    {
        int inCount = 0;
        int outCount = 0;
        lenIn = (lenIn / blockSize) * blockSize;
        for(int i = 0; i < outBuffer.length; i++)
            outBuffer[i] = 0;

        while(inCount < lenIn) 
        {
            int storedinCount = inCount;
            int storedoutCount = outCount;
            int prevValL = inBuffer[inCount++] & 0xff;
            prevValL |= inBuffer[inCount++] << 8;
            int indexL = inBuffer[inCount++] & 0xff;
            if(indexL > 88)
                indexL = 88;
            inCount++;
            outBuffer[outCount++] = (byte)(prevValL >> 8);
            outBuffer[outCount++] = (byte)prevValL;
            outCount += 2;
            inCount += 4;
            ima4state.valprev = prevValL;
            ima4state.index = indexL;
            for(int i = blockSize - 8; i > 0; i -= 8)
            {
                IMA4.decode(inBuffer, inCount, outBuffer, outCount, 8, ima4state, 2);
                inCount += 8;
                outCount += 32;
            }

            inCount = storedinCount + 4;
            outCount = storedoutCount + 2;
            int prevValR = inBuffer[inCount++] & 0xff;
            prevValR |= inBuffer[inCount++] << 8;
            int indexR = inBuffer[inCount++] & 0xff;
            if(indexR > 88)
                indexR = 88;
            inCount++;
            outBuffer[outCount++] = (byte)(prevValR >> 8);
            outBuffer[outCount++] = (byte)prevValR;
            ima4state.valprev = prevValR;
            ima4state.index = indexR;
            outCount += 2;
            inCount += 4;
            for(int i = blockSize - 8; i > 0; i -= 8)
            {
                IMA4.decode(inBuffer, inCount, outBuffer, outCount, 8, ima4state, 2);
                inCount += 8;
                outCount += 32;
            }

            inCount = storedinCount + blockSize;
            outCount = storedoutCount + (blockSize - 8 << 2) + 4;
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
