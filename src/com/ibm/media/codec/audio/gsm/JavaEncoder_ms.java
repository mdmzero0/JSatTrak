// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaEncoder_ms.java

package com.ibm.media.codec.audio.gsm;

import com.ibm.media.codec.audio.AudioCodec;
import com.ibm.media.codec.audio.BufferedEncoder;
import com.sun.media.format.WavAudioFormat;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.gsm:
//            JavaEncoder, GsmEncoder_ms, GsmEncoder

public class JavaEncoder_ms extends JavaEncoder
{

    public JavaEncoder_ms()
    {
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", -1D, 16, 1, 0, 1, -1, -1D, Format.byteArray)
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new WavAudioFormat("gsm/ms")
        });
        super.PLUGIN_NAME = "MS GSM Encoder";
        super.historySize = 640;
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        super.supportedOutputFormats = (new AudioFormat[] {
            new WavAudioFormat("gsm/ms", af.getSampleRate(), 0, af.getChannels(), 520, (int)(((af.getSampleRate() * (double)af.getChannels()) / 320D) * 65D), -1, -1, -1F, Format.byteArray, new byte[] {
                64, 1
            })
        });
        return super.supportedOutputFormats;
    }

    public void open()
        throws ResourceUnavailableException
    {
        super.encoder = new GsmEncoder_ms();
        super.encoder.gsm_encoder_reset();
    }

    protected int calculateOutputSize(int inputSize)
    {
        return calculateFramesNumber(inputSize) * 65;
    }

    protected int calculateFramesNumber(int inputSize)
    {
        return inputSize / 640;
    }

    protected boolean codecProcess(byte inpData[], int readPtr, byte outData[], int writePtr, int inpLength, int readBytes[], int writeBytes[], 
            int frameNumber[], int regions[], int regionsTypes[])
    {
        int inCount = 0;
        int outCount = 0;
        int channels = super.inputFormat.getChannels();
        boolean isStereo = channels == 2;
        int frames = inpLength / 640;
        regions[0] = writePtr;
        for(int frameCounter = 0; frameCounter < frames; frameCounter++)
        {
            super.encoder.gsm_encode_frame(inpData, readPtr, outData, writePtr);
            readPtr += 640;
            inCount += 640;
            outCount += 65;
            writePtr += 65;
            regions[frameCounter + 1] = outCount + writePtr;
            regionsTypes[frameCounter] = 0;
        }

        readBytes[0] = inCount;
        writeBytes[0] = outCount;
        frameNumber[0] = frames;
        return true;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1999.";
}
