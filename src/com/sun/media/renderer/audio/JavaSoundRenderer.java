// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaSoundRenderer.java

package com.sun.media.renderer.audio;

import com.sun.media.*;
import com.sun.media.controls.GainControlAdapter;
import com.sun.media.renderer.audio.device.AudioOutput;
import com.sun.media.renderer.audio.device.JavaSoundOutput;
import java.awt.*;
import java.io.PrintStream;
import javax.media.*;
import javax.media.format.AudioFormat;

// Referenced classes of package com.sun.media.renderer.audio:
//            AudioRenderer

public class JavaSoundRenderer extends AudioRenderer
    implements ExclusiveUse
{
    class PeakVolumeMeter
        implements Control, Owned
    {

        public Object getOwner()
        {
            return renderer;
        }

        public Component getControlComponent()
        {
            if(component == null)
            {
                canvas = new Canvas() {

                    public Dimension getPreferredSize()
                    {
                        return new Dimension(102, JavaSoundRenderer.METERHEIGHT);
                    }

                }
;
                cbEnabled = new Checkbox("Peak Volume Meter", false);
                component = new Panel();
                component.add(cbEnabled);
                component.add(canvas);
                canvas.setBackground(Color.black);
            }
            return component;
        }

        public void processData(Buffer buf)
        {
            AudioFormat af = (AudioFormat)buf.getFormat();
            int index = 0;
            int peak = 0;
            int inc = 2;
            if(component == null)
                return;
            if(!cbEnabled.getState())
                return;
            byte data[] = (byte[])buf.getData();
            if(buf.isDiscard())
                return;
            if(buf.getLength() <= 0)
                return;
            if(af.getEndian() == 0)
                index = 1;
            boolean signed = af.getSigned() == 1;
            if(af.getSampleSizeInBits() == 8)
                inc = 1;
            if(signed)
            {
                for(int i = index; i < buf.getLength(); i += inc * 5)
                {
                    int d = data[i];
                    if(d < 0)
                        d = -d;
                    if(d > peak)
                        peak = d;
                }

                peak = (peak * 100) / 127;
            } else
            {
                for(int i = index; i < buf.getLength(); i += inc * 5)
                    if((data[i] & 0xff) > peak)
                        peak = data[i] & 0xff;

                peak = (peak * 100) / 255;
            }
            averagePeak = (peak + averagePeak) / 2;
            long currentTime = System.currentTimeMillis();
            if(currentTime > lastResetTime + 100L)
            {
                lastResetTime = currentTime;
                updatePeak(averagePeak);
                averagePeak = peak;
            }
        }

        private void updatePeak(int newPeak)
        {
            if(canvas == null)
                return;
            if(cGraphics == null)
                cGraphics = canvas.getGraphics();
            if(cGraphics == null)
                return;
            if(newPeak > 99)
                newPeak = 99;
            cGraphics.setColor(Color.green);
            if(newPeak < 80)
            {
                cGraphics.drawLine(1, 1, newPeak + 1, 1);
                cGraphics.drawLine(1, 2, newPeak + 1, 2);
            } else
            {
                cGraphics.drawLine(1, 1, 81, 1);
                cGraphics.drawLine(1, 2, 81, 2);
                cGraphics.setColor(Color.yellow);
                if(newPeak < 90)
                {
                    cGraphics.drawLine(81, 1, newPeak + 1, 1);
                    cGraphics.drawLine(81, 2, newPeak + 1, 2);
                } else
                {
                    cGraphics.drawLine(81, 1, 91, 1);
                    cGraphics.drawLine(81, 2, 91, 2);
                    cGraphics.setColor(Color.red);
                    cGraphics.drawLine(91, 1, newPeak + 1, 1);
                    cGraphics.drawLine(91, 2, newPeak + 1, 2);
                }
            }
            cGraphics.setColor(Color.black);
            cGraphics.drawLine(newPeak + 2, 1, 102, 1);
            cGraphics.drawLine(newPeak + 2, 2, 102, 2);
            lastPeak = newPeak;
        }

        int averagePeak;
        int lastPeak;
        Panel component;
        Checkbox cbEnabled;
        Canvas canvas;
        AudioRenderer renderer;
        long lastResetTime;
        Graphics cGraphics;

        public PeakVolumeMeter(AudioRenderer r)
        {
            averagePeak = 0;
            lastPeak = 0;
            component = null;
            cbEnabled = null;
            canvas = null;
            cGraphics = null;
            renderer = r;
            lastResetTime = System.currentTimeMillis();
        }
    }

    class GCA extends GainControlAdapter
    {

        public void setMute(boolean mute)
        {
            if(renderer != null && renderer.device != null)
                renderer.device.setMute(mute);
            super.setMute(mute);
        }

        public float setLevel(float g)
        {
            float level = super.setLevel(g);
            if(renderer != null && renderer.device != null)
                renderer.device.setGain(getDB());
            return level;
        }

        AudioRenderer renderer;

        protected GCA(AudioRenderer r)
        {
            super(false);
            renderer = r;
        }
    }


    public JavaSoundRenderer()
    {
        decodeBuffer = null;
        if(!available)
        {
            throw new UnsatisfiedLinkError("No JavaSound library");
        } else
        {
            ulawFormat = new AudioFormat("ULAW");
            linearFormat = new AudioFormat("LINEAR");
            super.supportedFormats = new Format[2];
            super.supportedFormats[0] = linearFormat;
            super.supportedFormats[1] = ulawFormat;
            super.gainControl = new GCA(this);
            super.peakVolumeMeter = new PeakVolumeMeter(this);
            return;
        }
    }

    public String getName()
    {
        return NAME;
    }

    public void open()
        throws ResourceUnavailableException
    {
        if(super.device == null && super.inputFormat != null)
        {
            if(!initDevice(super.inputFormat))
                throw new ResourceUnavailableException("Cannot intialize audio device for playback");
            super.device.pause();
        }
    }

    public boolean isExclusive()
    {
        return false;
    }

    protected boolean initDevice(AudioFormat in)
    {
        Format newInput = in;
        if(ulawDecoder != null)
        {
            ulawDecoder.close();
            ulawDecoder = null;
        }
        Format outs[] = new Format[1];
        if(ulawFormat.matches(in))
        {
            ulawDecoder = SimpleGraphBuilder.findCodec(in, linearFormat, null, outs);
            if(ulawDecoder != null)
                ulawOutputFormat = newInput = outs[0];
            else
                return false;
        }
        super.devFormat = in;
        return super.initDevice((AudioFormat)newInput);
    }

    protected AudioOutput createDevice(AudioFormat format)
    {
        return new JavaSoundOutput();
    }

    public int processData(Buffer buffer)
    {
        if(!checkInput(buffer))
            return 1;
        if(ulawDecoder == null)
        {
            try
            {
                ((PeakVolumeMeter)super.peakVolumeMeter).processData(buffer);
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }
            return super.doProcessData(buffer);
        }
        if(decodeBuffer == null)
        {
            decodeBuffer = new Buffer();
            decodeBuffer.setFormat(ulawOutputFormat);
        }
        decodeBuffer.setLength(0);
        decodeBuffer.setOffset(0);
        decodeBuffer.setFlags(buffer.getFlags());
        decodeBuffer.setTimeStamp(buffer.getTimeStamp());
        decodeBuffer.setSequenceNumber(buffer.getSequenceNumber());
        int rc = ulawDecoder.process(buffer, decodeBuffer);
        if(rc == 0)
        {
            try
            {
                ((PeakVolumeMeter)super.peakVolumeMeter).processData(decodeBuffer);
            }
            catch(Throwable t)
            {
                System.err.println(t);
            }
            return super.doProcessData(decodeBuffer);
        } else
        {
            return 1;
        }
    }

    public Object[] getControls()
    {
        Control c[] = {
            super.gainControl, super.bufferControl, super.peakVolumeMeter
        };
        return c;
    }

    static String NAME = "JavaSound Renderer";
    Codec ulawDecoder;
    Format ulawOutputFormat;
    Format ulawFormat;
    Format linearFormat;
    static int METERHEIGHT = 4;
    static boolean available = false;
    Buffer decodeBuffer;

    static 
    {
        String javaVersion = null;
        String subver = null;
        try
        {
            javaVersion = System.getProperty("java.version");
            int len;
            if(javaVersion.length() < 3)
                len = javaVersion.length();
            else
                len = 3;
            subver = javaVersion.substring(0, len);
        }
        catch(Throwable t)
        {
            javaVersion = null;
            subver = null;
        }
        if(subver == null || subver.compareTo("1.3") < 0)
            try
            {
                JMFSecurityManager.loadLibrary("jmutil");
                JMFSecurityManager.loadLibrary("jsound");
                available = true;
            }
            catch(Throwable t) { }
        else
            available = true;
    }
}
