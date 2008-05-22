// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaSoundOutput.java

package com.sun.media.renderer.audio.device;

import com.sun.media.Log;
import javax.media.format.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

// Referenced classes of package com.sun.media.renderer.audio.device:
//            AudioOutput

public class JavaSoundOutput
    implements AudioOutput
{

    public JavaSoundOutput()
    {
        paused = true;
        lastPos = 0L;
        originPos = 0L;
        totalCount = 0L;
    }

    public boolean initialize(AudioFormat format, int bufSize)
    {
        boolean flag2;
        synchronized(initSync)
        {
            javax.sound.sampled.AudioFormat afmt = convertFormat(format);
            javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, afmt, bufSize);
            try
            {
                if(!AudioSystem.isLineSupported(info))
                {
                    Log.warning("DataLine not supported: " + format);
                    boolean flag = false;
                    return flag;
                }
                dataLine = (SourceDataLine)AudioSystem.getLine(info);
                dataLine.open(afmt, bufSize);
            }
            catch(Exception e)
            {
                Log.warning("Cannot open audio device: " + e);
                boolean flag3 = false;
                return flag3;
            }
            this.format = format;
            this.bufSize = bufSize;
            if(dataLine == null)
            {
                Log.warning("JavaSound unsupported format: " + format);
                boolean flag1 = false;
                return flag1;
            }
            try
            {
                gc = (FloatControl)dataLine.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                mc = (BooleanControl)dataLine.getControl(javax.sound.sampled.BooleanControl.Type.MUTE);
            }
            catch(Exception e)
            {
                Log.warning("JavaSound: No gain control");
            }
            try
            {
                rc = (FloatControl)dataLine.getControl(javax.sound.sampled.FloatControl.Type.SAMPLE_RATE);
            }
            catch(Exception e)
            {
                Log.warning("JavaSound: No rate control");
            }
            flag2 = true;
        }
        return flag2;
    }

    public void dispose()
    {
        dataLine.close();
    }

    public void finalize()
        throws Throwable
    {
        super.finalize();
        dispose();
    }

    public void pause()
    {
        if(dataLine != null)
            dataLine.stop();
        paused = true;
    }

    public void resume()
    {
        if(dataLine != null)
            dataLine.start();
        paused = false;
    }

    public void drain()
    {
        dataLine.drain();
    }

    public void flush()
    {
        dataLine.flush();
    }

    public AudioFormat getFormat()
    {
        return format;
    }

    public long getMediaNanoseconds()
    {
        if(dataLine == null || format == null)
            return 0L;
        long pos = dataLine.getFramePosition();
        if(pos < lastPos)
        {
            totalCount += lastPos - originPos;
            originPos = pos;
        }
        lastPos = pos;
        return (long)(((double)(((totalCount + pos) - originPos) * 1000L) / format.getSampleRate()) * 1000000D);
    }

    public void setGain(double g)
    {
        if(gc != null)
            gc.setValue((float)g);
    }

    public double getGain()
    {
        return gc == null ? 0.0D : gc.getValue();
    }

    public void setMute(boolean m)
    {
        if(mc != null)
            mc.setValue(m);
    }

    public boolean getMute()
    {
        return mc == null ? false : mc.getValue();
    }

    public float setRate(float r)
    {
        if(rc == null)
            return 1.0F;
        float rate = (float)((double)r * format.getSampleRate());
        if(rate > rc.getMaximum() || rate < rc.getMinimum())
        {
            return getRate();
        } else
        {
            rc.setValue(rate);
            return r;
        }
    }

    public float getRate()
    {
        if(rc == null)
            return 1.0F;
        else
            return (float)((double)rc.getValue() / format.getSampleRate());
    }

    public int bufferAvailable()
    {
        return dataLine.available();
    }

    public int write(byte data[], int off, int len)
    {
        return dataLine.write(data, off, len);
    }

    public static boolean isOpen()
    {
        Mixer mixer = AudioSystem.getMixer(null);
        Line lines[] = mixer.getSourceLines();
        return lines != null && lines.length > 0;
    }

    public static AudioFormat convertFormat(javax.sound.sampled.AudioFormat fmt)
    {
        javax.sound.sampled.AudioFormat.Encoding type = fmt.getEncoding();
        String encoding;
        if(type == javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED || type == javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED)
            encoding = "LINEAR";
        else
        if(type == javax.sound.sampled.AudioFormat.Encoding.ALAW)
            encoding = "alaw";
        else
        if(type == javax.sound.sampled.AudioFormat.Encoding.ULAW)
            encoding = "ULAW";
        else
            encoding = null;
        return new AudioFormat(encoding, fmt.getSampleRate(), fmt.getSampleSizeInBits(), fmt.getChannels(), fmt.isBigEndian() ? 1 : 0, type != javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED ? 0 : 1);
    }

    public static javax.sound.sampled.AudioFormat convertFormat(AudioFormat fmt)
    {
        return new javax.sound.sampled.AudioFormat(fmt.getSampleRate() != -1D ? (float)fmt.getSampleRate() : 8000F, fmt.getSampleSizeInBits() != -1 ? fmt.getSampleSizeInBits() : 16, fmt.getChannels() != -1 ? fmt.getChannels() : 1, fmt.getSigned() == 1, fmt.getEndian() == 1);
    }

    static Mixer mixer = null;
    static Object initSync = new Object();
    protected SourceDataLine dataLine;
    protected FloatControl gc;
    protected FloatControl rc;
    protected BooleanControl mc;
    protected boolean paused;
    protected int bufSize;
    protected AudioFormat format;
    long lastPos;
    long originPos;
    long totalCount;

}
