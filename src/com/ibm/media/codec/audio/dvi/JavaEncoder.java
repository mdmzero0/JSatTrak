// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaEncoder.java

package com.ibm.media.codec.audio.dvi;

import com.ibm.media.codec.audio.AudioCodec;
import com.ibm.media.codec.audio.AudioPacketizer;
import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.controls.SilenceSuppressionAdapter;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.ibm.media.codec.audio.dvi:
//            DVIState, PacketSizeAdapter, DVI

public class JavaEncoder extends AudioPacketizer
{

    public JavaEncoder()
    {
        pcmBuffer = new Buffer();
        dviState = new DVIState();
        currentSeq = 0L;
        super.packetSize = 240;
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("LINEAR", -1D, 16, 1, 0, 1, -1, -1D, Format.byteArray)
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("dvi/rtp", -1D, 4, 1, -1, -1, -1, -1D, Format.byteArray)
        });
        super.PLUGIN_NAME = "DVI Encoder";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        super.supportedOutputFormats = (new AudioFormat[] {
            new AudioFormat("dvi/rtp", af.getSampleRate(), 4, 1, -1, -1, -1, -1D, Format.byteArray)
        });
        return super.supportedOutputFormats;
    }

    public Format setOutputFormat(Format out)
    {
        Format f = super.setOutputFormat(out);
        AudioFormat af = (AudioFormat)f;
        if(af.getSampleRate() == 8000D)
            super.packetSize = 240;
        else
        if(af.getSampleRate() == 11025D)
            super.packetSize = 330;
        else
        if(af.getSampleRate() == 22050D)
            super.packetSize = 660;
        else
        if(af.getSampleRate() == 44100D)
            super.packetSize = 1320;
        return f;
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        int rc = super.process(inputBuffer, pcmBuffer);
        if((rc & 4) != 0)
        {
            return rc;
        } else
        {
            byte pcmData[] = (byte[])pcmBuffer.getData();
            int inpLength = pcmBuffer.getLength();
            int outLength = pcmBuffer.getLength() / 4;
            byte outData[] = validateByteArraySize(outputBuffer, outLength + 4);
            outData[0] = (byte)(dviState.valprev >> 8);
            outData[1] = (byte)dviState.valprev;
            outData[2] = (byte)dviState.index;
            outData[3] = 0;
            DVI.encode(pcmData, 0, outData, 4, inpLength >> 1, dviState);
            pcmBuffer.setOffset(0);
            pcmBuffer.setLength(0);
            outputBuffer.setSequenceNumber(currentSeq++);
            outputBuffer.setTimeStamp(pcmBuffer.getTimeStamp());
            updateOutput(outputBuffer, super.outputFormat, outLength + 4, 0);
            return rc;
        }
    }

    public void open()
        throws ResourceUnavailableException
    {
        dviState = new DVIState();
        setPacketSize(super.packetSize);
        reset();
    }

    public void reset()
    {
        super.reset();
        dviState.valprev = 0;
        dviState.index = 0;
        pcmBuffer.setOffset(0);
        pcmBuffer.setLength(0);
    }

    public Object[] getControls()
    {
        if(super.controls == null)
        {
            super.controls = new Control[2];
            super.controls[0] = new PacketSizeAdapter(this, super.packetSize, true);
            super.controls[1] = new SilenceSuppressionAdapter(this, false, false);
        }
        return (Object[])super.controls;
    }

    public synchronized void setPacketSize(int newPacketSize)
    {
        super.packetSize = newPacketSize * 4;
        super.sample_count = super.packetSize / 2;
        if(super.history == null)
        {
            super.history = new byte[super.packetSize];
            return;
        }
        if(super.packetSize > super.history.length)
        {
            byte newHistory[] = new byte[super.packetSize];
            System.arraycopy(super.history, 0, newHistory, 0, super.historyLength);
            super.history = newHistory;
        }
    }

    private Buffer pcmBuffer;
    private DVIState dviState;
    private long currentSeq;
}
