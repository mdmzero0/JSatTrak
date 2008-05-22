// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicFilterModule.java

package com.sun.media;

import java.awt.Frame;
import java.awt.Window;
import java.io.PrintStream;
import javax.media.*;
import javax.media.control.FrameProcessingControl;

// Referenced classes of package com.sun.media:
//            BasicModule, BasicInputConnector, BasicOutputConnector, SimpleGraphBuilder, 
//            InputConnector, Connector, OutputConnector, ModuleListener, 
//            PlaybackEngine, JMD, Log

public class BasicFilterModule extends BasicModule
{

    public BasicFilterModule(Codec c)
    {
        frameControl = null;
        curFramesBehind = 0.0F;
        prevFramesBehind = 0.0F;
        readPendingFlag = false;
        writePendingFlag = false;
        failed = false;
        markerSet = false;
        lastHdr = null;
        ic = new BasicInputConnector();
        registerInputConnector("input", ic);
        oc = new BasicOutputConnector();
        registerOutputConnector("output", oc);
        setCodec(c);
        super.protocol = 0;
        Object control = c.getControl("javax.media.control.FrameProcessingControl");
        if(control instanceof FrameProcessingControl)
            frameControl = (FrameProcessingControl)control;
    }

    public boolean doRealize()
    {
        if(codec != null)
            try
            {
                codec.open();
            }
            catch(ResourceUnavailableException rue)
            {
                return false;
            }
        return true;
    }

    public boolean doPrefetch()
    {
        return super.doPrefetch();
    }

    public void doClose()
    {
        if(codec != null)
            codec.close();
        if(controlFrame != null)
        {
            controlFrame.dispose();
            controlFrame = null;
        }
    }

    public void setFormat(Connector c, Format f)
    {
        if(c == ic)
        {
            if(codec != null)
                codec.setInputFormat(f);
        } else
        if(c == oc && codec != null)
            codec.setOutputFormat(f);
    }

    public boolean setCodec(String codec)
    {
        return true;
    }

    public boolean setCodec(Codec codec)
    {
        this.codec = codec;
        return true;
    }

    public Codec getCodec()
    {
        return codec;
    }

    public boolean isThreaded()
    {
        return getProtocol() == 1;
    }

    public Object[] getControls()
    {
        return codec.getControls();
    }

    public Object getControl(String s)
    {
        return codec.getControl(s);
    }

    protected void setFramesBehind(float framesBehind)
    {
        curFramesBehind = framesBehind;
    }

    protected boolean reinitCodec(Format input)
    {
        if(codec != null)
        {
            if(codec.setInputFormat(input) != null)
                return true;
            codec.close();
            codec = null;
        }
        Codec c;
        if((c = SimpleGraphBuilder.findCodec(input, null, null, null)) == null)
        {
            return false;
        } else
        {
            setCodec(c);
            return true;
        }
    }

