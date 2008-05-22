// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProgressSlider.java

package com.sun.media.ui;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.util.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.media.*;

// Referenced classes of package com.sun.media.ui:
//            BasicComp, ToolTip, DefaultControlPanel

public class ProgressSlider extends BasicComp
    implements MouseListener, MouseMotionListener, Runnable, ComponentListener, ControllerListener
{

    public ProgressSlider(String label, DefaultControlPanel cp, Player p)
    {
        super(label);
        leftBorder = 0;
        rightBorder = 0;
        timer = null;
        justSeeked = false;
        stopTimer = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        toolTip = null;
        progressCaching = 1.0D;
        resetMediaTime = false;
        disposeLock = new Object();
        syncStop = new Object();
        player = p;
        controlPanel = cp;
        imageGrabber = BasicComp.fetchImage("grabber.gif");
        imageGrabberDown = BasicComp.fetchImage("grabber-pressed.gif");
        grabberWidth = imageGrabber.getWidth(this);
        grabberHeight = imageGrabber.getHeight(this);
        leftBorder = grabberWidth / 2;
        rightBorder = leftBorder;
        addMouseListener(this);
        addMouseMotionListener(this);
        grabberPosition = 0;
        grabbed = false;
        entered = false;
        super.height = 18;
        super.width = 20;
        sliderWidth = super.width - leftBorder - rightBorder;
        addComponentListener(this);
        player.addControllerListener(this);
    }

    public void addNotify()
    {
        super.addNotify();
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
                ProgressSlider slider = this;
                Constructor cons = jdk12CreateThreadRunnableAction.cons;
                timer = (MediaThread)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        com.sun.media.util.MediaThread.class, this
                    })
                });
                timer.setName("Progress Slider thread");
                cons = jdk12PriorityAction.cons;
                jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        timer, new Integer(getControlPriority())
                    })
                });
                stopTimer = false;
                timer.start();
            }
            catch(Exception e) { }
        } else
        {
            timer = new MediaThread(this);
            timer.setName("Progress Slider thread");
            timer.useControlPriority();
            stopTimer = false;
            timer.start();
        }
    }

    public void removeNotify()
    {
        if(timer != null)
            synchronized(syncStop)
            {
                stopTimer = true;
                timer = null;
            }
        synchronized(disposeLock)
        {
            if(toolTip != null)
                toolTip.setVisible(false);
        }
        super.removeNotify();
    }

    public synchronized void dispose()
    {
        synchronized(syncStop)
        {
            if(timer != null)
                stopTimer = true;
        }
        removeMouseListener(this);
        removeMouseMotionListener(this);
        removeComponentListener(this);
        synchronized(disposeLock)
        {
            if(toolTip != null)
            {
                toolTip.dispose();
                toolTip = null;
            }
        }
        timer = null;
        player = null;
    }

    public void run()
    {
        int counter = 0;
        int pausecnt = -1;
        boolean doUpdate = true;
        while(!stopTimer) 
            try
            {
                if(player != null && player.getState() == 600)
                {
                    doUpdate = true;
                    pausecnt = -1;
                } else
                if(player != null && pausecnt < 5)
                {
                    pausecnt++;
                    doUpdate = true;
                } else
                if(resetMediaTime)
                {
                    doUpdate = true;
                    resetMediaTime = false;
                } else
                {
                    doUpdate = false;
                }
                try
                {
                    if(doUpdate)
                    {
                        long nanoDuration = player.getDuration().getNanoseconds();
                        if(nanoDuration > 0L)
                        {
                            long nanoTime = player.getMediaNanoseconds();
                            seek((float)nanoTime / (float)nanoDuration);
                            if(!grabbed)
                                updateToolTip(nanoTime);
                        }
                    }
                }
                catch(Exception e) { }
                int sleepTime = isEnabled() ? 200 : 1000;
                try
                {
                    Thread.sleep(sleepTime);
                }
                catch(Exception e) { }
                if(++counter == 1000 / sleepTime)
                {
                    counter = 0;
                    controlPanel.update();
                }
                if(justSeeked)
                {
                    justSeeked = false;
                    try
                    {
                        Thread.sleep(1000L);
                    }
                    catch(Exception e) { }
                }
            }
            catch(Exception e) { }
    }

    public void paint(Graphics g)
    {
        if(isEnabled())
        {
            int y = super.height / 2 - 2;
            int grabberY = super.height / 2 - grabberHeight / 2;
            g.setColor(getBackground().darker());
            g.drawRect(leftBorder, y, sliderWidth, 3);
            g.setColor(getBackground());
            int downloadCredit = grabberWidth;
            g.draw3DRect(leftBorder, y, (int)((double)(sliderWidth - downloadCredit) * progressCaching + (double)downloadCredit), 3, false);
            if(grabbed || entered)
                g.drawImage(imageGrabberDown, (grabberPosition + leftBorder) - grabberWidth / 2, grabberY, this);
            else
                g.drawImage(imageGrabber, (grabberPosition + leftBorder) - grabberWidth / 2, grabberY, this);
        } else
        if(player != null)
        {
            String strTime = formatTime(player.getMediaNanoseconds());
            java.awt.Font font = getFont();
            g.setFont(font);
            FontMetrics fontMetrics = getFontMetrics(font);
            g.drawString(strTime, 2, 2 + fontMetrics.getAscent());
        }
    }

    private void sliderSeek(float fraction)
    {
        if(player == null)
            return;
        long value = (long)(fraction * (float)player.getDuration().getNanoseconds());
        justSeeked = true;
        if(value >= 0L)
        {
            player.setMediaTime(new Time(value));
            controlPanel.resetPauseCount();
            controlPanel.update();
        }
    }

    public void seek(float fraction)
    {
        if(justSeeked)
            return;
        if(!grabbed)
        {
            int newPosition = (int)(fraction * (float)sliderWidth);
            if(newPosition > sliderWidth)
                newPosition = sliderWidth;
            if(newPosition < 0)
                newPosition = 0;
            if(grabberPosition != newPosition || !isEnabled())
            {
                grabberPosition = newPosition;
                repaint();
            }
        }
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(20, super.height);
    }

    public float sliderToSeek(int x)
    {
        float s = (float)x / (float)sliderWidth;
        return s;
    }

    public int mouseToSlider(int x)
    {
        if(x < leftBorder)
            x = leftBorder;
        if(x > super.width - rightBorder)
            x = super.width - rightBorder;
        x -= leftBorder;
        return x;
    }

    public void mousePressed(MouseEvent me)
    {
        if(!isEnabled())
        {
            return;
        } else
        {
            grabbed = true;
            grabberPosition = mouseToSlider(me.getX());
            repaint();
            return;
        }
    }

    public synchronized void mouseReleased(MouseEvent me)
    {
        if(!isEnabled())
            return;
        grabbed = false;
        grabberPosition = mouseToSlider(me.getX());
        float seek = sliderToSeek(grabberPosition);
        sliderSeek(seek);
        if(toolTip != null && !entered)
        {
            toolTip.dispose();
            toolTip = null;
        }
        repaint();
    }

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public synchronized void mouseEntered(MouseEvent me)
    {
        entered = true;
        if(toolTip == null && isEnabled() && player != null)
        {
            toolTip = new ToolTip("time/duration");
            updateToolTip(player.getMediaNanoseconds());
            if(isShowing())
            {
                Point pointScreen = getLocationOnScreen();
                pointScreen.y += super.height + 4;
                toolTip.setLocation(pointScreen);
                toolTip.show();
            }
        }
        repaint();
    }

    public synchronized void mouseExited(MouseEvent me)
    {
        if(toolTip != null && !grabbed)
        {
            toolTip.dispose();
            toolTip = null;
        }
        if(!isEnabled())
        {
            return;
        } else
        {
            entered = false;
            repaint();
            return;
        }
    }

    public synchronized void mouseMoved(MouseEvent me)
    {
        if(toolTip != null && isShowing())
        {
            Dimension dim = toolTip.getSize();
            Point pointScreen = getLocationOnScreen();
            pointScreen.x += me.getX() - dim.width - 2;
            pointScreen.y += me.getY();
        }
    }

    public synchronized void mouseDragged(MouseEvent me)
    {
        if(!isEnabled() || player == null)
            return;
        int newPosition = mouseToSlider(me.getX());
        if(newPosition != grabberPosition)
        {
            grabberPosition = newPosition;
            float seek = sliderToSeek(grabberPosition);
            if(player.getState() != 600)
                sliderSeek(seek);
            long value = (long)(seek * (float)player.getDuration().getNanoseconds());
            updateToolTip(value);
            repaint();
        }
    }

    public void componentResized(ComponentEvent event)
    {
        Dimension dim = getSize();
        if(dim.width - leftBorder - rightBorder < 1)
        {
            return;
        } else
        {
            grabberPosition = (int)((float)grabberPosition * ((float)(dim.width - leftBorder - rightBorder) / (float)(super.width - leftBorder - rightBorder)));
            super.width = dim.width;
            sliderWidth = super.width - leftBorder - rightBorder;
            return;
        }
    }

    public void componentMoved(ComponentEvent componentevent)
    {
    }

    public void componentShown(ComponentEvent componentevent)
    {
    }

    public void componentHidden(ComponentEvent componentevent)
    {
    }

    public synchronized void controllerUpdate(ControllerEvent event)
    {
        if(event instanceof CachingControlEvent)
        {
            CachingControl cachingControl = ((CachingControlEvent)event).getCachingControl();
            long length = cachingControl.getContentLength();
            long progress = cachingControl.getContentProgress();
            progressCaching = (double)progress / (double)length;
            repaint();
        } else
        if(event instanceof MediaTimeSetEvent)
            resetMediaTime = true;
    }

    private String formatTime(Time time)
    {
        String strTime = new String("<unknown>");
        if(time == null || time == Time.TIME_UNKNOWN || time == Duration.DURATION_UNKNOWN)
        {
            return strTime;
        } else
        {
            long nano = time.getNanoseconds();
            strTime = formatTime(nano);
            return strTime;
        }
    }

    private String formatTime(long nanoSeconds)
    {
        int seconds = (int)(nanoSeconds / 0x3b9aca00L);
        int hours = seconds / 3600;
        int minutes = (seconds - hours * 3600) / 60;
        seconds = seconds - hours * 3600 - minutes * 60;
        nanoSeconds = (nanoSeconds % 0x3b9aca00L) / 0x989680L;
        int hours10 = hours / 10;
        hours %= 10;
        int minutes10 = minutes / 10;
        minutes %= 10;
        int seconds10 = seconds / 10;
        seconds %= 10;
        long nano10 = nanoSeconds / 10L;
        nanoSeconds %= 10L;
        String strTime = new String("" + hours10 + hours + ":" + minutes10 + minutes + ":" + seconds10 + seconds + "." + nano10 + nanoSeconds);
        return strTime;
    }

    public void updateToolTip(long nanoMedia)
    {
        if(toolTip == null || player == null)
        {
            return;
        } else
        {
            Time timeDuration = player.getDuration();
            String strTool = new String(formatTime(nanoMedia) + " / " + formatTime(timeDuration));
            toolTip.setText(strTool);
            return;
        }
    }

    Image imageGrabber;
    Image imageGrabberDown;
    int grabberWidth;
    int grabberHeight;
    boolean grabbed;
    boolean entered;
    int grabberPosition;
    int leftBorder;
    int rightBorder;
    int sliderWidth;
    MediaThread timer;
    protected boolean justSeeked;
    protected boolean stopTimer;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    private Player player;
    private DefaultControlPanel controlPanel;
    private ToolTip toolTip;
    private double progressCaching;
    private boolean resetMediaTime;
    Object disposeLock;
    Object syncStop;

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
