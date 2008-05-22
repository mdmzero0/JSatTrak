// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Handler.java

package com.sun.media.processor.rtsp;

import com.sun.media.BasicController;
import com.sun.media.BasicPlayer;
import com.sun.media.BasicProcessor;
import com.sun.media.JMFSecurity;
import com.sun.media.Log;
import com.sun.media.content.rtsp.RtspUtil;
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
import javax.media.control.BufferControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
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
        first_pass = true;
        sessionError = "cannot create and initialize the RTP Session.";
        super.framePositioning = false;
        rtspUtil = new RtspUtil(this);
        locators = new Vector();
    }

    protected synchronized boolean doConfigure()
    {
        boolean configured = super.doConfigure();
        if(configured)
            configured = initRtspSession();
        return configured;
    }

    private boolean initRtspSession()
    {
        boolean realized = false;
        MediaLocator ml = (MediaLocator)locators.elementAt(0);
        rtspUtil.setUrl(ml.toString());
        String ipAddress = rtspUtil.getServerIpAddress();
        if(ipAddress == null)
        {
            System.out.println("Invalid server address.");
            realized = false;
        } else
        {
            rtspUtil.setUrl(ml.toString());
            realized = rtspUtil.createConnection();
            if(realized)
            {
                realized = rtspUtil.rtspSetup();
                try
                {
                    InetAddress destaddr = InetAddress.getByName(ipAddress);
                    int server_ports[] = rtspUtil.getServerPorts();
                    for(int i = 0; i < rtspUtil.getNumberOfTracks(); i++)
                    {
                        SessionAddress remoteAddress = new SessionAddress(destaddr, server_ports[i]);
                        rtspUtil.getRTPManager(i).addTarget(remoteAddress);
                        BufferControl bc = (BufferControl)rtspUtil.getRTPManager(i).getControl("javax.media.control.BufferControl");
                        String mediaType = rtspUtil.getMediaType(i);
                        if(mediaType.equals("audio"))
                        {
                            bc.setBufferLength(250L);
                            bc.setMinimumThreshold(125L);
                        } else
                        if(mediaType.equals("video"))
                        {
                            bc.setBufferLength(1500L);
                            bc.setMinimumThreshold(250L);
                        }
                    }

                }
                catch(Exception e)
                {
                    Log.error(e.getMessage());
                    return realized;
                }
            }
        }
        if(realized)
        {
            super.state = 1;
            int size = rtspUtil.getNumberOfTracks();
            data_sources = new DataSource[size];
            formats = new Format[size];
            if(!rtspUtil.rtspStart())
                if(first_pass && rtspUtil.getStatusCode() == 454)
                {
                    first_pass = false;
                    return initRtspSession();
                } else
                {
                    return false;
                }
            waitForData();
        }
        return realized;
    }

    private synchronized boolean waitForData()
    {
        try
        {
            synchronized(dataLock)
            {
                while(!dataReady) 
                    dataLock.wait();
            }
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        return dataReady;
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
        RTPManager mgrs[] = rtspUtil.getRTPManagers();
        for(int i = 0; i < mgrs.length; i++)
        {
            if(mgrs[i] != null)
            {
                mgrs[i].removeTargets("Closing session from the RTP Handler");
                mgrs[i].dispose();
            }
            mgrs[i] = null;
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
        RTPManager mgr = (RTPManager)event.getSource();
        if(data_sources == null)
            return;
        RTPManager mgrs[] = rtspUtil.getRTPManagers();
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
            if(data_sources[idx] != null)
                return;
            javax.media.rtp.ReceiveStream stream = null;
            try
            {
                stream = ((NewReceiveStreamEvent)event).getReceiveStream();
                data_sources[idx] = stream.getDataSource();
                RTPControl ctl = (RTPControl)data_sources[idx].getControl("javax.media.rtp.RTPControl");
                if(ctl != null)
                {
                    formats[idx] = ctl.getFormat();
                    if(formats[idx] instanceof AudioFormat)
                        audioEnabled = true;
                    if(formats[idx] instanceof VideoFormat)
                        videoEnabled = true;
                }
                for(int i = 0; i < data_sources.length; i++)
                    if(data_sources[i] == null)
                        return;

                DataSource mixDS;
                try
                {
                    mixDS = Manager.createMergingDataSource(data_sources);
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
        if(source instanceof com.sun.media.protocol.rtsp.DataSource)
        {
            MediaLocator ml = source.getLocator();
            locators.addElement(ml);
        } else
        {
            throw new IncompatibleSourceException();
        }
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

    private final int INITIALIZED = 0;
    private final int REALIZED = 1;
    private final int PLAYING = 2;
    private final int PAUSING = 3;
    private DataSource data_sources[];
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
    private boolean first_pass;
    RtspUtil rtspUtil;
    String sessionError;

}
