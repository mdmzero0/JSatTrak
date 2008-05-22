// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaDecoder.java

package com.ibm.media.codec.audio.dvi;

import com.ibm.media.codec.audio.AudioCodec;
import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.controls.SilenceSuppressionAdapter;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.dvi:
//            DVIState, DVI

public class JavaDecoder extends AudioCodec
{

    public JavaDecoder()
    {
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("dvi/rtp")
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR")
        });
        super.PLUGIN_NAME = "DVI Decoder";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        super.supportedOutputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", af.getSampleRate(), 16, af.getChannels(), 0, 1)
        });
        return super.supportedOutputFormats;
    }

    public void open()
    {
        dviState = new DVIState();
    }

    public void close()
    {
        dviState = null;
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        int outLength = 0;
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
            byte outData[] = validateByteArraySize(outputBuffer, (inData.length - 4) * 4);
            int offset = inputBuffer.getOffset();
            int prevVal = inData[offset++] << 8;
            prevVal |= inData[offset++] & 0xff;
            int index = inData[offset++] & 0xff;
            offset++;
            dviState.valprev = prevVal;
            dviState.index = index;
            DVI.decode(inData, offset, outData, 0, 2 * (inputBuffer.getLength() - 4), dviState);
            outLength = 4 * (inputBuffer.getLength() - 4);
            updateOutput(outputBuffer, super.outputFormat, outLength, 0);
            return 0;
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

    private DVIState dviState;
}
