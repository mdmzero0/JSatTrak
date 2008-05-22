// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaSoundSourceStream.java

package com.sun.media.protocol.javasound;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.*;
import com.sun.media.protocol.BasicSourceStream;
import com.sun.media.renderer.audio.device.JavaSoundOutput;
import com.sun.media.ui.AudioFormatChooser;
import com.sun.media.util.*;
import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.*;
import javax.media.control.BufferControl;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.*;
import javax.sound.sampled.*;

// Referenced classes of package com.sun.media.protocol.javasound:
//            PushThread, jdk12CreateThreadAction, DataSource

public class JavaSoundSourceStream extends BasicSourceStream
    implements PushBufferStream
{
    class BC
        implements BufferControl, Owned
    {

        public long getBufferLength()
        {
            return bufLenReq;
        }

        public long setBufferLength(long time)
        {
            if(time < (long)JavaSoundSourceStream.DefaultMinBufferSize)
                bufLenReq = JavaSoundSourceStream.DefaultMinBufferSize;
            else
            if(time > (long)JavaSoundSourceStream.DefaultMaxBufferSize)
                bufLenReq = JavaSoundSourceStream.DefaultMaxBufferSize;
            else
                bufLenReq = time;
            Log.comment("Capture buffer length set: " + bufLenReq);
            reconnect = true;
            return bufLenReq;
        }

        public long getMinimumThreshold()
        {
            return 0L;
        }

        public long setMinimumThreshold(long time)
        {
            return 0L;
        }

        public void setEnabledThreshold(boolean flag)
        {
        }

        public boolean getEnabledThreshold()
        {
            return false;
        }

        public Component getControlComponent()
        {
            return null;
        }

        public Object getOwner()
        {
            return dsource;
        }

        JavaSoundSourceStream jsss;

        BC(JavaSoundSourceStream js)
        {
            jsss = js;
        }
    }

    class FC
        implements FormatControl, Owned
    {

        public Object getOwner()
        {
            return dsource;
        }

        public Format getFormat()
        {
            return format;
        }

        public Format setFormat(Format fmt)
        {
            return jsss.setFormat(fmt);
        }

        public Format[] getSupportedFormats()
        {
            return JavaSoundSourceStream.supported;
        }

        public boolean isEnabled()
        {
            return true;
        }

        public void setEnabled(boolean flag)
        {
        }

        public Component getControlComponent()
        {
            if(afc == null)
            {
                afc = new AudioFormatChooser(JavaSoundSourceStream.supported, format);
                afc.setName("JavaSound");
                if(started || dataLine == null || JavaSoundOutput.isOpen())
                    afc.setEnabled(false);
            }
            return afc;
        }

        JavaSoundSourceStream jsss;

        public FC(JavaSoundSourceStream jsss)
        {
            this.jsss = jsss;
        }
    }


    public JavaSoundSourceStream(DataSource ds)
    {
        super(new ContentDescriptor("raw"), -1L);
        dataLine = null;
        reconnect = false;
        started = false;
        cb = new CircularBuffer(1);
        pushThread = null;
        mSecurity = new Method[1];
        clSecurity = new Class[1];
        argsSecurity = new Object[1][0];
        bufLenReq = 125L;
        dsource = ds;
        bc = new BC(this);
        super.controls = new Control[2];
        super.controls[0] = new FC(this);
        super.controls[1] = bc;
    }

    public static Format parseLocator(MediaLocator ml)
    {
        String rateStr = null;
        String bitsStr = null;
        String channelsStr = null;
        String endianStr = null;
        String signedStr = null;
        String remainder = ml.getRemainder();
        if(remainder != null && remainder.length() > 0)
        {
            for(; remainder.length() > 1 && remainder.charAt(0) == '/'; remainder = remainder.substring(1));
            int off = remainder.indexOf('/');
            if(off == -1)
            {
                if(!remainder.equals(""))
                    rateStr = remainder;
            } else
            {
                rateStr = remainder.substring(0, off);
                remainder = remainder.substring(off + 1);
                off = remainder.indexOf('/');
                if(off == -1)
                {
                    if(!remainder.equals(""))
                        bitsStr = remainder;
                } else
                {
                    bitsStr = remainder.substring(0, off);
                    remainder = remainder.substring(off + 1);
                    off = remainder.indexOf('/');
                    if(off == -1)
                    {
                        if(!remainder.equals(""))
                            channelsStr = remainder;
                    } else
                    {
                        channelsStr = remainder.substring(0, off);
                        remainder = remainder.substring(off + 1);
                        off = remainder.indexOf('/');
                        if(off == -1)
                        {
                            if(!remainder.equals(""))
                                endianStr = remainder;
                        } else
                        {
                            endianStr = remainder.substring(0, off);
                            if(!remainder.equals(""))
                                signedStr = remainder.substring(off + 1);
                        }
                    }
                }
            }
        }
        int rate = DefRate;
        if(rateStr != null)
        {
            try
            {
                Integer integer = Integer.valueOf(rateStr);
                if(integer != null)
                    rate = integer.intValue();
            }
            catch(Throwable t) { }
            if(rate <= 0 || rate > 0x17700)
            {
                Log.warning("JavaSound capture: unsupported sample rate: " + rate);
                rate = DefRate;
                Log.warning("        defaults to: " + rate);
            }
        }
        int bits = DefBits;
        if(bitsStr != null)
        {
            try
            {
                Integer integer = Integer.valueOf(bitsStr);
                if(integer != null)
                    bits = integer.intValue();
            }
            catch(Throwable t) { }
            if(bits != 8 && bits != 16)
            {
                Log.warning("JavaSound capture: unsupported sample size: " + bits);
                bits = DefBits;
                Log.warning("        defaults to: " + bits);
            }
        }
        int channels = DefChannels;
        if(channelsStr != null)
        {
            try
            {
                Integer integer = Integer.valueOf(channelsStr);
                if(integer != null)
                    channels = integer.intValue();
            }
            catch(Throwable t) { }
            if(channels != 1 && channels != 2)
            {
                Log.warning("JavaSound capture: unsupported # of channels: " + channels);
                channels = DefChannels;
                Log.warning("        defaults to: " + channels);
            }
        }
        int endian = DefEndian;
        if(endianStr != null)
            if(endianStr.equalsIgnoreCase("big"))
                endian = 1;
            else
            if(endianStr.equalsIgnoreCase("little"))
            {
                endian = 0;
            } else
            {
                Log.warning("JavaSound capture: unsupported endianess: " + endianStr);
                Log.warning("        defaults to: big endian");
            }
        int signed = DefSigned;
        if(signedStr != null)
            if(signedStr.equalsIgnoreCase("signed"))
                signed = 1;
            else
            if(signedStr.equalsIgnoreCase("unsigned"))
            {
                signed = 0;
            } else
            {
                Log.warning("JavaSound capture: unsupported signedness: " + signedStr);
                Log.warning("        defaults to: signed");
            }
        AudioFormat fmt = new AudioFormat("LINEAR", rate, bits, channels, endian, signed);
        return fmt;
    }

    public Format setFormat(Format fmt)
    {
        if(started)
        {
            Log.warning("Cannot change audio capture format after started.");
            return format;
        }
        if(fmt == null)
            return format;
        Format f = null;
        for(int i = 0; i < supported.length; i++)
            if(fmt.matches(supported[i]) && (f = fmt.intersects(supported[i])) != null)
                break;

        if(f == null)
            return format;
        try
        {
            if(devFormat != null)
            {
                if(!devFormat.matches(f) && !JavaSoundOutput.isOpen())
                {
                    format = (AudioFormat)f;
                    disconnect();
                    connect();
                }
            } else
            {
                format = (AudioFormat)f;
                connect();
            }
        }
        catch(IOException e)
        {
            return null;
        }
        if(afc != null)
            afc.setCurrentFormat(format);
        return format;
    }

    public boolean isConnected()
    {
        return devFormat != null;
    }

    public void connect()
        throws IOException
    {
        if(isConnected())
            return;
        if(JavaSoundOutput.isOpen())
        {
            Log.warning("JavaSound is already opened for rendering.  Will capture at the default format.");
            format = null;
        }
        openDev();
        if(pushThread == null)
        {
            if(jmfSecurity != null)
            {
                String permission = null;
                try
                {
                    if(jmfSecurity.getName().startsWith("jmf-security"))
                    {
                        permission = "thread";
                        jmfSecurity.requestPermission(mSecurity, clSecurity, argsSecurity, 16);
                        mSecurity[0].invoke(clSecurity[0], argsSecurity[0]);
                        permission = "thread group";
                        jmfSecurity.requestPermission(mSecurity, clSecurity, argsSecurity, 32);
                        mSecurity[0].invoke(clSecurity[0], argsSecurity[0]);
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
                    pushThread = (PushThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                        cons.newInstance(new Object[] {
                            com.sun.media.protocol.javasound.PushThread.class
                        })
                    });
                }
                catch(Exception e) { }
            else
                pushThread = new PushThread();
            pushThread.setSourceStream(this);
        }
        if(reconnect)
            Log.comment("Capture buffer size: " + bufSize);
        devFormat = format;
        reconnect = false;
    }

    void openDev()
        throws IOException
    {
        javax.sound.sampled.AudioFormat afmt = null;
        javax.sound.sampled.DataLine.Info info;
        if(format != null)
        {
            afmt = JavaSoundOutput.convertFormat(format);
            int chnls = format.getChannels() != -1 ? format.getChannels() : 1;
            int size = format.getSampleSizeInBits() != -1 ? format.getSampleSizeInBits() : 16;
            int frameSize = (size * chnls) / 8;
            if(frameSize == 0)
                frameSize = 1;
            bufSize = (int)((format.getSampleRate() * (double)frameSize * (double)bc.getBufferLength()) / 1000D);
            info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, afmt, bufSize);
        } else
        {
            info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, null, -1);
        }
        if(!AudioSystem.isLineSupported(info))
        {
            Log.error("Audio not supported: " + info + "\n");
            throw new IOException("Cannot open audio device for input.");
        }
        try
        {
            dataLine = (TargetDataLine)AudioSystem.getLine(info);
            if(format != null)
            {
                dataLine.open(afmt, bufSize);
            } else
            {
                dataLine.open();
                format = JavaSoundOutput.convertFormat(dataLine.getFormat());
            }
            bufSize = dataLine.getBufferSize();
        }
        catch(Exception e)
        {
            Log.error("Cannot open audio device for input: " + e);
            throw new IOException(e.getMessage());
        }
    }

    public void disconnect()
    {
        if(dataLine == null)
            return;
        dataLine.stop();
        dataLine.close();
        dataLine = null;
        devFormat = null;
        if(pushThread != null)
        {
            pushThread.kill();
            pushThread = null;
        }
    }

    public void start()
        throws IOException
    {
        if(dataLine == null)
            throw new IOException("A JavaSound input channel cannot be opened.");
        if(started)
            return;
        if(afc != null)
        {
            Format f;
            if((f = afc.getFormat()) != null && !f.matches(format))
                if(setFormat(f) != null);
            afc.setEnabled(false);
        }
        if(reconnect)
            disconnect();
        if(!isConnected())
            connect();
        synchronized(cb)
        {
            for(; cb.canRead(); cb.readReport())
                cb.read();

            cb.notifyAll();
        }
        pushThread.start();
        dataLine.flush();
        dataLine.start();
        started = true;
    }

    public void stop()
        throws IOException
    {
        if(!started)
            return;
        pushThread.pause();
        if(dataLine != null)
            dataLine.stop();
        started = false;
        if(afc != null && !JavaSoundOutput.isOpen())
            afc.setEnabled(true);
    }

    public Format getFormat()
    {
        return format;
    }

    public Object[] getControls()
    {
        return super.controls;
    }

    public static Format[] getSupportedFormats()
    {
        return supported;
    }

    public static CaptureDeviceInfo[] listCaptureDeviceInfo()
    {
        return deviceList;
    }

    public void setTransferHandler(BufferTransferHandler th)
    {
        transferHandler = th;
    }

    public boolean willReadBlock()
    {
        return !started;
    }

    public void read(Buffer in)
    {
        Buffer buffer;
        synchronized(cb)
        {
            while(!cb.canRead()) 
                try
                {
                    cb.wait();
                }
                catch(Exception e) { }
            buffer = cb.read();
        }
        Object data = in.getData();
        in.copy(buffer);
        buffer.setData(data);
        synchronized(cb)
        {
            cb.readReport();
            cb.notify();
        }
    }

    DataSource dsource;
    TargetDataLine dataLine;
    AudioFormat format;
    AudioFormat devFormat;
    boolean reconnect;
    int bufSize;
    BufferTransferHandler transferHandler;
    boolean started;
    AudioFormatChooser afc;
    BufferControl bc;
    CircularBuffer cb;
    PushThread pushThread;
    static int DefRate = 44100;
    static int DefBits = 16;
    static int DefChannels = 2;
    static int DefSigned;
    static int DefEndian;
    static int OtherEndian = Arch.isBigEndian() ? 1 : 0;
    static Format supported[];
    protected static CaptureDeviceInfo deviceList[];
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method mSecurity[];
    private Class clSecurity[];
    private Object argsSecurity[][];
    static int DefaultMinBufferSize = 16;
    static int DefaultMaxBufferSize = 4000;
    long bufLenReq;

    static 
    {
        DefSigned = 1;
        DefEndian = Arch.isBigEndian() ? 1 : 0;
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
        supported = (new Format[] {
            new AudioFormat("LINEAR", 44100D, 16, 2, DefEndian, DefSigned), new AudioFormat("LINEAR", 44100D, 16, 1, DefEndian, DefSigned), new AudioFormat("LINEAR", 22050D, 16, 2, DefEndian, DefSigned), new AudioFormat("LINEAR", 22050D, 16, 1, DefEndian, DefSigned), new AudioFormat("LINEAR", 11025D, 16, 2, DefEndian, DefSigned), new AudioFormat("LINEAR", 11025D, 16, 1, DefEndian, DefSigned), new AudioFormat("LINEAR", 8000D, 16, 2, DefEndian, DefSigned), new AudioFormat("LINEAR", 8000D, 16, 1, DefEndian, DefSigned)
        });
        deviceList = (new CaptureDeviceInfo[] {
            new CaptureDeviceInfo("JavaSound audio capture", new MediaLocator("javasound://44100"), supported)
        });
    }
}
