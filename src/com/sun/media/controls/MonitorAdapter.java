// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MonitorAdapter.java

package com.sun.media.controls;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.*;
import com.sun.media.util.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.*;
import javax.media.control.MonitorControl;
import javax.media.format.*;

// Referenced classes of package com.sun.media.controls:
//            MonitorThread, CreateWorkThreadAction

public class MonitorAdapter
    implements MonitorControl, Owned
{

    public MonitorAdapter(Format f, Object owner)
    {
        cc = null;
        enabled = false;
        closed = false;
        visualComponent = null;
        controlComponent = null;
        cbEnabled = null;
        format = null;
        inFrameRate = 0.0F;
        previewFrameRate = 30F;
        lastPreviewTime = 0L;
        previewInterval = 0x1fca055L;
        ml = null;
        rateMenu = null;
        mSecurity = new Method[1];
        clSecurity = new Class[1];
        argsSecurity = new Object[1][0];
        format = f;
        this.owner = owner;
    }

    protected boolean open()
    {
        try
        {
            if(format instanceof VideoFormat)
            {
                VideoFormat vf = (VideoFormat)format;
                cc = new VideoCodecChain(vf);
                inFrameRate = vf.getFrameRate();
                if(inFrameRate < 0.0F)
                    inFrameRate = 30F;
                inFrameRate = (float)(int)((double)(inFrameRate * 10F) + 0.5D) / 10F;
            } else
            if(format instanceof AudioFormat)
                cc = new AudioCodecChain((AudioFormat)format);
        }
        catch(UnsupportedFormatException e)
        {
            Log.warning("Failed to initialize the monitor control: " + e);
            return false;
        }
        if(cc == null)
            return false;
        bufferQ = new CircularBuffer(2);
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
                Constructor cons = CreateWorkThreadAction.cons;
                loopThread = (MonitorThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.controls.MonitorThread.class, com.sun.media.controls.MonitorAdapter.class, this
                    })
                });
            }
            catch(Exception e) { }
        else
            loopThread = new MonitorThread(this);
        return true;
    }

    public Object getOwner()
    {
        return owner;
    }

    public boolean setEnabled(boolean on)
    {
        if(on)
        {
            if(cc == null)
            {
                if(!open())
                    return false;
            } else
            {
                cc.reset();
            }
            if(!cc.prefetch())
                return false;
            synchronized(bufferQ)
            {
                for(; bufferQ.canRead(); bufferQ.readReport())
                    bufferQ.read();

            }
            enabled = true;
            loopThread.start();
        } else
        if(!on && cc != null)
        {
            loopThread.pause();
            synchronized(bufferQ)
            {
                enabled = false;
                bufferQ.notifyAll();
            }
            cc.deallocate();
        }
        return enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void reset()
    {
        if(cc != null)
            cc.reset();
    }

    public void close()
    {
        if(cc == null)
            return;
        loopThread.kill();
        synchronized(bufferQ)
        {
            closed = true;
            bufferQ.notifyAll();
        }
        cc.close();
        cc = null;
    }

    public void process(Buffer input)
    {
        if(input == null || previewFrameRate <= 0.0F || format == null || input.isEOM() || input.isDiscard() || (input.getFlags() & 0x200) != 0)
            return;
        if(!format.matches(input.getFormat()))
            return;
        Buffer buffer = null;
        synchronized(bufferQ)
        {
            while(!bufferQ.canWrite() && enabled && !closed) 
                try
                {
                    bufferQ.wait();
                }
                catch(Exception e) { }
            if(!enabled || closed)
                return;
            buffer = bufferQ.getEmptyBuffer();
        }
        buffer.setData(copyData(input.getData()));
        buffer.setFlags(input.getFlags());
        buffer.setFormat(input.getFormat());
        buffer.setSequenceNumber(input.getSequenceNumber());
        buffer.setHeader(input.getHeader());
        buffer.setLength(input.getLength());
        buffer.setOffset(input.getOffset());
        buffer.setTimeStamp(input.getTimeStamp());
        synchronized(bufferQ)
        {
            bufferQ.writeReport();
            bufferQ.notifyAll();
        }
    }

    public boolean doProcess()
    {
        Buffer buffer;
        synchronized(bufferQ)
        {
            while(!bufferQ.canRead() && enabled && !closed) 
                try
                {
                    bufferQ.wait();
                }
                catch(Exception e) { }
            if(closed)
            {
                boolean flag = false;
                return flag;
            }
            if(!enabled)
            {
                boolean flag1 = true;
                return flag1;
            }
            buffer = bufferQ.read();
        }
        boolean toDisplay = false;
        if(buffer.getFormat() instanceof AudioFormat)
        {
            toDisplay = true;
        } else
        {
            long time = buffer.getTimeStamp();
            if(time >= lastPreviewTime + previewInterval || time <= lastPreviewTime)
            {
                if(mpegVideo.matches(format))
                {
                    byte payload[] = (byte[])buffer.getData();
                    int offset = buffer.getOffset();
                    int ptype = payload[offset + 2] & 7;
                    if(ptype == 1)
                    {
                        lastPreviewTime = time;
                        toDisplay = true;
                    }
                } else
                {
                    lastPreviewTime = time;
                    toDisplay = true;
                }
            } else
            {
                toDisplay = false;
            }
        }
        cc.process(buffer, toDisplay);
        synchronized(bufferQ)
        {
            bufferQ.readReport();
            bufferQ.notifyAll();
        }
        return true;
    }

    private Object copyData(Object in)
    {
        if(in instanceof byte[])
        {
            byte out[] = new byte[((byte[])in).length];
            System.arraycopy(in, 0, out, 0, out.length);
            return out;
        }
        if(in instanceof short[])
        {
            short out[] = new short[((short[])in).length];
            System.arraycopy(in, 0, out, 0, out.length);
            return out;
        }
        if(in instanceof int[])
        {
            int out[] = new int[((int[])in).length];
            System.arraycopy(in, 0, out, 0, out.length);
            return out;
        } else
        {
            return in;
        }
    }

    public float setPreviewFrameRate(float value)
    {
        if(value > inFrameRate)
            value = inFrameRate;
        previewFrameRate = value;
        previewInterval = (long)(1000000000D / (double)value);
        return value;
    }

    public Component getControlComponent()
    {
        if(controlComponent != null)
            return controlComponent;
        if(cc == null && !open())
            return null;
        controlComponent = cc.getControlComponent();
        if((format instanceof AudioFormat) && controlComponent != null)
        {
            Container controlPanel = new Panel();
            controlPanel.setLayout(new BorderLayout());
            cbEnabled = new Checkbox("Monitor Audio");
            controlPanel.add("West", cbEnabled);
            controlPanel.add("Center", controlComponent);
            controlComponent = controlPanel;
            controlPanel.setBackground(Color.lightGray);
        }
        if((format instanceof VideoFormat) && controlComponent != null)
        {
            Container controlPanel = new Panel();
            controlPanel.setLayout(new BorderLayout());
            cbEnabled = new Checkbox("Monitor Video");
            controlPanel.add("South", cbEnabled);
            controlPanel.add("Center", controlComponent);
            addPopupMenu(controlComponent);
            controlComponent = controlPanel;
            controlPanel.setBackground(Color.lightGray);
        }
        if(cbEnabled != null)
        {
            cbEnabled.setState(isEnabled());
            cbEnabled.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent ie)
                {
                    setEnabled(cbEnabled.getState());
                }

            }
);
        }
        return controlComponent;
    }

    private void addPopupMenu(Component visual)
    {
        visualComponent = visual;
        rateMenu = new PopupMenu("Monitor Rate");
        ActionListener rateSelect = new ActionListener() {

            public void actionPerformed(ActionEvent ae)
            {
                String action = ae.getActionCommand();
                int space = action.indexOf(" ");
                String rateString = action.substring(0, space);
                try
                {
                    int rate = Integer.parseInt(rateString);
                    setPreviewFrameRate(rate);
                }
                catch(Throwable t)
                {
                    if(t instanceof ThreadDeath)
                        throw (ThreadDeath)t;
                }
            }

        }
;
        visual.add(rateMenu);
        int lastAdded = 0;
        for(int i = 0; i < frameRates.length; i++)
            if((float)frameRates[i] < inFrameRate)
            {
                MenuItem mi = new MenuItem(frameRates[i] + " fps");
                rateMenu.add(mi);
                mi.addActionListener(rateSelect);
                lastAdded = frameRates[i];
            }

        if((float)lastAdded < inFrameRate)
        {
            MenuItem mi = new MenuItem(inFrameRate + " fps");
            rateMenu.add(mi);
            mi.addActionListener(rateSelect);
        }
        visual.addMouseListener(ml = new MouseAdapter() {

            public void mousePressed(MouseEvent me)
            {
                if(me.isPopupTrigger())
                    rateMenu.show(visualComponent, me.getX(), me.getY());
            }

            public void mouseReleased(MouseEvent me)
            {
                if(me.isPopupTrigger())
                    rateMenu.show(visualComponent, me.getX(), me.getY());
            }

            public void mouseClicked(MouseEvent me)
            {
                if(me.isPopupTrigger())
                    rateMenu.show(visualComponent, me.getX(), me.getY());
            }

        }
);
    }

    public void finalize()
    {
        if(visualComponent != null)
        {
            visualComponent.remove(rateMenu);
            visualComponent.removeMouseListener(ml);
        }
    }

    protected CodecChain cc;
    protected boolean enabled;
    protected boolean closed;
    protected Component visualComponent;
    protected Component controlComponent;
    protected Checkbox cbEnabled;
    protected Format format;
    protected float inFrameRate;
    protected float previewFrameRate;
    protected long lastPreviewTime;
    protected long previewInterval;
    protected MouseListener ml;
    protected PopupMenu rateMenu;
    protected LoopThread loopThread;
    protected int frameRates[] = {
        0, 1, 2, 5, 7, 10, 15, 20, 30, 60, 
        90
    };
    CircularBuffer bufferQ;
    Object owner;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method mSecurity[];
    private Class clSecurity[];
    private Object argsSecurity[][];
    static VideoFormat mpegVideo = new VideoFormat("mpeg/rtp");

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
