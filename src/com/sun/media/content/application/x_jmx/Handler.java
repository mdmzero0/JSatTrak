// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Handler.java

package com.sun.media.content.application.x_jmx;

import com.sun.media.*;
import java.awt.*;
import java.io.*;
import java.util.Vector;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.renderer.VisualContainer;

public class Handler extends BasicPlayer
{
    class PlayerListener
        implements ControllerListener
    {

        public synchronized void controllerUpdate(ControllerEvent ce)
        {
            Player p = (Player)ce.getSourceController();
            if(p == null)
                return;
            int idx;
            for(idx = 0; idx < players.length; idx++)
                if(players[idx] == p)
                    break;

            if(idx >= players.length)
            {
                System.err.println("Unknown player: " + p);
                return;
            }
            if(ce instanceof RealizeCompleteEvent)
            {
                realized[idx] = true;
                for(int i = 0; i < realized.length; i++)
                    if(!realized[i])
                        return;

                synchronized(realizedSync)
                {
                    playersRealized = true;
                    realizedSync.notifyAll();
                }
            }
            if(ce instanceof ControllerErrorEvent)
            {
                players[idx].removeControllerListener(this);
                Log.error("Meta Handler internal error: " + ce);
                players[idx] = null;
            }
        }

        Handler handler;

        public PlayerListener(Handler handler)
        {
            this.handler = handler;
        }
    }

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


    public Handler()
    {
        players = null;
        master = null;
        realized = null;
        locators = new Vector();
        listener = new PlayerListener(this);
        playersRealized = false;
        realizedSync = new Object();
        closed = false;
        audioEnabled = false;
        videoEnabled = false;
        sessionError = "Cannot create a Player for: ";
        container = null;
        super.framePositioning = true;
    }

    protected boolean doRealize()
    {
        super.doRealize();
        MediaLocator ml = null;
        try
        {
            players = new Player[locators.size()];
            realized = new boolean[locators.size()];
            for(int i = 0; i < locators.size(); i++)
            {
                ml = (MediaLocator)locators.elementAt(i);
                players[i] = Manager.createPlayer(ml);
                players[i].addControllerListener(listener);
                realized[i] = false;
                players[i].realize();
            }

        }
        catch(Exception e)
        {
            Log.error(sessionError + ml);
            super.processError = sessionError + ml;
            return false;
        }
        try
        {
            synchronized(realizedSync)
            {
                for(; !playersRealized && !isInterrupted() && !closed; realizedSync.wait());
            }
        }
        catch(Exception e) { }
        if(closed || isInterrupted())
        {
            resetInterrupt();
            super.processError = "Realize interrupted";
            return false;
        }
        try
        {
            master = players[0];
            for(int i = 1; i < players.length; i++)
                master.addController(players[i]);

        }
        catch(IncompatibleTimeBaseException e)
        {
            super.processError = "AddController failed";
            return false;
        }
        manageController(master);
        return true;
    }

    protected void completeRealize()
    {
        super.state = 300;
        super.completeRealize();
    }

    protected void doStart()
    {
        super.doStart();
    }

    protected void doStop()
    {
        super.doStop();
    }

    protected void doDeallocate()
    {
        synchronized(realizedSync)
        {
            realizedSync.notify();
        }
    }

    protected void doClose()
    {
        closed = true;
        synchronized(realizedSync)
        {
            realizedSync.notify();
        }
        stop();
        super.doClose();
    }

    protected TimeBase getMasterTimeBase()
    {
        return master.getTimeBase();
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

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        super.setSource(source);
        if(!(source instanceof PullDataSource))
            throw new IncompatibleSourceException();
        PullSourceStream pss[] = ((PullDataSource)source).getStreams();
        if(pss.length != 1)
            throw new IncompatibleSourceException();
        source.start();
        int len = (int)pss[0].getContentLength();
        if((long)len == -1L)
            throw new IncompatibleSourceException();
        byte barray[] = new byte[len];
        String content;
        try
        {
            len = pss[0].read(barray, 0, len);
            content = new String(barray);
        }
        catch(Exception e)
        {
            throw new IncompatibleSourceException();
        }
        int start = 0;
        int size = content.length();
        String relPath = null;
        char ch = content.charAt(start);
        int idx;
        for(; start < size; start = idx)
        {
            while(ch == ' ' || ch == '\n') 
            {
                if(++start >= size)
                    break;
                ch = content.charAt(start);
            }
            if(start >= size)
                break;
            idx = start;
            do
            {
                if(++idx >= size)
                    break;
                ch = content.charAt(idx);
            } while(ch != '\n');
            String str = content.substring(start, idx);
            if(str.indexOf(':') == -1)
            {
                if(relPath == null)
                {
                    MediaLocator loc = source.getLocator();
                    if(loc == null)
                        throw new IncompatibleSourceException();
                    relPath = loc.toString();
                    int i = relPath.lastIndexOf('/');
                    if(i < 0)
                        i = relPath.lastIndexOf(File.separator);
                    relPath = relPath.substring(0, i + 1);
                }
                str = relPath + str;
            }
            locators.addElement(new MediaLocator(str));
        }

        if(locators.size() < 1)
            throw new IncompatibleSourceException();
        else
            return;
    }

    private void invalidateComp()
    {
        super.controlComp = null;
        super.controls = null;
    }

    public Component getVisualComponent()
    {
        Vector visuals = new Vector(1);
        for(int i = 0; i < players.length; i++)
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

    public void updateStats()
    {
        for(int i = 0; i < players.length; i++)
            if(players[i] != null)
                ((BasicPlayer)players[i]).updateStats();

    }

    Player players[];
    Player master;
    boolean realized[];
    Vector locators;
    ControllerListener listener;
    boolean playersRealized;
    Object realizedSync;
    private boolean closed;
    private boolean audioEnabled;
    private boolean videoEnabled;
    String sessionError;
    private Container container;
}
