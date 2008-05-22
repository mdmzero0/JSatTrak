// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Handler.java

package com.sun.media.content.rtsp;

import com.sun.media.BasicController;
import com.sun.media.BasicPlayer;
import com.sun.media.Log;
import com.sun.media.controls.RtspAdapter;
import com.sun.media.protocol.BufferListener;
import com.sun.media.protocol.rtp.DataSource;
import com.sun.media.rtsp.Timer;
import com.sun.media.rtsp.TimerListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Vector;
import javax.media.Clock;
import javax.media.ClockStartedError;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Duration;
import javax.media.DurationUpdateEvent;
import javax.media.EndOfMediaEvent;
import javax.media.GainControl;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NotRealizedError;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.SystemTimeBase;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.control.BufferControl;
import javax.media.renderer.VisualContainer;
import javax.media.rtp.RTPManager;
import javax.media.rtp.RTPStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.RTPEvent;
import javax.media.rtp.event.ReceiveStreamEvent;

// Referenced classes of package com.sun.media.content.rtsp:
//            RtspUtil

public class Handler extends BasicPlayer
    implements ReceiveStreamListener, TimerListener, BufferListener
{
    class LightPanel extends Container
        implements VisualContainer
    {

        public LightPanel(Vector visuals)
        {
        }
    }

    class HeavyPanel extends Panel
        implements VisualContainer
    {

        public HeavyPanel(Vector visuals)
        {
        }
    }

    class StateListener
        implements ControllerListener
    {

        public void controllerUpdate(ControllerEvent ce)
        {
            if(!(ce instanceof ControllerClosedEvent));
            if(ce instanceof ResourceUnavailableEvent)
            {
                waitFailed = true;
                synchronized(stateSync)
                {
                    stateSync.notify();
                }
            }
            if(ce instanceof RealizeCompleteEvent)
                synchronized(stateSync)
                {
                    stateSync.notify();
                }
            if(ce instanceof ControllerEvent)
                synchronized(stateSync)
                {
                    stateSync.notify();
                }
            if(!(ce instanceof EndOfMediaEvent));
        }

        StateListener()
        {
        }
    }


    public Handler()
    {
        readySync = new Object();
        stateSync = new Object();
        first_pass = true;
        container = null;
        rtspUtil = new RtspUtil(this);
        super.framePositioning = false;
        playerList = new Vector();
        state = 0;
        super.stopThreadEnabled = true;
    }

    protected synchronized boolean doRealize()
    {
        boolean realized = super.doRealize();
        if(realized)
        {
            realized = initRtspSession();
            if(!realized)
            {
                super.processError = rtspUtil.getProcessError();
            } else
            {
                long duration = rtspUtil.getDuration();
                if(duration > 0L)
                    sendEvent(new DurationUpdateEvent(this, new Time(duration)));
            }
        }
        return realized;
    }

    private boolean initRtspSession()
    {
        boolean realized = false;
        rtspUtil.setUrl(url);
        String ipAddress = rtspUtil.getServerIpAddress();
        if(ipAddress == null)
        {
            Log.error("Invalid server address");
            rtspUtil.setProcessError("Invalid server address");
            realized = false;
        } else
        {
            realized = rtspUtil.createConnection();
            if(realized)
            {
                realized = rtspUtil.rtspSetup();
                try
                {
                    InetAddress destaddr = InetAddress.getByName(ipAddress);
                    int numberOfTracks = rtspUtil.getNumberOfTracks();
                    int server_ports[] = rtspUtil.getServerPorts();
                    for(int i = 0; i < numberOfTracks; i++)
                    {
                        SessionAddress remoteAddress = new SessionAddress(destaddr, server_ports[i]);
                        RTPManager mgr = rtspUtil.getRTPManager(i);
                        mgr.addTarget(remoteAddress);
                        BufferControl bc = (BufferControl)mgr.getControl("javax.media.control.BufferControl");
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
            state = 1;
            int size = rtspUtil.getNumberOfTracks();
            players = new Player[size];
            data_sources = new DataSource[size];
            track_ready = new boolean[size];
            dataReceived = false;
            if(!rtspUtil.rtspStart())
                if(first_pass && rtspUtil.getStatusCode() == 454)
                {
                    first_pass = false;
                    playerList = new Vector();
                    return initRtspSession();
                } else
                {
                    return false;
                }
            waitForData();
            if(playerList.size() > 0)
            {
                rtspStop();
                rtspUtil.setStartPos(0.0D);
                for(int i = 0; i < rtspUtil.getNumberOfTracks(); i++)
                    data_sources[i].flush();

            } else
            {
                rtspUtil.setProcessError("Media tracks not supported");
                realized = false;
            }
        }
        return realized;
    }

    public boolean doPrefetch()
    {
        boolean prefetched = super.doPrefetch();
        return prefetched;
    }

    public void doStart()
    {
        if(state >= 1 && state != 2)
        {
            for(int i = 0; i < rtspUtil.getNumberOfTracks(); i++)
            {
                track_ready[i] = rtspUtil.getRTPManager(i) == null;
                data_sources[i].prebuffer();
            }

            boolean success = rtspUtil.rtspStart();
            synchronized(readySync)
            {
                boolean ready = true;
                for(int i = 0; i < rtspUtil.getNumberOfTracks(); i++)
                {
                    if(track_ready[i])
                        continue;
                    ready = false;
                    break;
                }

                if(!ready)
                    try
                    {
                        readySync.wait(3000L);
                    }
                    catch(Exception e) { }
            }
            if(success)
            {
                super.doStart();
                startPlayers();
                state = 2;
                long duration = rtspUtil.getDuration();
                if(duration > 0L)
                {
                    timer = new Timer(this, (duration + 0x1dcd6500L) - getMediaTime().getNanoseconds());
                    timer.start();
                }
            }
        }
    }

    public void doSetMediaTime(Time now)
    {
        super.doSetMediaTime(now);
        rtspUtil.setStartPos(now.getNanoseconds());
        for(int i = 0; i < rtspUtil.getNumberOfTracks(); i++)
            data_sources[i].flush();

    }

    public Time getMediaTime()
    {
        Time time = super.getMediaTime();
        return time;
    }

    public void timerExpired()
    {
        timer = null;
        processEndOfMedia();
    }

    public void doStop()
    {
        if(state == 2)
        {
            super.doStop();
            if(timer != null)
            {
                timer.stopTimer();
                timer.removeListener(this);
                timer = null;
            }
            stopPlayers();
            rtspStop();
            state = 3;
        }
    }

    public void rtspStop()
    {
        rtspUtil.setStartPos(getMediaTime().getNanoseconds());
        rtspUtil.rtspStop();
    }

    public void doClose()
    {
        stopPlayers();
        closePlayers();
        if(timer != null)
        {
            timer.stopTimer();
            timer.removeListener(this);
            timer = null;
        }
        if(state == 2)
            rtspUtil.rtspTeardown();
        state = 0;
        rtspUtil.closeConnection();
        for(int i = 0; i < rtspUtil.getNumberOfTracks(); i++)
        {
            RTPManager mgr = rtspUtil.getRTPManager(i);
            mgr.removeTargets("server down.");
            mgr.dispose();
        }

        super.doClose();
    }

    public float setRate(float rate)
    {
        if(getState() < 300)
            throwError(new NotRealizedError("Cannot set rate on an unrealized Player."));
        return 1.0F;
    }

    public void setStopTime(Time t)
    {
        controllerSetStopTime(t);
    }

    protected void stopAtTime()
    {
        controllerStopAtTime();
    }

    public synchronized void addController(Controller newController)
        throws IncompatibleTimeBaseException
    {
        int playerState = getState();
        if(playerState == 600)
            throwError(new ClockStartedError("Cannot add controller to a started player"));
        if(playerState == 100 || playerState == 200)
            throwError(new NotRealizedError("A Controller cannot be added to an Unrealized Player"));
        throw new IncompatibleTimeBaseException();
    }

    public boolean audioEnabled()
    {
        boolean enabled = true;
        return enabled;
    }

    public boolean videoEnabled()
    {
        boolean enabled = true;
        return enabled;
    }

    public void updateStats()
    {
    }

    protected TimeBase getMasterTimeBase()
    {
        return new SystemTimeBase();
    }

    public synchronized void update(ReceiveStreamEvent event)
    {
        RTPManager source = (RTPManager)event.getSource();
        if(event instanceof NewReceiveStreamEvent)
        {
            javax.media.rtp.ReceiveStream stream = ((NewReceiveStreamEvent)event).getReceiveStream();
            javax.media.rtp.Participant part = stream.getParticipant();
            int numberOfTracks = rtspUtil.getNumberOfTracks();
            for(int i = 0; i < numberOfTracks; i++)
            {
                if(source != rtspUtil.getRTPManager(i))
                    continue;
                DataSource ds = (DataSource)stream.getDataSource();
                try
                {
                    players[i] = Manager.createPlayer(ds);
                }
                catch(Exception e)
                {
                    System.err.println("Failed to create a player from the given Data Source: " + e);
                }
                try
                {
                    waitFailed = false;
                    players[i].addControllerListener(new StateListener());
                    players[i].realize();
                    waitForState(players[i], 300);
                }
                catch(Exception e) { }
                if(players[i].getState() == 300)
                {
                    playerList.addElement(players[i]);
                    ds.setBufferListener(this);
                    data_sources[i] = ds;
                } else
                {
                    players[i].close();
                    players[i] = null;
                    rtspUtil.removeTrack(i);
                }
                break;
            }

            if(playerList.size() == rtspUtil.getNumberOfTracks())
            {
                dataReceived = true;
                synchronized(this)
                {
                    notifyAll();
                }
            }
        } else
        if(!(event instanceof ByeEvent));
    }

    public void minThresholdReached(javax.media.protocol.DataSource ds)
    {
        synchronized(readySync)
        {
            for(int i = 0; i < rtspUtil.getNumberOfTracks(); i++)
            {
                if(ds != data_sources[i])
                    continue;
                track_ready[i] = true;
                break;
            }

            boolean all_ready = true;
            for(int i = 0; i < rtspUtil.getNumberOfTracks(); i++)
            {
                if(track_ready[i])
                    continue;
                all_ready = false;
                break;
            }

            if(all_ready)
                readySync.notifyAll();
        }
    }

    public long getMediaNanoseconds()
    {
        long value = super.getMediaNanoseconds();
        return value;
    }

    public Time getDuration()
    {
        long t = rtspUtil.getDuration();
        if(t <= 0L)
            return Duration.DURATION_UNKNOWN;
        else
            return new Time(t);
    }

    private synchronized void waitForState(Player p, int state)
    {
        while(p.getState() < state && !waitFailed) 
            synchronized(stateSync)
            {
                try
                {
                    stateSync.wait();
                }
                catch(InterruptedException ie) { }
            }
    }

    private synchronized boolean waitForData()
    {
        try
        {
            synchronized(this)
            {
                while(!dataReceived) 
                    wait();
            }
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        return dataReceived;
    }

    public Component getVisualComponent()
    {
        Vector visuals = new Vector(1);
        for(int i = 0; i < rtspUtil.getNumberOfTracks(); i++)
            if(players[i] != null)
            {
                Component comp = players[i].getVisualComponent();
                if(comp != null)
                    visuals.addElement(comp);
            }

        if(visuals.size() == 0)
            return null;
        if(visuals.size() == 1)
            return (Component)visuals.elementAt(0);
        else
            return createVisualContainer(visuals);
    }

    protected Component createVisualContainer(Vector visuals)
    {
        Boolean hint = (Boolean)Manager.getHint(3);
        if(container == null)
        {
            if(hint == null || !hint.booleanValue())
                container = new HeavyPanel(visuals);
            else
                container = new LightPanel(visuals);
            container.setLayout(new FlowLayout());
            container.setBackground(Color.black);
            for(int i = 0; i < visuals.size(); i++)
            {
                Component c = (Component)visuals.elementAt(i);
                container.add(c);
                c.setSize(c.getPreferredSize());
            }

        }
        return container;
    }

    public GainControl getGainControl()
    {
        GainControl gainControl = null;
        for(int i = 0; i < playerList.size(); i++)
        {
            Player player = (Player)playerList.elementAt(i);
            gainControl = player.getGainControl();
            if(gainControl != null)
                break;
        }

        return gainControl;
    }

    public Control[] getControls()
    {
        int size = 0;
        for(int i = 0; i < playerList.size(); i++)
        {
            Control controls[] = ((Player)playerList.elementAt(i)).getControls();
            size += controls.length;
        }

        Control rtspControls[] = new Control[++size];
        RtspAdapter rtspAdapter = new RtspAdapter();
        rtspAdapter.setRTPManagers(rtspUtil.getRTPManagers());
        rtspAdapter.setMediaTypes(rtspUtil.getMediaTypes());
        int counter = 0;
        rtspControls[counter++] = rtspAdapter;
        for(int i = 0; i < playerList.size(); i++)
        {
            Control controls[] = ((Player)playerList.elementAt(i)).getControls();
            for(int k = 0; k < controls.length; k++)
                rtspControls[counter++] = controls[k];

        }

        return rtspControls;
    }

    private void startPlayers()
    {
        for(int i = 0; i < playerList.size(); i++)
        {
            Player player = (Player)playerList.elementAt(i);
            player.start();
        }

    }

    private void stopPlayers()
    {
        for(int i = 0; i < playerList.size(); i++)
        {
            Player player = (Player)playerList.elementAt(i);
            player.stop();
        }

    }

    private void closePlayers()
    {
        for(int i = 0; i < playerList.size(); i++)
        {
            Player player = (Player)playerList.elementAt(i);
            player.close();
        }

    }

    public void setSource(javax.media.protocol.DataSource source)
        throws IOException, IncompatibleSourceException
    {
        if(source instanceof com.sun.media.protocol.rtsp.DataSource)
        {
            MediaLocator ml = source.getLocator();
            try
            {
                url = ml.toString();
            }
            catch(Exception e)
            {
                throw new IncompatibleSourceException();
            }
        } else
        {
            throw new IncompatibleSourceException();
        }
    }

    private final int INITIALIZED = 0;
    private final int REALIZED = 1;
    private final int PLAYING = 2;
    private final int PAUSING = 3;
    private RtspUtil rtspUtil;
    private Player players[];
    private Vector playerList;
    private boolean dataReceived;
    private DataSource data_sources[];
    private boolean track_ready[];
    private String url;
    private Object readySync;
    private Object stateSync;
    private boolean waitFailed;
    private int state;
    private boolean first_pass;
    private Timer timer;
    private Container container;


}
