// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PlayerWindow.java

package com.sun.media.ui;

import com.sun.media.util.JMFI18N;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import javax.media.*;
import javax.media.control.MonitorControl;
import javax.media.format.FormatChangeEvent;

public class PlayerWindow extends Frame
    implements ControllerListener
{

    public PlayerWindow(Player player)
    {
        this(player, JMFI18N.getResource("mediaplayer.windowtitle"), true, true);
    }

    public PlayerWindow(Player player, String title)
    {
        this(player, title, true, true);
    }

    public PlayerWindow(Player player, String title, boolean autoStart)
    {
        this(player, title, autoStart, true);
    }

    public PlayerWindow(Player player, String title, boolean autoStart, boolean autoLoop)
    {
        super(title);
        controlComp = null;
        visualComp = null;
        zoomMenu = null;
        windowCreated = false;
        newVideo = true;
        panelResized = false;
        this.autoStart = true;
        this.autoLoop = true;
        progressBar = null;
        playerLock = new Integer(1);
        this.autoStart = autoStart;
        this.autoLoop = autoLoop;
        this.player = player;
        setLayout(new BorderLayout());
        framePanel = new Panel();
        framePanel.setLayout(null);
        add(framePanel, "Center");
        insets = getInsets();
        setSize(insets.left + insets.right + 320, insets.top + insets.bottom + 30);
        setVisible(true);
        addWindowListener(wl = new WindowAdapter() {

            public void windowClosing(WindowEvent we)
            {
                killThePlayer();
            }

        }
);
        framePanel.addComponentListener(fcl = new ComponentAdapter() {

            public void componentResized(ComponentEvent ce)
            {
                panelResized = true;
                doResize();
            }

        }
);
        addComponentListener(fcl = new ComponentAdapter() {

            public void componentResized(ComponentEvent ce)
            {
                insets = getInsets();
                Dimension dim = getSize();
                framePanel.setSize(dim.width - insets.left - insets.right, dim.height - insets.top - insets.bottom);
            }

        }
);
        player.addControllerListener(this);
        player.realize();
    }

    void sleep(long time)
    {
        try
        {
            Thread.currentThread();
            Thread.sleep(time);
        }
        catch(Exception e) { }
    }

    public void addNotify()
    {
        super.addNotify();
        windowCreated = true;
        invalidate();
    }

    public void doResize()
    {
        Dimension d = framePanel.getSize();
        int videoHeight = d.height;
        if(controlComp != null)
        {
            videoHeight -= controlComp.getPreferredSize().height;
            if(videoHeight < 2)
                videoHeight = 2;
            if(d.width < 80)
                d.width = 80;
            controlComp.setBounds(0, videoHeight, d.width, controlComp.getPreferredSize().height);
            controlComp.invalidate();
        }
        if(visualComp != null)
            visualComp.setBounds(0, 0, d.width, videoHeight);
        framePanel.validate();
    }

    public void killThePlayer()
    {
        synchronized(playerLock)
        {
            if(visualComp != null)
            {
                framePanel.remove(visualComp);
                visualComp = null;
            }
            if(controlComp != null)
            {
                framePanel.remove(controlComp);
                controlComp = null;
            }
            if(player != null)
                player.close();
        }
    }

    public void controllerUpdate(ControllerEvent ce)
    {
        synchronized(playerLock)
        {
            if(ce instanceof RealizeCompleteEvent)
            {
                int width = 320;
                int height = 0;
                insets = getInsets();
                if(progressBar != null)
                    framePanel.remove(progressBar);
                if((visualComp = player.getVisualComponent()) != null)
                {
                    width = visualComp.getPreferredSize().width;
                    height = visualComp.getPreferredSize().height;
                    framePanel.add(visualComp);
                    visualComp.setBounds(0, 0, width, height);
                    addPopupMenu(visualComp);
                } else
                {
                    MonitorControl mc = (MonitorControl)player.getControl("javax.media.control.MonitorControl");
                    if(mc != null)
                    {
                        Control controls[] = player.getControls();
                        Panel mainPanel = new Panel(new BorderLayout());
                        Panel currentPanel = mainPanel;
                        for(int i = 0; i < controls.length; i++)
                            if(controls[i] instanceof MonitorControl)
                            {
                                mc = (MonitorControl)controls[i];
                                mc.setEnabled(true);
                                if(mc.getControlComponent() != null)
                                {
                                    currentPanel.add("North", mc.getControlComponent());
                                    Panel newPanel = new Panel(new BorderLayout());
                                    currentPanel.add("South", newPanel);
                                    currentPanel = newPanel;
                                }
                            }

                        visualComp = mainPanel;
                        width = visualComp.getPreferredSize().width;
                        height = visualComp.getPreferredSize().height;
                        framePanel.add(visualComp);
                        visualComp.setBounds(0, 0, width, height);
                    }
                }
                if((controlComp = player.getControlPanelComponent()) != null)
                {
                    int prefHeight = controlComp.getPreferredSize().height;
                    framePanel.add(controlComp);
                    controlComp.setBounds(0, height, width, prefHeight);
                    height += prefHeight;
                }
                setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);
                if(autoStart)
                    player.prefetch();
            } else
            if(ce instanceof PrefetchCompleteEvent)
            {
                if(visualComp != null)
                {
                    Dimension vSize = visualComp.getPreferredSize();
                    if(controlComp != null)
                        vSize.height += controlComp.getPreferredSize().height;
                    panelResized = false;
                    setSize(vSize.width + insets.left + insets.right, vSize.height + insets.top + insets.bottom);
                    for(int waited = 0; !panelResized && waited < 2000;)
                        try
                        {
                            waited += 50;
                            Thread.currentThread();
                            Thread.sleep(50L);
                            Thread.currentThread();
                            Thread.yield();
                        }
                        catch(Exception e) { }

                } else
                {
                    int height = 1;
                    if(controlComp != null)
                        height = controlComp.getPreferredSize().height;
                    setSize(320 + insets.left + insets.right, height + insets.top + insets.bottom);
                }
                if(autoStart && player != null && player.getTargetState() != 600)
                    player.start();
            } else
            if(ce instanceof EndOfMediaEvent)
            {
                if(autoLoop)
                {
                    player.setMediaTime(new Time(0L));
                    player.start();
                }
            } else
            if(ce instanceof ControllerErrorEvent)
            {
                System.err.println("Received controller error");
                killThePlayer();
                dispose();
            } else
            if(ce instanceof SizeChangeEvent)
            {
                if(framePanel != null)
                {
                    SizeChangeEvent sce = (SizeChangeEvent)ce;
                    int nooWidth = sce.getWidth();
                    int nooHeight = sce.getHeight();
                    if(controlComp != null)
                        nooHeight += controlComp.getPreferredSize().height;
                    if(framePanel.getSize().width != nooWidth || framePanel.getSize().height != nooHeight)
                        setSize(nooWidth + insets.left + insets.right, nooHeight + insets.top + insets.bottom);
                    else
                        doResize();
                    if(controlComp != null)
                        controlComp.invalidate();
                }
            } else
            if(ce instanceof FormatChangeEvent)
            {
                Dimension vSize = new Dimension(320, 0);
                Component oldVisualComp = visualComp;
                if((visualComp = player.getVisualComponent()) != null && oldVisualComp != visualComp)
                {
                    if(oldVisualComp != null && zoomMenu != null)
                        oldVisualComp.remove(zoomMenu);
                    framePanel.remove(oldVisualComp);
                    vSize = visualComp.getPreferredSize();
                    framePanel.add(visualComp);
                    visualComp.setBounds(0, 0, vSize.width, vSize.height);
                    addPopupMenu(visualComp);
                }
                Component oldComp = controlComp;
                if((controlComp = player.getControlPanelComponent()) != null && oldComp != controlComp)
                {
                    framePanel.remove(oldComp);
                    framePanel.add(controlComp);
                    if(controlComp != null)
                    {
                        int prefHeight = controlComp.getPreferredSize().height;
                        controlComp.setBounds(0, vSize.height, vSize.width, prefHeight);
                    }
                }
            } else
            if(ce instanceof ControllerClosedEvent)
            {
                if(visualComp != null)
                {
                    if(zoomMenu != null)
                        visualComp.remove(zoomMenu);
                    visualComp.removeMouseListener(ml);
                }
                removeWindowListener(wl);
                removeComponentListener(cl);
                if(framePanel != null)
                    framePanel.removeAll();
                player = null;
                visualComp = null;
                controlComp = null;
                sleep(200L);
                dispose();
            } else
            if(ce instanceof CachingControlEvent)
            {
                CachingControl cc = ((CachingControlEvent)ce).getCachingControl();
                if(cc != null && progressBar == null)
                {
                    progressBar = cc.getControlComponent();
                    if(progressBar == null)
                        progressBar = cc.getProgressBarComponent();
                    if(progressBar != null)
                    {
                        framePanel.add(progressBar);
                        Dimension prefSize = progressBar.getPreferredSize();
                        progressBar.setBounds(0, 0, prefSize.width, prefSize.height);
                        insets = getInsets();
                        framePanel.setSize(prefSize.width, prefSize.height);
                        setSize(insets.left + insets.right + prefSize.width, insets.top + insets.bottom + prefSize.height);
                    }
                }
            }
        }
    }

    public void zoomTo(float z)
    {
        if(visualComp != null)
        {
            insets = getInsets();
            Dimension d = visualComp.getPreferredSize();
            d.width = (int)((float)d.width * z);
            d.height = (int)((float)d.height * z);
            if(controlComp != null)
                d.height += controlComp.getPreferredSize().height;
            setSize(d.width + insets.left + insets.right, d.height + insets.top + insets.bottom);
        }
    }

    private void addPopupMenu(Component visual)
    {
        zoomMenu = new PopupMenu(MENU_ZOOM);
        ActionListener zoomSelect = new ActionListener() {

            public void actionPerformed(ActionEvent ae)
            {
                String action = ae.getActionCommand();
                if(action.equals(PlayerWindow.MENU_ZOOM_1_2))
                    zoomTo(0.5F);
                else
                if(action.equals(PlayerWindow.MENU_ZOOM_1_1))
                    zoomTo(1.0F);
                else
                if(action.equals(PlayerWindow.MENU_ZOOM_2_1))
                    zoomTo(2.0F);
                else
                if(action.equals(PlayerWindow.MENU_ZOOM_4_1))
                    zoomTo(4F);
            }

        }
;
        visual.add(zoomMenu);
        MenuItem mi = new MenuItem(MENU_ZOOM_1_2);
        zoomMenu.add(mi);
        mi.addActionListener(zoomSelect);
        mi = new MenuItem(MENU_ZOOM_1_1);
        zoomMenu.add(mi);
        mi.addActionListener(zoomSelect);
        mi = new MenuItem(MENU_ZOOM_2_1);
        zoomMenu.add(mi);
        mi.addActionListener(zoomSelect);
        mi = new MenuItem(MENU_ZOOM_4_1);
        zoomMenu.add(mi);
        mi.addActionListener(zoomSelect);
        visual.addMouseListener(ml = new MouseAdapter() {

            public void mousePressed(MouseEvent me)
            {
                if(me.isPopupTrigger())
                    zoomMenu.show(visualComp, me.getX(), me.getY());
            }

            public void mouseReleased(MouseEvent me)
            {
                if(me.isPopupTrigger())
                    zoomMenu.show(visualComp, me.getX(), me.getY());
            }

            public void mouseClicked(MouseEvent me)
            {
                if(me.isPopupTrigger())
                    zoomMenu.show(visualComp, me.getX(), me.getY());
            }

        }
);
    }

    private static final String MENU_ZOOM_1_2 = JMFI18N.getResource("mediaplayer.zoom.1:2");
    private static final String MENU_ZOOM_1_1 = JMFI18N.getResource("mediaplayer.zoom.1:1");
    private static final String MENU_ZOOM_2_1 = JMFI18N.getResource("mediaplayer.zoom.2:1");
    private static final String MENU_ZOOM_4_1 = JMFI18N.getResource("mediaplayer.zoom.4:1");
    private static final String MENU_ZOOM = JMFI18N.getResource("mediaplayer.menu.zoom");
    Player player;
    Panel framePanel;
    ComponentListener cl;
    ComponentListener fcl;
    WindowListener wl;
    MouseListener ml;
    Component controlComp;
    Component visualComp;
    Insets insets;
    PopupMenu zoomMenu;
    boolean windowCreated;
    boolean newVideo;
    boolean panelResized;
    boolean autoStart;
    boolean autoLoop;
    Component progressBar;
    private Integer playerLock;





}