    public void process()
    {
        do
        {
            Buffer inputBuffer;
            if(readPendingFlag)
            {
                inputBuffer = storedInputBuffer;
            } else
            {
                inputBuffer = ic.getValidBuffer();
                Format incomingFormat = inputBuffer.getFormat();
                if(incomingFormat == null)
                {
                    incomingFormat = ic.getFormat();
                    inputBuffer.setFormat(incomingFormat);
                }
                if(incomingFormat != ic.getFormat() && incomingFormat != null && !incomingFormat.equals(ic.getFormat()) && !inputBuffer.isDiscard())
                {
                    if(writePendingFlag)
                    {
                        storedOutputBuffer.setDiscard(true);
                        oc.writeReport();
                        writePendingFlag = false;
                    }
                    if(!reinitCodec(inputBuffer.getFormat()))
                    {
                        inputBuffer.setDiscard(true);
                        ic.readReport();
                        failed = true;
                        if(super.moduleListener != null)
                            super.moduleListener.formatChangedFailure(this, ic.getFormat(), inputBuffer.getFormat());
                        return;
                    }
                    Format oldFormat = ic.getFormat();
                    ic.setFormat(inputBuffer.getFormat());
                    if(super.moduleListener != null)
                        super.moduleListener.formatChanged(this, oldFormat, inputBuffer.getFormat());
                }
                if((inputBuffer.getFlags() & 0x400) != 0)
                    markerSet = true;
                if(PlaybackEngine.DEBUG && inputBuffer != null)
                    super.jmd.moduleIn(this, 0, inputBuffer, true);
            }
            Buffer outputBuffer;
            if(writePendingFlag)
            {
                outputBuffer = storedOutputBuffer;
            } else
            {
                outputBuffer = oc.getEmptyBuffer();
                if(outputBuffer != null)
                {
                    if(PlaybackEngine.DEBUG)
                        super.jmd.moduleOut(this, 0, outputBuffer, true);
                    outputBuffer.setLength(0);
                    outputBuffer.setOffset(0);
                    lastHdr = outputBuffer.getHeader();
                }
            }
            outputBuffer.setTimeStamp(inputBuffer.getTimeStamp());
            outputBuffer.setDuration(inputBuffer.getDuration());
            outputBuffer.setSequenceNumber(inputBuffer.getSequenceNumber());
            outputBuffer.setFlags(inputBuffer.getFlags());
            outputBuffer.setHeader(inputBuffer.getHeader());
            if(super.resetted)
            {
                if((inputBuffer.getFlags() & 0x200) != 0)
                {
                    codec.reset();
                    super.resetted = false;
                }
                readPendingFlag = writePendingFlag = false;
                ic.readReport();
                oc.writeReport();
                return;
            }
            if(failed || inputBuffer.isDiscard())
            {
                if(markerSet)
                {
                    outputBuffer.setFlags(outputBuffer.getFlags() & 0xfffffbff);
                    markerSet = false;
                }
                curFramesBehind = 0.0F;
                ic.readReport();
                if(!writePendingFlag)
                    oc.writeReport();
                return;
            }
            if(frameControl != null && curFramesBehind != prevFramesBehind && (inputBuffer.getFlags() & 0x20) == 0)
            {
                frameControl.setFramesBehind(curFramesBehind);
                prevFramesBehind = curFramesBehind;
            }
            int rc = 0;
            try
            {
                rc = codec.process(inputBuffer, outputBuffer);
            }
            catch(Throwable e)
            {
                Log.dumpStack(e);
                if(super.moduleListener != null)
                    super.moduleListener.internalErrorOccurred(this);
            }
            if(PlaybackEngine.TRACE_ON && !verifyBuffer(outputBuffer))
            {
                System.err.println("verify buffer failed: " + codec);
                Thread.dumpStack();
                if(super.moduleListener != null)
                    super.moduleListener.internalErrorOccurred(this);
            }
            if((rc & 8) != 0)
            {
                failed = true;
                if(super.moduleListener != null)
                    super.moduleListener.pluginTerminated(this);
                readPendingFlag = writePendingFlag = false;
                ic.readReport();
                oc.writeReport();
                return;
            }
            if(curFramesBehind > 0.0F && outputBuffer.isDiscard())
            {
                curFramesBehind--;
                if(curFramesBehind < 0.0F)
                    curFramesBehind = 0.0F;
                BasicFilterModule _tmp = this;
                rc &= ~4;
            }
            if((rc & 1) != 0)
            {
                outputBuffer.setDiscard(true);
                if(markerSet)
                {
                    outputBuffer.setFlags(outputBuffer.getFlags() & 0xfffffbff);
                    markerSet = false;
                }
                if(PlaybackEngine.DEBUG)
                    super.jmd.moduleIn(this, 0, inputBuffer, false);
                ic.readReport();
                if(PlaybackEngine.DEBUG)
                    super.jmd.moduleOut(this, 0, outputBuffer, false);
                oc.writeReport();
                readPendingFlag = writePendingFlag = false;
                return;
            }
            if(outputBuffer.isEOM() && ((rc & 2) != 0 || (rc & 4) != 0))
                outputBuffer.setEOM(false);
            if((rc & 4) != 0)
            {
                writePendingFlag = true;
                storedOutputBuffer = outputBuffer;
            } else
            {
                if(PlaybackEngine.DEBUG)
                    super.jmd.moduleOut(this, 0, outputBuffer, false);
                if(markerSet)
                {
                    outputBuffer.setFlags(outputBuffer.getFlags() | 0x400);
                    markerSet = false;
                }
                oc.writeReport();
                writePendingFlag = false;
            }
            if((rc & 2) != 0 || inputBuffer.isEOM() && !outputBuffer.isEOM())
            {
                readPendingFlag = true;
                storedInputBuffer = inputBuffer;
            } else
            {
                if(PlaybackEngine.DEBUG)
                    super.jmd.moduleIn(this, 0, inputBuffer, false);
                inputBuffer.setHeader(lastHdr);
                ic.readReport();
                readPendingFlag = false;
            }
        } while(readPendingFlag);
    }

    protected Codec codec;
    protected InputConnector ic;
    protected OutputConnector oc;
    protected FrameProcessingControl frameControl;
    protected float curFramesBehind;
    protected float prevFramesBehind;
    protected Frame controlFrame;
    protected final boolean VERBOSE_CONTROL = false;
    protected Buffer storedInputBuffer;
    protected Buffer storedOutputBuffer;
    protected boolean readPendingFlag;
    protected boolean writePendingFlag;
    private boolean failed;
    private boolean markerSet;
    private Object lastHdr;
}
