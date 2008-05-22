// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RCModule.java

package com.ibm.media.codec.audio.rc;

import com.ibm.media.codec.audio.AudioCodec;
import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import java.io.PrintStream;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.rc:
//            RateConversion

public class RCModule extends AudioCodec
{

    public RCModule()
    {
        rateConversion = null;
        lastInputFormat = null;
        lastOutputFormat = null;
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", -1D, 16, 2, -1, -1), new AudioFormat("LINEAR", -1D, 16, 1, -1, -1), new AudioFormat("LINEAR", -1D, 8, 2, -1, -1), new AudioFormat("LINEAR", -1D, 8, 1, -1, -1)
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", 8000D, 16, 2, 0, 1), new AudioFormat("LINEAR", 8000D, 16, 1, 0, 1)
        });
        super.PLUGIN_NAME = "Rate Conversion";
    }

    public Format setInputFormat(Format format)
    {
        if(!isSampleRateSupported(format))
            return null;
        else
            return super.setInputFormat(format);
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        if(!isSampleRateSupported(in))
        {
            return new Format[0];
        } else
        {
            super.supportedOutputFormats = (new AudioFormat[] {
                new AudioFormat("LINEAR", 8000D, 16, 1, 0, 1), new AudioFormat("LINEAR", 8000D, 16, 2, 0, 1)
            });
            return super.supportedOutputFormats;
        }
    }

    public void open()
        throws ResourceUnavailableException
    {
    }

    public void reset()
    {
        if(null != rateConversion)
            rateConversion.reset();
    }

    public void close()
    {
        if(null != rateConversion)
            rateConversion.close();
        rateConversion = null;
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(!checkInputBuffer(inputBuffer))
            return 1;
        if(isEOM(inputBuffer))
        {
            propagateEOM(outputBuffer);
            return 0;
        }
        int inputLength = inputBuffer.getLength();
        if((lastInputFormat != super.inputFormat || lastOutputFormat != super.outputFormat || rateConversion == null) && !initConverter(super.inputFormat, super.outputFormat, inputLength))
        {
            return 1;
        } else
        {
            int maxOutLength = rateConversion.getMaxOutputLength(inputLength);
            byte inputData[] = (byte[])inputBuffer.getData();
            byte outData[] = validateByteArraySize(outputBuffer, maxOutLength);
            int outLength = rateConversion.process(inputData, inputBuffer.getOffset(), inputLength, outData, outputBuffer.getOffset());
            updateOutput(outputBuffer, super.outputFormat, outLength, outputBuffer.getOffset());
            return 0;
        }
    }

    private boolean isSampleRateSupported(Format format)
    {
        try
        {
            int sampleRate = (int)((AudioFormat)format).getSampleRate();
            if(sampleRate != 11025 && sampleRate != 11127 && sampleRate != 16000 && sampleRate != 22050 && sampleRate != 22254 && sampleRate != 22255 && sampleRate != 32000 && sampleRate != 44100 && sampleRate != 48000)
            {
                if(DEBUG)
                    System.out.println("RCModule - input format sampling rate isn't supported");
                return false;
            }
        }
        catch(Throwable t)
        {
            return false;
        }
        return true;
    }

    private boolean initConverter(AudioFormat inFormat, AudioFormat outFormat, int inputLength)
    {
        lastInputFormat = inFormat;
        lastOutputFormat = outFormat;
        boolean isSigned = false;
        int numberOfInputChannels = inFormat.getChannels();
        int numberOfOutputChannels = outFormat.getChannels();
        int inputSampleSize = inFormat.getSampleSizeInBits();
        int sampleRate = (int)inFormat.getSampleRate();
        boolean ulawOutput = false;
        if(sampleRate == 8000)
            return false;
        int pcmType = 1;
        if(inFormat.getEndian() == 1)
            pcmType = 0;
        if(8 == inputSampleSize)
            pcmType = 2;
        if(inFormat.getSigned() == 1)
            isSigned = true;
        if(rateConversion != null)
            close();
        if(outFormat.getEncoding() == "ULAW")
            ulawOutput = true;
        rateConversion = new RateConversion();
        if(-1 != rateConversion.init(inputLength, sampleRate, 8000, numberOfInputChannels, numberOfOutputChannels, pcmType, isSigned, ulawOutput))
        {
            rateConversion = null;
            return false;
        } else
        {
            return true;
        }
    }

    private RateConversion rateConversion;
    private Format lastInputFormat;
    private Format lastOutputFormat;
    private static boolean DEBUG = false;

}
