// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Packetizer.java

package com.sun.media.codec.audio.ulaw;

import com.ibm.media.codec.audio.AudioCodec;
import com.ibm.media.codec.audio.AudioPacketizer;
import com.sun.media.BasicPlugIn;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.sun.media.codec.audio.ulaw:
//            PacketSizeAdapter

public class Packetizer extends AudioPacketizer
{

    public Packetizer()
    {
        super.packetSize = 480;
        super.supportedInputFormats = (new AudioFormat[] {
            new AudioFormat("ULAW", -1D, 8, 1, -1, -1, 8, -1D, Format.byteArray)
        });
        super.defaultOutputFormats = (new AudioFormat[] {
            new AudioFormat("ULAW/rtp", -1D, 8, 1, -1, -1, 8, -1D, Format.byteArray)
        });
        super.PLUGIN_NAME = "Mu-Law Packetizer";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        AudioFormat af = (AudioFormat)in;
        super.supportedOutputFormats = (new AudioFormat[] {
            new AudioFormat("ULAW/rtp", af.getSampleRate(), 8, 1, -1, -1, 8, -1D, Format.byteArray)
        });
        return super.supportedOutputFormats;
    }

    public void open()
        throws ResourceUnavailableException
    {
        setPacketSize(super.packetSize);
        reset();
    }

    public Object[] getControls()
    {
        if(super.controls == null)
        {
            super.controls = new Control[1];
            super.controls[0] = new PacketSizeAdapter(this, super.packetSize, true);
        }
        return (Object[])super.controls;
    }

    public synchronized void setPacketSize(int newPacketSize)
    {
        super.packetSize = newPacketSize;
        super.sample_count = super.packetSize;
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
}
