/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   AudioFormat.java

package javax.media.format;

import javax.media.Format;

public class AudioFormat extends Format
{

    public AudioFormat(String encoding)
    {
        super(encoding);
        sampleRate = -1D;
        sampleSizeInBits = -1;
        channels = -1;
        endian = -1;
        signed = -1;
        frameRate = -1D;
        frameSizeInBits = -1;
        multiplier = -1D;
        margin = 0;
        init = false;
    }

    public AudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels)
    {
        this(encoding);
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
    }

    public AudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels, int endian, int signed)
    {
        this(encoding, sampleRate, sampleSizeInBits, channels);
        this.endian = endian;
        this.signed = signed;
    }

    public AudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels, int endian, int signed, 
            int frameSizeInBits, double frameRate, Class dataType)
    {
        this(encoding, sampleRate, sampleSizeInBits, channels, endian, signed);
        this.frameSizeInBits = frameSizeInBits;
        this.frameRate = frameRate;
        super.dataType = dataType;
    }

    public double getSampleRate()
    {
        return sampleRate;
    }

    public int getSampleSizeInBits()
    {
        return sampleSizeInBits;
    }

    public int getChannels()
    {
        return channels;
    }

    public int getEndian()
    {
        return endian;
    }

    public int getSigned()
    {
        return signed;
    }

    public int getFrameSizeInBits()
    {
        return frameSizeInBits;
    }

    public double getFrameRate()
    {
        return frameRate;
    }

    public long computeDuration(long length)
    {
        if(init)
            if(multiplier < 0.0D)
                return -1L;
            else
                return (long)((double)(length - (long)margin) * multiplier) * 1000L;
        if(super.encoding == null)
        {
            init = true;
            return -1L;
        }
        if(super.encoding.equalsIgnoreCase("LINEAR") || super.encoding.equalsIgnoreCase("ULAW"))
        {
            if(sampleSizeInBits > 0 && channels > 0 && sampleRate > 0.0D)
                multiplier = (double)(0x7a1200 / sampleSizeInBits / channels) / sampleRate;
        } else
        if(super.encoding.equalsIgnoreCase("ULAW/rtp"))
        {
            if(sampleSizeInBits > 0 && channels > 0 && sampleRate > 0.0D)
                multiplier = (double)(0x7a1200 / sampleSizeInBits / channels) / sampleRate;
        } else
        if(super.encoding.equalsIgnoreCase("dvi/rtp"))
        {
            if(sampleSizeInBits > 0 && sampleRate > 0.0D)
                multiplier = (double)(0x7a1200 / sampleSizeInBits) / sampleRate;
            margin = 4;
        } else
        if(super.encoding.equalsIgnoreCase("gsm/rtp"))
        {
            if(sampleRate > 0.0D)
                multiplier = 4848484D / sampleRate;
        } else
        if(super.encoding.equalsIgnoreCase("g723/rtp"))
        {
            if(sampleRate > 0.0D)
                multiplier = 10000000D / sampleRate;
        } else
        if(frameSizeInBits != -1 && frameRate != -1D && frameSizeInBits > 0 && frameRate > 0.0D)
            multiplier = (double)(0x7a1200 / frameSizeInBits) / frameRate;
        init = true;
        if(multiplier > 0.0D)
            return (long)((double)(length - (long)margin) * multiplier) * 1000L;
        else
            return -1L;
    }

    public String toString()
    {
        String strChannels = "";
        String strEndian = "";
        if(channels == 1)
            strChannels = ", Mono";
        else
        if(channels == 2)
            strChannels = ", Stereo";
        else
        if(channels != -1)
            strChannels = ", " + channels + "-channel";
        if(sampleSizeInBits > 8)
            if(endian == 1)
                strEndian = ", BigEndian";
            else
            if(endian == 0)
                strEndian = ", LittleEndian";
        return getEncoding() + (sampleRate == -1D ? ", Unknown Sample Rate" : ", " + sampleRate + " Hz") + (sampleSizeInBits == -1 ? "" : ", " + sampleSizeInBits + "-bit") + strChannels + strEndian + (signed == -1 ? "" : signed != 1 ? ", Unsigned" : ", Signed") + (frameRate == -1D ? "" : ", " + frameRate + " frame rate") + (frameSizeInBits == -1 ? "" : ", FrameSize=" + frameSizeInBits + " bits") + (super.dataType == Format.byteArray || super.dataType == null ? "" : ", " + super.dataType);
    }

    public boolean equals(Object format)
    {
        if(format instanceof AudioFormat)
        {
            AudioFormat other = (AudioFormat)format;
            return super.equals(format) && sampleRate == other.sampleRate && sampleSizeInBits == other.sampleSizeInBits && channels == other.channels && endian == other.endian && signed == other.signed && frameSizeInBits == other.frameSizeInBits && frameRate == other.frameRate;
        } else
        {
            return false;
        }
    }

    public boolean matches(Format format)
    {
        if(!super.matches(format))
            return false;
        if(!(format instanceof AudioFormat))
        {
            return true;
        } else
        {
            AudioFormat other = (AudioFormat)format;
            return (sampleRate == -1D || other.sampleRate == -1D || sampleRate == other.sampleRate) && (sampleSizeInBits == -1 || other.sampleSizeInBits == -1 || sampleSizeInBits == other.sampleSizeInBits) && (channels == -1 || other.channels == -1 || channels == other.channels) && (endian == -1 || other.endian == -1 || endian == other.endian) && (signed == -1 || other.signed == -1 || signed == other.signed) && (frameSizeInBits == -1 || other.frameSizeInBits == -1 || frameSizeInBits == other.frameSizeInBits) && (frameRate == -1D || other.frameRate == -1D || frameRate == other.frameRate);
        }
    }

    public Format intersects(Format format)
    {
        Format fmt;
        if((fmt = super.intersects(format)) == null)
            return null;
        if(!(fmt instanceof AudioFormat))
        {
            return fmt;
        } else
        {
            AudioFormat other = (AudioFormat)format;
            AudioFormat res = (AudioFormat)fmt;
            res.sampleRate = sampleRate == -1D ? other.sampleRate : sampleRate;
            res.sampleSizeInBits = sampleSizeInBits == -1 ? other.sampleSizeInBits : sampleSizeInBits;
            res.channels = channels == -1 ? other.channels : channels;
            res.endian = endian == -1 ? other.endian : endian;
            res.signed = signed == -1 ? other.signed : signed;
            res.frameSizeInBits = frameSizeInBits == -1 ? other.frameSizeInBits : frameSizeInBits;
            res.frameRate = frameRate == -1D ? other.frameRate : frameRate;
            return res;
        }
    }

    public Object clone()
    {
        AudioFormat f = new AudioFormat(super.encoding);
        f.copy(this);
        return f;
    }

    protected void copy(Format f)
    {
        super.copy(f);
        AudioFormat other = (AudioFormat)f;
        sampleRate = other.sampleRate;
        sampleSizeInBits = other.sampleSizeInBits;
        channels = other.channels;
        endian = other.endian;
        signed = other.signed;
        frameSizeInBits = other.frameSizeInBits;
        frameRate = other.frameRate;
    }

    public static final int BIG_ENDIAN = 1;
    public static final int LITTLE_ENDIAN = 0;
    public static final int SIGNED = 1;
    public static final int UNSIGNED = 0;
    protected double sampleRate;
    protected int sampleSizeInBits;
    protected int channels;
    protected int endian;
    protected int signed;
    protected double frameRate;
    protected int frameSizeInBits;
    public static final String LINEAR = "LINEAR";
    public static final String ULAW = "ULAW";
    public static final String ULAW_RTP = "ULAW/rtp";
    public static final String ALAW = "alaw";
    public static final String IMA4 = "ima4";
    public static final String IMA4_MS = "ima4/ms";
    public static final String MSADPCM = "msadpcm";
    public static final String DVI = "dvi";
    public static final String DVI_RTP = "dvi/rtp";
    public static final String G723 = "g723";
    public static final String G723_RTP = "g723/rtp";
    public static final String G728 = "g728";
    public static final String G728_RTP = "g728/rtp";
    public static final String G729 = "g729";
    public static final String G729_RTP = "g729/rtp";
    public static final String G729A = "g729a";
    public static final String G729A_RTP = "g729a/rtp";
    public static final String GSM = "gsm";
    public static final String GSM_MS = "gsm/ms";
    public static final String GSM_RTP = "gsm/rtp";
    public static final String MAC3 = "MAC3";
    public static final String MAC6 = "MAC6";
    public static final String TRUESPEECH = "truespeech";
    public static final String MSNAUDIO = "msnaudio";
    public static final String MPEGLAYER3 = "mpeglayer3";
    public static final String VOXWAREAC8 = "voxwareac8";
    public static final String VOXWAREAC10 = "voxwareac10";
    public static final String VOXWAREAC16 = "voxwareac16";
    public static final String VOXWAREAC20 = "voxwareac20";
    public static final String VOXWAREMETAVOICE = "voxwaremetavoice";
    public static final String VOXWAREMETASOUND = "voxwaremetasound";
    public static final String VOXWARERT29H = "voxwarert29h";
    public static final String VOXWAREVR12 = "voxwarevr12";
    public static final String VOXWAREVR18 = "voxwarevr18";
    public static final String VOXWARETQ40 = "voxwaretq40";
    public static final String VOXWARETQ60 = "voxwaretq60";
    public static final String MSRT24 = "msrt24";
    public static final String MPEG = "mpegaudio";
    public static final String MPEG_RTP = "mpegaudio/rtp";
    public static final String DOLBYAC3 = "dolbyac3";
    double multiplier;
    int margin;
    boolean init;
}
