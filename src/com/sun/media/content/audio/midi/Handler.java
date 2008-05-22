// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Handler.java

package com.sun.media.content.audio.midi;

import com.sun.media.*;
import com.sun.media.controls.GainControlAdapter;
import com.sun.media.parser.BasicPullParser;
import com.sun.media.parser.BasicTrack;
import java.io.*;
import javax.media.*;
import javax.media.protocol.*;
import javax.sound.midi.*;

public class Handler extends BasicPlayer
{
    class MidiFileInputStream extends InputStream
    {

        public void rewind()
        {
            index = 0;
            markpos = 0;
        }

        public int read()
            throws IOException
        {
            if(index >= length)
                return -1;
            else
                return data[index++];
        }

        public int available()
            throws IOException
        {
            return length - index;
        }

        public int read(byte b[])
            throws IOException
        {
            return read(b, 0, b.length);
        }

        public int read(byte b[], int off, int len)
            throws IOException
        {
            if(len > available())
                len = available();
            if(len == 0)
            {
                return -1;
            } else
            {
                System.arraycopy(data, index, b, off, len);
                index += len;
                return len;
            }
        }

        private int length;
        private int index;
        private byte data[];
        private int markpos;

        MidiFileInputStream(byte data[], int length)
        {
            index = 0;
            markpos = 0;
            this.data = data;
            this.length = length;
        }
    }

    class MidiParser extends BasicPullParser
    {

        public ContentDescriptor[] getSupportedInputContentDescriptors()
        {
            return null;
        }

        public PullSourceStream getStream()
        {
            PullSourceStream stream = (PullSourceStream)super.streams[0];
            return stream;
        }

        public Track[] getTracks()
            throws IOException, BadHeaderException
        {
            return null;
        }

        public Time setPosition(Time where, int rounding)
        {
            return null;
        }

        public Time getMediaTime()
        {
            return null;
        }

        public Time getDuration()
        {
            return null;
        }

        public String getName()
        {
            return "Parser for MIDI file format";
        }

        MidiParser()
        {
        }
    }

