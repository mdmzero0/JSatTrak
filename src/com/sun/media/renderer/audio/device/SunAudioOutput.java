// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SunAudioOutput.java

package com.sun.media.renderer.audio.device;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.renderer.audio.SunAudioRenderer;
import com.sun.media.util.LoopThread;
import com.sun.media.util.jdk12;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.Format;
import javax.media.format.AudioFormat;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

// Referenced classes of package com.sun.media.renderer.audio.device:
//            SunAudioPlayThread, AudioOutput, jdk12CreateThreadAction

public class SunAudioOutput extends InputStream
    implements AudioOutput
{

    public SunAudioOutput()
    {
        paused = false;
        started = false;
        flushing = false;
        startAfterWrite = false;
        SUN_MAGIC = 0x2e736e64;
        HDR_SIZE = 24;
        FILE_LENGTH = 0;
        SAMPLE_RATE = 8000;
        ENCODING = 1;
        CHANNELS = 1;
        in = 0;
        out = 0;
        eom = false;
        samplesPlayed = 0;
        isMuted = false;
        gain = 0.0D;
        internalDelayUpdate = false;
        timeUpdatingThread = null;
        sunAudioInitialCount = 0;
        sunAudioFinalCount = 0;
        silenceCount = 0;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
    }

    public boolean initialize(AudioFormat format, int length)
    {
        this.format = format;
        bufLength = 12000;
        buffer = new byte[bufLength];
        silence = new byte[bufLength];
        for(int i = 0; i < bufLength; i++)
            silence[i] = 127;

        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "thread";
                    jmfSecurity.requestPermission(m, cl, args, 16);
                    m[0].invoke(cl[0], args[0]);
                    permission = "thread group";
                    jmfSecurity.requestPermission(m, cl, args, 32);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.THREAD);
                    PolicyEngine.assertPermission(PermissionID.THREAD);
                }
            }
            catch(Throwable e)
            {
                securityPrivelege = false;
            }
        }
        if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            try
            {
                Constructor cons = jdk12CreateThreadAction.cons;
                timeUpdatingThread = (SunAudioPlayThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.renderer.audio.device.SunAudioPlayThread.class
                    })
                });
            }
            catch(Exception e) { }
        else
            timeUpdatingThread = new SunAudioPlayThread();
        timeUpdatingThread.setStream(this);
        setPaddingLength(800);
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        DataOutputStream tempData = new DataOutputStream(tempOut);
        try
        {
            tempData.writeInt(SUN_MAGIC);
            tempData.writeInt(HDR_SIZE);
            tempData.writeInt(FILE_LENGTH);
            tempData.writeInt(ENCODING);
            tempData.writeInt(SAMPLE_RATE);
            tempData.writeInt(CHANNELS);
        }
        catch(Exception e) { }
        byte buf[] = tempOut.toByteArray();
        write(buf, 0, buf.length);
        String encoding = format.getEncoding();
        int sampleRate = (int)format.getSampleRate();
        if(format.getChannels() != 1 || sampleRate != 8000 || !encoding.equals("ULAW"))
        {
            System.out.println("AudioPlay:Unsupported Audio Format");
            return false;
        }
        try
        {
            audioStream = new AudioStream(this);
        }
        catch(Exception e)
        {
            System.err.println("Exception: " + e);
            audioStream = null;
            return false;
        }
        return true;
    }

    public void finalize()
        throws Throwable
    {
        super.finalize();
        dispose();
    }

    public void pause()
    {
        if(audioStream != null)
        {
            timeUpdatingThread.pause();
            AudioPlayer.player.stop(audioStream);
        }
        paused = true;
    }

    public synchronized void resume()
    {
        if(audioStream != null && (!started || paused))
        {
            started = true;
            AudioPlayer.player.start(audioStream);
            timeUpdatingThread.start();
        }
        paused = false;
    }

    public synchronized void dispose()
    {
        if(audioStream != null)
        {
            timeUpdatingThread.kill();
            AudioPlayer.player.stop(audioStream);
        }
        buffer = null;
    }

    public void drain()
    {
        synchronized(this)
        {
            int len;
            for(int remain = endOfMediaPaddingLength; remain > 0; remain -= len)
                len = write(silence, 0, remain);

            while(in != out && !paused) 
                try
                {
                    wait();
                }
                catch(InterruptedException e) { }
            if(SunAudioRenderer.runningOnMac)
                try
                {
                    Thread.sleep(SunAudioRenderer.DEVICE_LATENCY / 0xf4240L);
                }
                catch(InterruptedException e) { }
        }
    }

    public synchronized void flush()
    {
        in = 0;
        out = 0;
        sunAudioInitialCount = sunAudioFinalCount = samplesPlayed;
        flushing = true;
        notifyAll();
    }

    public long getMediaNanoseconds()
    {
        return audioStream != null ? (long)samplesPlayed * 0x1e848L : 0L;
    }

    public void setMute(boolean m)
    {
        isMuted = m;
    }

    public boolean getMute()
    {
        return isMuted;
    }

    public void setGain(double d)
    {
    }

    public double getGain()
    {
        return 0.0D;
    }

    public float setRate(float r)
    {
        return 1.0F;
    }

    public float getRate()
    {
        return 1.0F;
    }

    public int dataAvailable()
    {
        if(in == out)
            return 0;
        if(in > out)
            return in - out;
        else
            return bufLength - (out - in);
    }

    public int bufferAvailable()
    {
        if(SunAudioRenderer.runningOnMac)
            return 0;
        else
            return bufLength - dataAvailable() - 1;
    }

    public synchronized int read()
    {
        while(in == out) 
        {
            if(eom)
            {
                eom = false;
                return EOM;
            }
            try
            {
                wait();
            }
            catch(InterruptedException e) { }
        }
        int ret = buffer[out++] & 0xff;
        if(out >= buffer.length)
            out = 0;
        return ret;
    }

    public synchronized int read(byte b[], int off, int len)
    {
        int inputLength = len;
        if(len <= 0)
            return -1;
        if(len > 4 && !internalDelayUpdate)
        {
            internalDelayUpdate = true;
            timeUpdatingThread.setInternalDelay(len);
        }
        if(dataAvailable() == 0)
        {
            System.arraycopy(silence, 0, b, off, inputLength);
            silenceCount += inputLength;
            return inputLength;
        }
        int c = read();
        if(c < 0)
            return -1;
        b[off] = (byte)c;
        int rlen = 1;
        if(in != out)
        {
            len--;
            if(out < in)
            {
                int avail = in - out;
                if(avail > len)
                    avail = len;
                System.arraycopy(buffer, out, b, off + 1, avail);
                out += avail;
                rlen += avail;
            } else
            if(out > in)
            {
                int avail = bufLength - out;
                if(avail >= len)
                {
                    avail = len;
                    System.arraycopy(buffer, out, b, off + 1, avail);
                    out += avail;
                    if(out >= bufLength)
                        out = 0;
                    rlen += avail;
                } else
                {
                    System.arraycopy(buffer, out, b, off + 1, avail);
                    out += avail;
                    if(out >= bufLength)
                        out = 0;
                    int copied = avail;
                    rlen += avail;
                    int need = len - avail;
                    avail = in - out;
                    int size;
                    if(need <= avail)
                        size = need;
                    else
                        size = avail;
                    System.arraycopy(buffer, 0, b, off + 1 + copied, size);
                    out += size;
                    rlen += size;
                }
            }
        }
        if(isMuted)
            System.arraycopy(silence, 0, b, off, inputLength);
        else
        if(rlen < inputLength)
        {
            System.arraycopy(silence, 0, b, off + rlen, inputLength - rlen);
            silenceCount += inputLength - rlen;
        } else
        if(silenceCount > 0)
            if(silenceCount > rlen)
            {
                silenceCount -= rlen;
                rlen = 0;
            } else
            {
                rlen -= silenceCount;
                silenceCount = 0;
            }
        timeUpdatingThread.resetSampleCountTime();
        sunAudioInitialCount = sunAudioFinalCount;
        sunAudioFinalCount += rlen;
        notifyAll();
        return inputLength;
    }

    public synchronized int write(byte data[], int off, int len)
    {
        flushing = false;
        if(len <= 0)
            return 0;
        while((in + 1) % buffer.length == out) 
            try
            {
                wait();
            }
            catch(InterruptedException e) { }
        if(flushing)
            return 0;
        int wlen = 0;
        if(in < out)
        {
            int canWrite = out - in - 1;
            int actualWrite = canWrite >= len ? len : canWrite;
            System.arraycopy(data, off, buffer, in, actualWrite);
            in += actualWrite;
            wlen += actualWrite;
        } else
        {
            int length1;
            if(out == 0)
                length1 = bufLength - in - 1;
            else
                length1 = bufLength - in;
            if(length1 >= len)
            {
                int actualWrite = len;
                System.arraycopy(data, off, buffer, in, actualWrite);
                in += actualWrite;
                if(in >= bufLength)
                    in = 0;
                wlen += actualWrite;
            } else
            {
                int actualWrite = length1;
                System.arraycopy(data, off, buffer, in, actualWrite);
                in += actualWrite;
                if(in >= bufLength)
                    in = 0;
                wlen += actualWrite;
                len -= actualWrite;
                int actualWrite1 = actualWrite;
                if(out > 0)
                {
                    int canWrite = out - in - 1;
                    actualWrite = canWrite >= len ? len : canWrite;
                    System.arraycopy(data, off + actualWrite1, buffer, 0, actualWrite);
                    wlen += actualWrite;
                    in = actualWrite;
                }
            }
        }
        notifyAll();
        return wlen;
    }

    protected void setPaddingLength(int paddingLength)
    {
        endOfMediaPaddingLength = paddingLength;
        if(endOfMediaPaddingLength > silence.length)
            endOfMediaPaddingLength = silence.length;
    }

    protected AudioStream audioStream;
    protected int bufLength;
    protected byte buffer[];
    protected static int EOM = -1;
    protected boolean paused;
    protected boolean started;
    protected boolean flushing;
    private boolean startAfterWrite;
    protected AudioFormat format;
    private int SUN_MAGIC;
    private int HDR_SIZE;
    private int FILE_LENGTH;
    private int SAMPLE_RATE;
    private int ENCODING;
    private int CHANNELS;
    int in;
    int out;
    boolean eom;
    int samplesPlayed;
    private boolean isMuted;
    private double gain;
    private byte silence[];
    private static final int END_OF_MEDIA_PADDING_LENGTH = 800;
    private int endOfMediaPaddingLength;
    private byte conversionBuffer[];
    static final int SLEEP_TIME = 50;
    protected boolean internalDelayUpdate;
    private SunAudioPlayThread timeUpdatingThread;
    protected int sunAudioInitialCount;
    protected int sunAudioFinalCount;
    protected int silenceCount;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
    }
}
