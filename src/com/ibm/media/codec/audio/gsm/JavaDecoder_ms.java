// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaDecoder_ms.java

package com.ibm.media.codec.audio.gsm;

import com.ibm.media.codec.audio.AudioCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.controls.SilenceSuppressionAdapter;
import javax.media.Control;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.gsm:
//            JavaDecoder, GsmDecoder_ms, GsmDecoder

public class JavaDecoder_ms extends JavaDecoder
{

    public JavaDecoder_ms()
    {
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("gsm/ms")
        });
        super.PLUGIN_NAME = "GSM MS Decoder";
    }

    public void open()
        throws ResourceUnavailableException
    {
        super.decoder = new GsmDecoder_ms();
        super.decoder.decoderInit();
    }

    protected int calculateOutputSize(int inputSize)
    {
        return (inputSize / 65) * 640;
    }

    protected void decode(byte inpData[], int readPtr, byte outData[], int writePtr, int inpLength)
    {
        int numberOfFrames = inpLength / 65;
        for(int n = 1; n <= numberOfFrames;)
        {
            super.decoder.decodeFrame(inpData, readPtr, outData, writePtr);
            n++;
            writePtr += 640;
            readPtr += 65;
        }

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

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1999.";
}
