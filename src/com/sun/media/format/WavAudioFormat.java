// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   WavAudioFormat.java

package com.sun.media.format;

import java.util.Hashtable;
import javax.media.Format;
import javax.media.format.AudioFormat;

public class WavAudioFormat extends AudioFormat
{

    public WavAudioFormat(String encoding)
    {
        super(encoding);
        codecSpecificHeader = null;
        averageBytesPerSecond = -1;
    }

    public WavAudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels, int frameSizeInBits, int averageBytesPerSecond, 
            byte codecSpecificHeader[])
    {
        super(encoding, sampleRate, sampleSizeInBits, channels);
        this.codecSpecificHeader = null;
        this.averageBytesPerSecond = -1;
        this.codecSpecificHeader = codecSpecificHeader;
        this.averageBytesPerSecond = averageBytesPerSecond;
        super.frameRate = averageBytesPerSecond;
        super.frameSizeInBits = frameSizeInBits;
    }

    public WavAudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels, int frameSizeInBits, int averageBytesPerSecond, 
            int endian, int signed, float frameRate, Class dataType, byte codecSpecificHeader[])
    {
        super(encoding, sampleRate, sampleSizeInBits, channels, endian, signed, frameSizeInBits, frameRate, dataType);
        this.codecSpecificHeader = null;
        this.averageBytesPerSecond = -1;
        this.codecSpecificHeader = codecSpecificHeader;
        this.averageBytesPerSecond = averageBytesPerSecond;
        super.frameRate = averageBytesPerSecond;
    }

    public int getAverageBytesPerSecond()
    {
        return averageBytesPerSecond;
    }

    public byte[] getCodecSpecificHeader()
    {
        return codecSpecificHeader;
    }

    public boolean equals(Object format)
    {
        if(format instanceof WavAudioFormat)
        {
            WavAudioFormat other = (WavAudioFormat)format;
            if(!super.equals(format))
                return false;
            if(codecSpecificHeader == null && other.codecSpecificHeader == null)
                return true;
            if(codecSpecificHeader == null || other.codecSpecificHeader == null)
                return false;
            if(codecSpecificHeader.length != other.codecSpecificHeader.length)
                return false;
            for(int i = 0; i < codecSpecificHeader.length; i++)
                if(codecSpecificHeader[i] != other.codecSpecificHeader[i])
                    return false;

            return true;
        } else
        {
            return false;
        }
    }

    public boolean matches(Format format)
    {
        if(!super.matches(format))
            return false;
        if(!(format instanceof WavAudioFormat))
            return true;
        WavAudioFormat other = (WavAudioFormat)format;
        if(codecSpecificHeader == null || other.codecSpecificHeader == null)
            return true;
        if(codecSpecificHeader.length != other.codecSpecificHeader.length)
            return false;
        for(int i = 0; i < codecSpecificHeader.length; i++)
            if(codecSpecificHeader[i] != other.codecSpecificHeader[i])
                return false;

        return true;
    }

    public Format intersects(Format format)
    {
        Format fmt;
        if((fmt = super.intersects(format)) == null)
            return null;
        if(!(format instanceof WavAudioFormat))
        {
            return fmt;
        } else
        {
            WavAudioFormat other = (WavAudioFormat)format;
            WavAudioFormat res = (WavAudioFormat)fmt;
            res.codecSpecificHeader = codecSpecificHeader == null ? other.codecSpecificHeader : codecSpecificHeader;
            return res;
        }
    }

    public Object clone()
    {
        WavAudioFormat f = new WavAudioFormat(super.encoding);
        f.copy(this);
        return f;
    }

    protected void copy(Format f)
    {
        super.copy(f);
        WavAudioFormat other = (WavAudioFormat)f;
        averageBytesPerSecond = other.averageBytesPerSecond;
        codecSpecificHeader = other.codecSpecificHeader;
    }

    public static final int WAVE_FORMAT_PCM = 1;
    public static final int WAVE_FORMAT_ADPCM = 2;
    public static final int WAVE_FORMAT_ALAW = 6;
    public static final int WAVE_FORMAT_MULAW = 7;
    public static final int WAVE_FORMAT_OKI_ADPCM = 16;
    public static final int WAVE_FORMAT_DIGISTD = 21;
    public static final int WAVE_FORMAT_DIGIFIX = 22;
    public static final int WAVE_FORMAT_GSM610 = 49;
    public static final int WAVE_IBM_FORMAT_MULAW = 257;
    public static final int WAVE_IBM_FORMAT_ALAW = 258;
    public static final int WAVE_IBM_FORMAT_ADPCM = 259;
    public static final int WAVE_FORMAT_DVI_ADPCM = 17;
    public static final int WAVE_FORMAT_SX7383 = 7175;
    public static final int WAVE_FORMAT_DSPGROUP_TRUESPEECH = 34;
    public static final int WAVE_FORMAT_MSNAUDIO = 50;
    public static final int WAVE_FORMAT_MSG723 = 66;
    public static final int WAVE_FORMAT_MPEG_LAYER3 = 85;
    public static final int WAVE_FORMAT_VOXWARE_AC8 = 112;
    public static final int WAVE_FORMAT_VOXWARE_AC10 = 113;
    public static final int WAVE_FORMAT_VOXWARE_AC16 = 114;
    public static final int WAVE_FORMAT_VOXWARE_AC20 = 115;
    public static final int WAVE_FORMAT_VOXWARE_METAVOICE = 116;
    public static final int WAVE_FORMAT_VOXWARE_METASOUND = 117;
    public static final int WAVE_FORMAT_VOXWARE_RT29H = 118;
    public static final int WAVE_FORMAT_VOXWARE_VR12 = 119;
    public static final int WAVE_FORMAT_VOXWARE_VR18 = 120;
    public static final int WAVE_FORMAT_VOXWARE_TQ40 = 121;
    public static final int WAVE_FORMAT_VOXWARE_TQ60 = 129;
    public static final int WAVE_FORMAT_MSRT24 = 130;
    protected byte codecSpecificHeader[];
    private int averageBytesPerSecond;
    public static final Hashtable formatMapper;
    public static final Hashtable reverseFormatMapper;

    static 
    {
        formatMapper = new Hashtable();
        reverseFormatMapper = new Hashtable();
        formatMapper.put(new Integer(1), "LINEAR");
        formatMapper.put(new Integer(2), "msadpcm");
        formatMapper.put(new Integer(17), "ima4/ms");
        formatMapper.put(new Integer(6), "alaw");
        formatMapper.put(new Integer(7), "ULAW");
        formatMapper.put(new Integer(49), "gsm/ms");
        formatMapper.put(new Integer(34), "truespeech");
        formatMapper.put(new Integer(50), "msnaudio");
        formatMapper.put(new Integer(112), "voxwareac8");
        formatMapper.put(new Integer(113), "voxwareac10");
        formatMapper.put(new Integer(114), "voxwareac16");
        formatMapper.put(new Integer(115), "voxwareac20");
        formatMapper.put(new Integer(116), "voxwaremetavoice");
        formatMapper.put(new Integer(117), "voxwaremetasound");
        formatMapper.put(new Integer(118), "voxwarert29h");
        formatMapper.put(new Integer(119), "voxwarevr12");
        formatMapper.put(new Integer(120), "voxwarevr18");
        formatMapper.put(new Integer(121), "voxwaretq40");
        formatMapper.put(new Integer(129), "voxwaretq60");
        formatMapper.put(new Integer(85), "mpeglayer3");
        formatMapper.put(new Integer(130), "msrt24");
        reverseFormatMapper.put("LINEAR".toLowerCase(), new Integer(1));
        reverseFormatMapper.put("msadpcm".toLowerCase(), new Integer(2));
        reverseFormatMapper.put("ima4/ms".toLowerCase(), new Integer(17));
        reverseFormatMapper.put("alaw".toLowerCase(), new Integer(6));
        reverseFormatMapper.put("ULAW".toLowerCase(), new Integer(7));
        reverseFormatMapper.put("gsm/ms".toLowerCase(), new Integer(49));
        reverseFormatMapper.put("truespeech".toLowerCase(), new Integer(34));
        reverseFormatMapper.put("msnaudio".toLowerCase(), new Integer(50));
        reverseFormatMapper.put("voxwareac8".toLowerCase(), new Integer(112));
        reverseFormatMapper.put("voxwareac10".toLowerCase(), new Integer(113));
        reverseFormatMapper.put("voxwareac16".toLowerCase(), new Integer(114));
        reverseFormatMapper.put("voxwareac20".toLowerCase(), new Integer(115));
        reverseFormatMapper.put("voxwaremetavoice".toLowerCase(), new Integer(116));
        reverseFormatMapper.put("voxwaremetasound".toLowerCase(), new Integer(117));
        reverseFormatMapper.put("voxwarert29h".toLowerCase(), new Integer(118));
        reverseFormatMapper.put("voxwarevr12".toLowerCase(), new Integer(119));
        reverseFormatMapper.put("voxwarevr18".toLowerCase(), new Integer(120));
        reverseFormatMapper.put("voxwaretq40".toLowerCase(), new Integer(121));
        reverseFormatMapper.put("voxwaretq60".toLowerCase(), new Integer(129));
        reverseFormatMapper.put("mpeglayer3".toLowerCase(), new Integer(85));
        reverseFormatMapper.put("msrt24".toLowerCase(), new Integer(130));
    }
}
