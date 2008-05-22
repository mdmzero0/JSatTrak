// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Handler.java

package com.sun.media.processor.rtp;

import com.sun.media.BasicController;
import com.sun.media.BasicPlayer;
import com.sun.media.BasicProcessor;
import com.sun.media.JMFSecurity;
import com.sun.media.Log;
import com.sun.media.rtp.RTPMediaLocator;
import com.sun.media.rtp.RTPSessionMgr;
import java.awt.Component;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Vector;
import javax.media.Clock;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.Player;
import javax.media.Processor;
import javax.media.SystemTimeBase;
import javax.media.TimeBase;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.RTPPushDataSource;
import javax.media.rtp.RTPSocket;
import javax.media.rtp.RTPStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.RTPEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;

public class Handler extends BasicProcessor
    implements ReceiveStreamListener
{
    class StateWaiter
        implements ControllerListener
    {

        public boolean waitForConfigure(Processor p)
        {
            p.addControllerListener(this);
            p.configure();
            synchronized(stateLock)
            {
                while(p.getState() != 180 && !closeDown) 
                {
                    try
                    {
                        stateLock.wait(1000L);
                        continue;
                    }
                    catch(InterruptedException ie) { }
                    break;
                }
            }
            p.removeControllerListener(this);
            return !closeDown;
        }

        public boolean waitForRealize(Processor p)
        {
            p.addControllerListener(this);
            p.realize();
            synchronized(stateLock)
            {
                while(p.getState() != 300 && !closeDown) 
                {
                    try
                    {
                        stateLock.wait(1000L);
                        continue;
                    }
                    catch(InterruptedException ie) { }
                    break;
                }
            }
            p.removeControllerListener(this);
            return !closeDown;
        }

        public void waitForStart(Player p, boolean startOn)
        {
            p.addControllerListener(this);
            if(startOn)
                p.start();
            else
                p.stop();
            synchronized(stateLock)
            {
                while((startOn && p.getState() != 600 || !startOn && p.getState() == 600) && !closeDown) 
                {
                    try
                    {
                        stateLock.wait(1000L);
                        continue;
                    }
                    catch(InterruptedException ie) { }
                    break;
                }
            }
            p.removeControllerListener(this);
        }

        public void waitForClose(Player p)
        {
            p.addControllerListener(this);
            p.close();
            synchronized(stateLock)
            {
                while(!closeDown) 
                {
                    try
                    {
                        stateLock.wait(1000L);
                        continue;
                    }
                    catch(InterruptedException ie) { }
                    break;
                }
            }
            p.removeControllerListener(this);
        }

        public void controllerUpdate(ControllerEvent ce)
        {
            if((ce instanceof ControllerClosedEvent) || (ce instanceof ControllerErrorEvent))
                closeDown = true;
            synchronized(stateLock)
            {
                stateLock.notify();
            }
        }

        boolean closeDown;
        Object stateLock;

        StateWaiter()
        {
            closeDown = false;
            stateLock = new Object();
        }
    }


    public Handler()
    {
        mgrs = null;
        sources = null;
        processor = null;
        formats = null;
        locators = null;
        dataLock = new Object();
        dataReady = false;
        closed = false;
        audioEnabled = false;
        videoEnabled = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        sessionError = "cannot create and initialize the RTP Session.";
        closeSync = new Object();
        super.framePositioning = false;
    }

    protected synchronized boolean doConfigure()
    {
        super.doConfigure();
        try
        {
            if(super.source instanceof RTPSocket)
            {
                mgrs = new RTPSessionMgr[1];
                mgrs[1] = new RTPSessionMgr((RTPSocket)super.source);
                mgrs[1].addReceiveStreamListener(this);
                sources = new DataSource[1];
                sources[0] = super.source;
                formats = new Format[1];
                dataReady = false;
            } else
            {
                SessionAddress localAddr = new SessionAddress();
                mgrs = new RTPSessionMgr[locators.size()];
                sources = new DataSource[locators.size()];
                formats = new Format[locators.size()];
                dataReady = false;
                for(int i = 0; i < locators.size(); i++)
                {
                    RTPMediaLocator rml = (RTPMediaLocator)locators.elementAt(i);
                    mgrs[i] = (RTPSessionMgr)RTPManager.newInstance();
                    mgrs[i].addReceiveStreamListener(this);
                    InetAddress ipAddr = InetAddress.getByName(rml.getSessionAddress());
                    SessionAddress destAddr;
                    if(ipAddr.isMulticastAddress())
                    {
                        localAddr = new SessionAddress(ipAddr, rml.getSessionPort(), rml.getTTL());
                        destAddr = new SessionAddress(ipAddr, rml.getSessionPort(), rml.getTTL());
                    } else
                    {
                        localAddr = new SessionAddress(InetAddress.getLocalHost(), rml.getSessionPort());
                        destAddr = new SessionAddress(ipAddr, rml.getSessionPort());
                    }
                    mgrs[i].initialize(localAddr);
                    mgrs[i].addTarget(destAddr);
                }

            }
        }
        catch(Exception e)
        {
            Log.error("Cannot create the RTP Session: " + e.getMessage());
            super.processError = sessionError;
            return false;
        }
        try
        {
            synchronized(dataLock)
            {
                for(; !dataReady && !isInterrupted() && !closed; dataLock.wait());
            }
        }
        catch(Exception e) { }
        if(closed || isInterrupted())
        {
            resetInterrupt();
            super.processError = "no RTP data was received.";
            return false;
        } else
        {
            return true;
        }
    }

    protected void completeConfigure()
    {
        super.state = 180;
        super.completeConfigure();
    }

    protected void doFailedConfigure()
    {
        closeSessions();
        super.doFailedConfigure();
    }

    private void closeSessions()
    {
        synchronized(closeSync)
        {
            for(int i = 0; i < mgrs.length; i++)
                if(mgrs[i] != null)
                {
                    mgrs[i].removeTargets("Closing session from the RTP Handler");
                    mgrs[i].dispose();
                    mgrs[i] = null;
                }

        }
    }

    protected boolean doRealize()
    {
        return waitForRealize(processor);
    }

    protected void completeRealize()
    {
        super.state = 300;
        super.completeRealize();
    }

    protected void doFailedRealize()
    {
        closeSessions();
        super.doFailedRealize();
    }

    protected void doStart()
    {
        super.doStart();
        waitForStart(processor);
    }

    protected void doStop()
    {
        super.doStop();
        waitForStop(processor);
    }

    protected void doDeallocate()
    {
        processor.deallocate();
        synchronized(dataLock)
        {
            dataLock.notifyAll();
        }
    }

    protected void doClose()
    {
        closed = true;
        synchronized(dataLock)
        {
            dataLock.notify();
        }
        stop();
        if(processor != null)
            processor.close();
        closeSessions();
        super.doClose();
    }

    public void setTimeBase(TimeBase timebase)
        throws IncompatibleTimeBaseException
    {
    }

    protected TimeBase getMasterTimeBase()
    {
        return new SystemTimeBase();
    }

    protected boolean audioEnabled()
    {
        return audioEnabled;
    }

    protected boolean videoEnabled()
    {
        return videoEnabled;
    }

    private void sendMyEvent(ControllerEvent e)
    {
        super.sendEvent(e);
    }

    public void update(ReceiveStreamEvent event)
    {
        RTPSessionMgr mgr = (RTPSessionMgr)event.getSource();
        int idx;
        for(idx = 0; idx < mgrs.length; idx++)
            if(mgrs[idx] == mgr)
                break;

        if(idx >= mgrs.length)
        {
            System.err.println("Unknown manager: " + mgr);
            return;
        }
        if(event instanceof RemotePayloadChangeEvent)
        {
            Log.comment("Received an RTP PayloadChangeEvent");
            Log.error("The RTP processor cannot handle mid-stream payload change.\n");
            sendEvent(new ControllerErrorEvent(this, "Cannot handle mid-stream payload change."));
            close();
        }
        if(event instanceof NewReceiveStreamEvent)
        {
            if(sources[idx] != null)
                return;
            javax.media.rtp.ReceiveStream stream = null;
            try
            {
                stream = ((NewReceiveStreamEvent)event).getReceiveStream();
                sources[idx] = stream.getDataSource();
                RTPControl ctl = (RTPControl)sources[idx].getControl("javax.media.rtp.RTPControl");
                if(ctl != null)
                {
                    formats[idx] = ctl.getFormat();
                    if(formats[idx] instanceof AudioFormat)
                        audioEnabled = true;
                    if(formats[idx] instanceof VideoFormat)
                        videoEnabled = true;
                }
                if(super.source instanceof RTPSocket)
                    ((RTPSocket)super.source).setChild(sources[idx]);
                else
                    ((com.sun.media.protocol.rtp.DataSource)super.source).setChild((com.sun.media.protocol.rtp.DataSource)sources[idx]);
                for(int i = 0; i < sources.length; i++)
                    if(sources[i] == null)
                        return;

                DataSource mixDS;
                try
                {
                    mixDS = Manager.createMergingDataSource(sources);
                }
                catch(Exception e)
                {
                    System.err.println("Cannot merge data sources.");
                    return;
                }
                try
                {
                    processor = Manager.createProcessor(mixDS);
                }
                catch(Exception e)
                {
                    System.err.println("Cannot create the mix processor.");
                    return;
                }
                if(!waitForConfigure(processor))
                    return;
                synchronized(dataLock)
                {
                    dataReady = true;
                    dataLock.notifyAll();
                }
            }
            catch(Exception e)
            {
                System.err.println("NewReceiveStreamEvent exception " + e.getMessage());
                return;
            }
        }
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        super.setSource(source);
        if(source instanceof com.sun.media.protocol.rtp.DataSource)
        {
            MediaLocator ml = source.getLocator();
            String mlStr = ml.getRemainder();
            int start;
            for(start = 0; mlStr.charAt(start) == '/'; start++);
            locators = new Vector();
            try
            {
                String str;
                int i;
                RTPMediaLocator rml;
                for(; start < mlStr.length() && (i = mlStr.indexOf("&", start)) != -1; start = i + 1)
                {
                    str = mlStr.substring(start, i);
                    rml = new RTPMediaLocator("rtp://" + str);
                    locators.addElement(rml);
                }

                if(start != 0)
                    str = mlStr.substring(start);
                else
                    str = mlStr;
                rml = new RTPMediaLocator("rtp://" + str);
                locators.addElement(rml);
            }
            catch(Exception e)
            {
                throw new IncompatibleSourceException();
            }
        } else
        if(!(source instanceof RTPSocket))
            throw new IncompatibleSourceException();
        RTPControl ctl = (RTPControl)source.getControl("javax.media.rtp.RTPControl");
        if(ctl != null)
            ctl.addFormat(new AudioFormat("dvi/rtp", 44100D, 4, 1), 18);
    }

    private void invalidateComp()
    {
        super.controlComp = null;
        super.controls = null;
    }

    public Component getVisualComponent()
    {
        super.getVisualComponent();
        return processor.getVisualComponent();
    }

    public Control[] getControls()
    {
        return processor.getControls();
    }

    public void updateStats()
    {
        if(processor != null)
            ((BasicProcessor)processor).updateStats();
    }

    public TrackControl[] getTrackControls()
        throws NotConfiguredError
    {
        super.getTrackControls();
        return processor.getTrackControls();
    }

    public ContentDescriptor[] getSupportedContentDescriptors()
        throws NotConfiguredError
    {
        super.getSupportedContentDescriptors();
        return processor.getSupportedContentDescriptors();
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor ocd)
        throws NotConfiguredError
    {
        super.setContentDescriptor(ocd);
        return processor.setContentDescriptor(ocd);
    }

    public ContentDescriptor getContentDescriptor()
        throws NotConfiguredError
    {
        super.getContentDescriptor();
        return processor.getContentDescriptor();
    }

    public DataSource getDataOutput()
        throws NotRealizedError
    {
        super.getDataOutput();
        return processor.getDataOutput();
    }

    private boolean waitForConfigure(Processor p)
    {
        return (new StateWaiter()).waitForConfigure(p);
    }

    private boolean waitForRealize(Processor p)
    {
        return (new StateWaiter()).waitForRealize(p);
    }

    private void waitForStart(Player p)
    {
        (new StateWaiter()).waitForStart(p, true);
    }

    private void waitForStop(Player p)
    {
        (new StateWaiter()).waitForStart(p, false);
    }

    private void waitForClose(Player p)
    {
        (new StateWaiter()).waitForClose(p);
    }

    RTPSessionMgr mgrs[];
    DataSource sources[];
    Processor processor;
    Format formats[];
    Vector locators;
    Object dataLock;
    boolean dataReady;
    private boolean closed;
    private boolean audioEnabled;
    private boolean videoEnabled;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    String sessionError;
    Object closeSync;

}
