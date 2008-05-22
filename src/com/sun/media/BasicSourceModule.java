// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicSourceModule.java

package com.sun.media;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.rtp.util.RTPTimeBase;
import com.sun.media.util.LoopThread;
import com.sun.media.util.MediaThread;
import com.sun.media.util.jdk12;
import com.sun.media.util.jdk12PriorityAction;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media:
//            BasicModule, SourceThread, MyOutputConnector, PlaybackEngine, 
//            BasicPlugIn, JMFSecurity, BasicConnector, BasicOutputConnector, 
//            CreateSourceThreadAction, JMFSecurityManager, Connector

public class BasicSourceModule extends BasicModule
    implements Duration, Positionable
{

    public static BasicSourceModule createModule(DataSource ds)
        throws IOException, IncompatibleSourceException
    {
        Demultiplexer parser = createDemultiplexer(ds);
        if(parser == null)
            return null;
        else
            return new BasicSourceModule(ds, parser);
    }

    protected BasicSourceModule(DataSource ds, Demultiplexer demux)
    {
        tracks = new Track[0];
        bitsRead = 0L;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        resetSync = new Object();
        started = false;
        systemTimeBase = new SystemTimeBase();
        lastSystemTime = 0L;
        originSystemTime = 0L;
        currentSystemTime = 0L;
        lastPositionSet = new Time(0L);
        rtpMapperUpdatable = null;
        rtpMapper = null;
        currentRTPTime = 0L;
        oldOffset = 0L;
        rtpOffsetInvalid = true;
        cname = null;
        errMsg = null;
        latencyTrack = -1;
        source = ds;
        parser = demux;
        javax.media.protocol.SourceStream stream = null;
        if(source instanceof PullDataSource)
            stream = ((PullDataSource)source).getStreams()[0];
        else
        if(source instanceof PushDataSource)
            stream = ((PushDataSource)source).getStreams()[0];
    }

    protected static Demultiplexer createDemultiplexer(DataSource ds)
        throws IOException, IncompatibleSourceException
    {
        ContentDescriptor cd = new ContentDescriptor(ds.getContentType());
        Vector cnames = PlugInManager.getPlugInList(cd, null, 1);
        Demultiplexer parser = null;
        IOException ioe = null;
        IncompatibleSourceException ise = null;
        for(int i = 0; i < cnames.size(); i++)
            try
            {
                Class cls = BasicPlugIn.getClassForName((String)cnames.elementAt(i));
                Object p = cls.newInstance();
                if(!(p instanceof Demultiplexer))
                    continue;
                parser = (Demultiplexer)p;
                try
                {
                    parser.setSource(ds);
                    break;
                }
                catch(IOException e)
                {
                    parser = null;
                    ioe = e;
                }
                catch(IncompatibleSourceException e)
                {
                    parser = null;
                    ise = e;
                }
            }
            catch(ClassNotFoundException e) { }
            catch(InstantiationException e) { }
            catch(IllegalAccessException e) { }

        if(parser == null)
        {
            if(ioe != null)
                throw ioe;
            if(ise != null)
                throw ise;
        }
        return parser;
    }

    public boolean doRealize()
    {
        try
        {
            parser.open();
        }
        catch(ResourceUnavailableException e)
        {
            errMsg = "Resource unavailable: " + e.getMessage();
            return false;
        }
        try
        {
            parser.start();
            tracks = parser.getTracks();
        }
        catch(BadHeaderException e)
        {
            errMsg = "Bad header in the media: " + e.getMessage();
            parser.close();
            return false;
        }
        catch(IOException e)
        {
            errMsg = "IO exception: " + e.getMessage();
            parser.close();
            return false;
        }
        if(tracks == null || tracks.length == 0)
        {
            errMsg = "The media has 0 track";
            parser.close();
            return false;
        }
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
            catch(Exception e)
            {
                securityPrivelege = false;
            }
        }
        loops = new SourceThread[tracks.length];
        connectorNames = new String[tracks.length];
        for(int i = 0; i < tracks.length; i++)
        {
            MyOutputConnector oc = new MyOutputConnector(tracks[i]);
            oc.setProtocol(0);
            oc.setSize(1);
            connectorNames[i] = tracks[i].toString();
            registerOutputConnector(tracks[i].toString(), oc);
            loops[i] = null;
        }

        engine = (PlaybackEngine)getController();
        if(engine == null || !engine.isRTP())
            parser.stop();
        return true;
    }

    SourceThread createSourceThread(int idx)
    {
        SourceThread thread = null;
        MyOutputConnector oc = (MyOutputConnector)getOutputConnector(connectorNames[idx]);
        if(oc == null || oc.getInputConnector() == null)
        {
            tracks[idx].setEnabled(false);
            return null;
        }
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
        {
            try
            {
                Constructor cons = CreateSourceThreadAction.cons;
                Constructor pcons = jdk12PriorityAction.cons;
                thread = (SourceThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.SourceThread.class, this, oc, new Integer(idx)
                    })
                });
                int priority;
                if(tracks[idx].getFormat() instanceof AudioFormat)
                    priority = MediaThread.getAudioPriority();
                else
                    priority = MediaThread.getVideoPriority();
                thread.useVideoPriority();
                jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    pcons.newInstance(new Object[] {
                        thread, new Integer(priority)
                    })
                });
            }
            catch(Exception e)
            {
                thread = null;
            }
        } else
        {
            thread = new SourceThread(this, oc, idx);
            if(tracks[idx].getFormat() instanceof AudioFormat)
                thread.useAudioPriority();
            else
                thread.useVideoPriority();
        }
        if(thread == null)
            tracks[idx].setEnabled(false);
        return thread;
    }

    public void doFailedRealize()
    {
        parser.stop();
        parser.close();
    }

    public void abortRealize()
    {
        parser.stop();
        parser.close();
    }

    public boolean doPrefetch()
    {
        super.doPrefetch();
        return true;
    }

    public void doFailedPrefetch()
    {
    }

    public void abortPrefetch()
    {
        doStop();
    }

    public void doStart()
    {
        lastSystemTime = systemTimeBase.getNanoseconds();
        originSystemTime = currentSystemTime;
        rtpOffsetInvalid = true;
        super.doStart();
        try
        {
            parser.start();
        }
        catch(IOException e) { }
        for(int i = 0; i < loops.length; i++)
            if(tracks[i].isEnabled() && (loops[i] != null || (loops[i] = createSourceThread(i)) != null))
                loops[i].start();

        started = true;
    }

    public void doStop()
    {
        started = false;
    }

    public void pause()
    {
        synchronized(resetSync)
        {
            for(int i = 0; i < loops.length; i++)
                if(tracks[i].isEnabled() && loops[i] != null && !loops[i].resetted)
                    loops[i].pause();

            parser.stop();
        }
    }

    public void doDealloc()
    {
    }

    public void doClose()
    {
        parser.close();
        if(tracks == null)
            return;
        for(int i = 0; i < tracks.length; i++)
            if(loops[i] != null)
                loops[i].kill();

        if(rtpMapperUpdatable != null)
        {
            RTPTimeBase.returnMapperUpdatable(rtpMapperUpdatable);
            rtpMapperUpdatable = null;
        }
    }

    public void reset()
    {
        synchronized(resetSync)
        {
            super.reset();
            for(int i = 0; i < loops.length; i++)
                if(tracks[i].isEnabled() && (loops[i] != null || (loops[i] = createSourceThread(i)) != null))
                {
                    loops[i].resetted = true;
                    loops[i].start();
                }

        }
    }

    public String[] getOutputConnectorNames()
    {
        return connectorNames;
    }

    public Time getDuration()
    {
        return parser.getDuration();
    }

    public Time setPosition(Time when, int rounding)
    {
        Time t = parser.setPosition(when, rounding);
        if(lastPositionSet.getNanoseconds() == t.getNanoseconds())
            lastPositionSet = new Time(t.getNanoseconds() + 1L);
        else
            lastPositionSet = t;
        return t;
    }

    public boolean isPositionable()
    {
        return parser.isPositionable();
    }

    public boolean isRandomAccess()
    {
        return parser.isRandomAccess();
    }

    public Object[] getControls()
    {
        return parser.getControls();
    }

    public Object getControl(String s)
    {
        return parser.getControl(s);
    }

    public Demultiplexer getDemultiplexer()
    {
        return parser;
    }

    public void setFormat(Connector connector1, Format format1)
    {
    }

    public void process()
    {
    }

    public long getBitsRead()
    {
        return bitsRead;
    }

    public void resetBitsRead()
    {
        bitsRead = 0L;
    }

    boolean readHasBlocked()
    {
        if(loops == null)
            return false;
        for(int i = 0; i < loops.length; i++)
            if(loops[i] != null && loops[i].readBlocked)
                return true;

        return false;
    }

    public void checkLatency()
    {
        if(latencyTrack > -1)
        {
            if(tracks[latencyTrack].isEnabled() && loops[latencyTrack] != null)
            {
                loops[latencyTrack].checkLatency = true;
                return;
            }
            latencyTrack = -1;
        }
        for(int i = 0; i < tracks.length; i++)
        {
            if(!tracks[i].isEnabled())
                continue;
            latencyTrack = i;
            if(tracks[i].getFormat() instanceof VideoFormat)
                break;
        }

        if(latencyTrack > -1 && loops[latencyTrack] != null)
            loops[latencyTrack].checkLatency = true;
    }

    protected boolean checkAllPaused()
    {
        for(int i = 0; i < loops.length; i++)
            if(tracks[i].isEnabled() && loops[i] != null && !loops[i].isPaused())
                return false;

        return true;
    }

    PlaybackEngine engine;
    protected DataSource source;
    protected Demultiplexer parser;
    protected Track tracks[];
    protected SourceThread loops[];
    protected String connectorNames[];
    protected long bitsRead;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    Object resetSync;
    protected boolean started;
    protected SystemTimeBase systemTimeBase;
    protected long lastSystemTime;
    protected long originSystemTime;
    protected long currentSystemTime;
    protected Time lastPositionSet;
    RTPTimeBase rtpMapperUpdatable;
    RTPTimeBase rtpMapper;
    long currentRTPTime;
    long oldOffset;
    boolean rtpOffsetInvalid;
    String cname;
    public String errMsg;
    int latencyTrack;

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