    class MidiController extends BasicController
        implements MetaEventListener
    {
        class GCA extends GainControlAdapter
        {

            public void setMute(boolean mute)
            {
                super.setMute(mute);
                muteChange(mute);
            }

            public float setLevel(float g)
            {
                float level = super.setLevel(g);
                gainChange(g);
                return level;
            }

            GCA()
            {
                super(1.0F);
            }
        }


        protected boolean isConfigurable()
        {
            return false;
        }

        public void setSource(DataSource source)
            throws IOException, IncompatibleSourceException
        {
            midiParser = new MidiParser();
            midiParser.setSource(source);
            datasource = source;
        }

        protected TimeBase getMasterTimeBase()
        {
            return new SystemTimeBase();
        }

        protected boolean doRealize()
        {
            if(datasource == null)
                return false;
            try
            {
                datasource.start();
            }
            catch(IOException e)
            {
                return false;
            }
            stream = midiParser.getStream();
            long contentLength = stream.getContentLength();
            long minLocation = 0L;
            minLocation = 0L;
            long maxLocation;
            int bufferSize;
            if(contentLength != -1L)
            {
                maxLocation = contentLength;
                bufferSize = (int)contentLength;
            } else
            {
                maxLocation = 0x7fffffffffffffffL;
                bufferSize = (int)maxLocation;
            }
            int numBuffers = 1;
            track = new BasicTrack(midiParser, null, true, Duration.DURATION_UNKNOWN, new Time(0L), numBuffers, bufferSize, stream, minLocation, maxLocation);
            return true;
        }

        protected boolean doPrefetch()
        {
            if(track == null)
                return false;
            if(sequencer == null)
            {
                try
                {
                    sequencer = MidiSystem.getSequencer();
                    if(sequencer instanceof Synthesizer)
                    {
                        synthesizer = (Synthesizer)sequencer;
                        channels = synthesizer.getChannels();
                    }
                }
                catch(MidiUnavailableException e)
                {
                    return false;
                }
                sequencer.addMetaEventListener(this);
            }
            if(buffer.getLength() == 0)
            {
                track.readFrame(buffer);
                if(buffer.isDiscard() || buffer.isEOM())
                {
                    buffer.setLength(0);
                    return false;
                }
                mididata = (byte[])buffer.getData();
                is = new MidiFileInputStream(mididata, buffer.getLength());
            }
            synchronized(this)
            {
                if(is != null)
                {
                    try
                    {
                        is.rewind();
                    }
                    catch(Exception e) { }
                } else
                {
                    boolean flag = false;
                    return flag;
                }
            }
            try
            {
                sequencer.open();
            }
            catch(MidiUnavailableException e)
            {
                Log.error("Cannot open sequencer " + e + "\n");
                return false;
            }
            catch(Exception e)
            {
                Log.error("Cannot open sequencer " + e + "\n");
                return false;
            }
            try
            {
                sequencer.setSequence(new BufferedInputStream(is));
                long durationNano = sequencer.getMicrosecondLength() * 1000L;
                duration = new Time(durationNano);
            }
            catch(InvalidMidiDataException e)
            {
                Log.error("Invalid Midi Data " + e + "\n");
                sequencer.close();
                return false;
            }
            catch(Exception e)
            {
                Log.error("Error setting sequence " + e + "\n");
                sequencer.close();
                return false;
            }
            return true;
        }

        protected void abortRealize()
        {
        }

        protected void abortPrefetch()
        {
            if(sequencer != null && sequencer.isOpen())
                sequencer.close();
        }

        protected void doStart()
        {
            if(sequencer == null)
                return;
            if(!sequencer.isOpen())
            {
                return;
            } else
            {
                sequencer.start();
                return;
            }
        }

        protected void doStop()
        {
            if(sequencer == null)
            {
                return;
            } else
            {
                sequencer.stop();
                sendEvent(new StopByRequestEvent(this, 600, 500, getTargetState(), getMediaTime()));
                return;
            }
        }

        protected void doDeallocate()
        {
            if(sequencer == null)
                return;
            synchronized(this)
            {
                try
                {
                    sequencer.close();
                }
                catch(Exception e)
                {
                    Log.error("Exception when deallocating: " + e + "\n");
                }
            }
        }

        protected void doClose()
        {
            if(closed)
                return;
            doDeallocate();
            if(datasource != null)
                datasource.disconnect();
            datasource = null;
            sequencer.removeMetaEventListener(this);
            closed = true;
            super.doClose();
        }

        protected float doSetRate(float factor)
        {
            if(sequencer != null)
            {
                sequencer.setTempoFactor(factor);
                return sequencer.getTempoFactor();
            } else
            {
                return 1.0F;
            }
        }

        protected void doSetMediaTime(Time when)
        {
            if(when != null && sequencer != null)
                sequencer.setMicrosecondPosition(when.getNanoseconds() / 1000L);
        }

        public void meta(MetaMessage me)
        {
            if(me.getType() != META_EVENT_END_OF_MEDIA)
                return;
            if(sequencer != null && sequencer.isOpen())
            {
                stopControllerOnly();
                sequencer.stop();
                if(duration == Duration.DURATION_UNKNOWN)
                {
                    duration = getMediaTime();
                    sendEvent(new DurationUpdateEvent(this, duration));
                }
                sendEvent(new EndOfMediaEvent(this, 600, 500, getTargetState(), getMediaTime()));
            }
        }

        public Time getDuration()
        {
            return duration;
        }

        public Control[] getControls()
        {
            if(controls == null)
            {
                controls = new Control[1];
                gc = new GCA();
                controls[0] = gc;
            }
            return controls;
        }

        public void gainChange(float g)
        {
            if(channels == null || gc == null)
                return;
            float level = gc.getLevel();
            for(int i = 0; i < channels.length; i++)
                channels[i].controlChange(7, (int)((double)level * 127D));

        }

        public void muteChange(boolean muted)
        {
            if(channels == null)
                return;
            for(int i = 0; i < channels.length; i++)
                channels[i].setMute(muted);

        }

        private MidiParser midiParser;
        private Track track;
        private Buffer buffer;
        private PullSourceStream stream;
        private Sequencer sequencer;
        private Synthesizer synthesizer;
        protected MidiChannel channels[];
        private Sequence sequence;
        private byte mididata[];
        private MidiFileInputStream is;
        private Time duration;
        private GCA gc;

        MidiController()
        {
            track = null;
            buffer = new Buffer();
            sequencer = null;
            synthesizer = null;
            sequence = null;
            mididata = null;
            is = null;
            duration = Duration.DURATION_UNKNOWN;
        }
    }


    public Handler()
    {
        datasource = null;
        closed = false;
        META_EVENT_END_OF_MEDIA = 47;
        controls = null;
        controller = new MidiController();
        manageController(controller);
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        super.setSource(source);
        controller.setSource(source);
    }

    protected boolean audioEnabled()
    {
        return true;
    }

    protected boolean videoEnabled()
    {
        return false;
    }

    protected TimeBase getMasterTimeBase()
    {
        return controller.getMasterTimeBase();
    }

    public void updateStats()
    {
    }

    private MidiController controller;
    protected DataSource datasource;
    private boolean closed;
    private int META_EVENT_END_OF_MEDIA;
    private Control controls[];





}
