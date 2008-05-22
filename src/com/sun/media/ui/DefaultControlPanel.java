// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DefaultControlPanel.java

package com.sun.media.ui;

import com.sun.media.controls.ProgressControl;
import com.sun.media.controls.SliderRegionControl;
import com.sun.media.util.JMFI18N;
import java.awt.*;
import java.awt.event.*;
import java.awt.peer.LightweightPeer;
import java.util.Vector;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.ui:
//            BufferedPanelLight, BufferedPanel, TransparentPanel, ButtonComp, 
//            AudioButton, ProgressSlider, PropertySheet, BasicComp, 
//            GainControlComponent

public class DefaultControlPanel extends BufferedPanelLight
    implements ActionListener, ItemListener, ControllerListener, GainChangeListener, ComponentListener
{

    public DefaultControlPanel(Player player)
    {
        parentFrame = null;
        container = null;
        boolAdded = false;
        buttonPlay = null;
        buttonStepBack = null;
        buttonStepFwd = null;
        buttonAudio = null;
        buttonMedia = null;
        progressSlider = null;
        menuItemCheck = null;
        menuPopup = null;
        wl = null;
        firstTime = true;
        started = false;
        localLock = new Integer(0);
        audioControls = null;
        propsSheet = null;
        controlFrame = null;
        progressControl = null;
        gainControl = null;
        regionControl = null;
        urlName = null;
        lFrameStep = 0L;
        menuRate_1_4 = null;
        menuRate_1_2 = null;
        menuRate_1_1 = null;
        menuRate_2_1 = null;
        menuRate_4_1 = null;
        menuRate_8_1 = null;
        vectorTracksAudio = new Vector();
        vectorTracksVideo = new Vector();
        pausecnt = -1;
        resetMediaTimeinPause = false;
        this.player = player;
        try
        {
            init();
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void addNotify()
    {
        boolean boolLightweight = true;
        if(!boolAdded)
        {
            for(Container containerParent = getParent(); containerParent != null && boolLightweight;)
            {
                java.awt.peer.ComponentPeer compPeer = containerParent.getPeer();
                containerParent = containerParent.getParent();
                if(containerParent == null)
                    break;
                if(compPeer != null && !(compPeer instanceof LightweightPeer))
                    boolLightweight = false;
            }

            if(container != null)
            {
                container.remove(panelLeft);
                container.remove(panelRight);
                container.remove(panelProgress);
                if(container != this)
                    remove(container);
            }
            if(boolLightweight)
            {
                container = this;
            } else
            {
                container = new BufferedPanel(new BorderLayout());
                container.setBackground(colorBackground);
                ((BufferedPanel)container).setBackgroundTile(BasicComp.fetchImage("texture3.gif"));
                add(container, "Center");
            }
            container.add(panelLeft, "West");
            container.add(panelRight, "East");
            container.add(panelProgress, "Center");
            boolAdded = true;
        }
        setVisible(true);
        super.addNotify();
        validate();
    }

    public void removeNotify()
    {
        super.removeNotify();
        if(boolAdded)
            boolAdded = false;
    }

    protected void removePlayButton()
    {
        panelLeft.remove(buttonPlay);
    }

    private void init()
        throws Exception
    {
        getPlayerControls();
        if(gainControl != null)
            gainControl.addGainChangeListener(this);
        setBackground(colorBackground);
        setLayout(new BorderLayout());
        addComponentListener(this);
        container = this;
        panelLeft = new TransparentPanel(new GridLayout(1, 0));
        container.add(panelLeft, "West");
        panelRight = new TransparentPanel(new GridLayout(1, 0));
        container.add(panelRight, "East");
        panelProgress = new TransparentPanel(new BorderLayout());
        container.add(panelProgress, "Center");
        buttonPlay = new ButtonComp("Play", "play.gif", "play-active.gif", "play-pressed.gif", "play-disabled.gif", "pause.gif", "pause-active.gif", "pause-pressed.gif", "pause-disabled.gif");
        buttonPlay.setActionListener(this);
        panelLeft.add(buttonPlay);
        if(controlFrame != null)
        {
            buttonStepBack = new ButtonComp("StepBack", "step-back.gif", "step-back-active.gif", "step-back-pressed.gif", "step-back-disabled.gif", "step-back.gif", "step-back-active.gif", "step-back-pressed.gif", "step-back-disabled.gif");
            buttonStepBack.setActionListener(this);
            buttonStepBack.setContMousePress(true);
            panelLeft.add(buttonStepBack);
            buttonStepFwd = new ButtonComp("StepForward", "step-fwd.gif", "step-fwd-active.gif", "step-fwd-pressed.gif", "step-fwd-disabled.gif", "step-fwd.gif", "step-fwd-active.gif", "step-fwd-pressed.gif", "step-fwd-disabled.gif");
            buttonStepFwd.setActionListener(this);
            buttonStepFwd.setContMousePress(true);
            panelLeft.add(buttonStepFwd);
        }
        if(gainControl != null)
        {
            buttonAudio = new AudioButton(gainControl);
            buttonAudio.setActionListener(this);
            panelRight.add(buttonAudio);
        }
        buttonMedia = new ButtonComp("Media", "media.gif", "media-active.gif", "media-pressed.gif", "media-disabled.gif", "media.gif", "media-active.gif", "media-pressed.gif", "media-disabled.gif");
        buttonMedia.setActionListener(this);
        panelRight.add(buttonMedia);
        progressSlider = new ProgressSlider("mediatime", this, player);
        progressSlider.setActionListener(this);
        panelProgress.add(progressSlider, "Center");
        Time duration = player.getDuration();
        if(duration == Duration.DURATION_UNBOUNDED || duration == Duration.DURATION_UNKNOWN)
            progressSlider.setEnabled(false);
        updateButtonState();
        validate();
        Dimension dim = getPreferredSize();
        setSize(dim);
        setVisible(true);
        setBackgroundTile(BasicComp.fetchImage("texture3.gif"));
        player.addControllerListener(this);
        javax.media.Control arrControls[] = player.getControls();
        int nCount = arrControls.length;
        for(int i = 0; i < nCount; i++)
            if(arrControls[i] instanceof TrackControl)
            {
                TrackControl trackControl = (TrackControl)arrControls[i];
                javax.media.Format format = trackControl.getFormat();
                if(format instanceof AudioFormat)
                    vectorTracksAudio.addElement(trackControl);
                else
                if(format instanceof VideoFormat)
                {
                    vectorTracksVideo.addElement(trackControl);
                    VideoFormat formatVideo = (VideoFormat)format;
                    float frameRate = formatVideo.getFrameRate();
                    lFrameStep = (long)(1E+009F / frameRate);
                }
            }

        menuPopup = new PopupMenu(MENU_MEDIA);
        buttonMedia.setPopupMenu(menuPopup);
        nCount = vectorTracksAudio.size();
        boolean aTrackAudioIconEnabled = false;
        if(nCount > 1)
        {
            for(int i = 0; i < nCount; i++)
            {
                TrackControl trackControl = (TrackControl)vectorTracksAudio.elementAt(i);
                boolean boolEnable = false;
                if(!aTrackAudioIconEnabled && trackControl.isEnabled())
                {
                    aTrackAudioIconEnabled = true;
                    boolEnable = true;
                }
                menuItemCheck = new CheckboxMenuItem(MENU_AUDIO + " " + i, boolEnable);
                muteAudioTrack(trackControl, !boolEnable);
                menuItemCheck.addItemListener(this);
                menuPopup.add(menuItemCheck);
            }

            menuPopup.addSeparator();
        }
        menuRate_1_4 = new CheckboxMenuItem(MENU_RATE_1_4, false);
        menuRate_1_4.addItemListener(this);
        menuPopup.add(menuRate_1_4);
        menuRate_1_2 = new CheckboxMenuItem(MENU_RATE_1_2, false);
        menuRate_1_2.addItemListener(this);
        menuPopup.add(menuRate_1_2);
        menuRate_1_1 = new CheckboxMenuItem(MENU_RATE_1_1, true);
        menuRate_1_1.addItemListener(this);
        menuPopup.add(menuRate_1_1);
        menuRate_2_1 = new CheckboxMenuItem(MENU_RATE_2_1, false);
        menuRate_2_1.addItemListener(this);
        menuPopup.add(menuRate_2_1);
        menuRate_4_1 = new CheckboxMenuItem(MENU_RATE_4_1, false);
        menuRate_4_1.addItemListener(this);
        menuPopup.add(menuRate_4_1);
        menuRate_8_1 = new CheckboxMenuItem(MENU_RATE_8_1, false);
        menuRate_8_1.addItemListener(this);
        menuPopup.add(menuRate_8_1);
    }

    private void updateButtonState()
    {
        if(player == null)
        {
            buttonPlay.setEnabled(false);
        } else
        {
            buttonPlay.setEnabled(true);
            if(player.getState() == 600)
                buttonPlay.setValue(true);
            else
                buttonPlay.setValue(false);
        }
    }

    public void minicleanUp()
    {
        synchronized(localLock)
        {
            firstTime = true;
        }
    }

    public void dispose()
    {
        synchronized(localLock)
        {
            if(player == null)
                return;
            if(propsSheet != null)
            {
                propsSheet.dispose();
                propsSheet = null;
            }
            if(progressSlider != null)
            {
                progressSlider.dispose();
                progressSlider = null;
            }
            if(audioControls != null)
            {
                remove(audioControls);
                audioControls = null;
            }
            if(buttonAudio != null)
            {
                buttonAudio.dispose();
                buttonAudio = null;
            }
            player = null;
            gainControl = null;
            controlFrame = null;
            if(parentFrame != null && wl != null)
            {
                parentFrame.removeWindowListener(wl);
                parentFrame = null;
                wl = null;
            }
            vectorTracksAudio.removeAllElements();
            vectorTracksVideo.removeAllElements();
            if(menuItemCheck != null)
                menuItemCheck.removeItemListener(this);
            menuRate_1_4.removeItemListener(this);
            menuRate_1_2.removeItemListener(this);
            menuRate_8_1.removeItemListener(this);
            menuRate_4_1.removeItemListener(this);
            menuRate_2_1.removeItemListener(this);
            menuRate_1_1.removeItemListener(this);
            buttonMedia.setPopupMenu(null);
        }
    }

    private void getPlayerControls()
    {
        if(player == null)
            return;
        gainControl = player.getGainControl();
        javax.media.Control control = player.getControl("javax.media.control.FramePositioningControl");
        if(control != null && (control instanceof FramePositioningControl))
            controlFrame = (FramePositioningControl)control;
    }

    public void actionPerformed(ActionEvent ae)
    {
        String command = ae.getActionCommand();
        if(command.equalsIgnoreCase(buttonPlay.getLabel()))
            playStop();
        if(buttonAudio != null && command.equalsIgnoreCase(buttonAudio.getLabel()))
            audioMute();
        else
        if(command.equalsIgnoreCase(buttonMedia.getLabel()) || command.equalsIgnoreCase(MENU_PROPERTIES))
            showPropsSheet();
        else
        if(buttonStepBack != null && command.equalsIgnoreCase(buttonStepBack.getLabel()))
            playStep(false);
        else
        if(buttonStepFwd != null && command.equalsIgnoreCase(buttonStepFwd.getLabel()))
            playStep(true);
    }

    public void itemStateChanged(ItemEvent event)
    {
        java.awt.ItemSelectable item = event.getItemSelectable();
        int nState = event.getStateChange();
        Object objectItem = event.getItem();
        if(item == menuRate_1_4 && nState == 1)
        {
            menuRate_1_4.setState(false);
            player.setRate(0.25F);
        } else
        if(item == menuRate_1_2 && nState == 1)
        {
            menuRate_1_2.setState(false);
            player.setRate(0.5F);
        } else
        if(item == menuRate_1_1 && nState == 1)
        {
            menuRate_1_1.setState(false);
            player.setRate(1.0F);
        } else
        if(item == menuRate_2_1 && nState == 1)
        {
            menuRate_2_1.setState(false);
            player.setRate(2.0F);
        } else
        if(item == menuRate_4_1 && nState == 1)
        {
            menuRate_4_1.setState(false);
            player.setRate(4F);
        } else
        if(item == menuRate_8_1 && nState == 1)
        {
            menuRate_8_1.setState(false);
            player.setRate(8F);
        } else
        if(objectItem instanceof String)
        {
            String strItem = (String)objectItem;
            if(strItem.substring(0, 5).equalsIgnoreCase(MENU_AUDIO))
            {
                int nIndex = Integer.valueOf(strItem.substring(6)).intValue();
                TrackControl trackControl = (TrackControl)vectorTracksAudio.elementAt(nIndex);
                boolean boolEnabled = event.getStateChange() == 1;
                muteAudioTrack(trackControl, !boolEnabled);
            } else
            if(!strItem.substring(0, 5).equalsIgnoreCase(MENU_VIDEO));
        }
    }

    void update()
    {
        if(propsSheet == null || player == null)
            return;
        if(player.getState() == 600)
        {
            pausecnt = -1;
            propsSheet.update();
        } else
        if(pausecnt < 5)
        {
            pausecnt++;
            propsSheet.update();
        } else
        if(pausecnt == 5)
        {
            pausecnt++;
            propsSheet.clearBRFR();
        } else
        if(resetMediaTimeinPause)
        {
            resetMediaTimeinPause = false;
            propsSheet.updateMediaTime();
        }
    }

    void resetPauseCount()
    {
        pausecnt = -1;
    }

    private void playStop()
    {
        boolean state = buttonPlay.getValue();
        synchronized(localLock)
        {
            if(player == null || buttonPlay == null)
                return;
            if(state)
            {
                if(player.getTargetState() != 600)
                {
                    buttonPlay.setEnabled(false);
                    long lDuration = player.getDuration().getNanoseconds();
                    long lMedia = player.getMediaNanoseconds();
                    if(lMedia >= lDuration)
                        player.setMediaTime(new Time(0L));
                    player.start();
                }
            } else
            if(player.getTargetState() == 600)
            {
                buttonPlay.setEnabled(false);
                player.stop();
            }
        }
    }

    private void audioMute()
    {
        if(gainControl == null)
        {
            return;
        } else
        {
            boolean boolState = buttonAudio.getValue();
            gainControl.setMute(boolState);
            return;
        }
    }

    private void playStep(boolean boolFwd)
    {
        if(controlFrame == null)
            return;
        if(player.getTargetState() == 600)
        {
            buttonPlay.setEnabled(false);
            player.stop();
        }
        controlFrame.skip(boolFwd ? 1 : -1);
    }

    public void controllerUpdate(ControllerEvent ce)
    {
        synchronized(localLock)
        {
            if(player == null)
                return;
            if(ce instanceof StartEvent)
            {
                buttonPlay.setValue(true);
                buttonPlay.setEnabled(true);
                if(buttonStepFwd != null)
                    buttonStepFwd.setEnabled(true);
                if(buttonStepBack != null)
                    buttonStepBack.setEnabled(true);
            } else
            if((ce instanceof StopEvent) || (ce instanceof ResourceUnavailableEvent))
            {
                buttonPlay.setValue(false);
                buttonPlay.setEnabled(true);
                Thread.yield();
                long lDuration = player.getDuration().getNanoseconds();
                long lMedia = player.getMediaNanoseconds();
                if(buttonStepFwd != null)
                    if(lMedia < lDuration - 1L)
                        buttonStepFwd.setEnabled(true);
                    else
                        buttonStepFwd.setEnabled(false);
                if(buttonStepBack != null)
                    if(lMedia > 0L)
                        buttonStepBack.setEnabled(true);
                    else
                        buttonStepBack.setEnabled(false);
            } else
            if(ce instanceof DurationUpdateEvent)
            {
                Time duration = player.getDuration();
                if(duration == Duration.DURATION_UNKNOWN || duration == Duration.DURATION_UNBOUNDED)
                    progressSlider.setEnabled(false);
                else
                    progressSlider.setEnabled(true);
                if(propsSheet != null)
                    propsSheet.updateDuration();
            } else
            if(ce instanceof MediaTimeSetEvent)
            {
                Thread.yield();
                long lDuration = player.getDuration().getNanoseconds();
                long lMedia = player.getMediaNanoseconds();
                if(buttonStepFwd != null)
                    if(lMedia < lDuration - 1L)
                        buttonStepFwd.setEnabled(true);
                    else
                        buttonStepFwd.setEnabled(false);
                if(buttonStepBack != null)
                    if(lMedia > 0L)
                        buttonStepBack.setEnabled(true);
                    else
                        buttonStepBack.setEnabled(false);
                resetMediaTimeinPause = true;
            } else
            if(ce instanceof RateChangeEvent)
            {
                menuRate_1_4.setState(false);
                menuRate_1_2.setState(false);
                menuRate_1_1.setState(false);
                menuRate_2_1.setState(false);
                menuRate_4_1.setState(false);
                menuRate_8_1.setState(false);
                float fRate = player.getRate();
                if((double)fRate < 0.5D)
                {
                    menuRate_1_4.removeItemListener(this);
                    menuRate_1_4.setState(true);
                    menuRate_1_4.addItemListener(this);
                } else
                if((double)fRate < 1.0D)
                {
                    menuRate_1_2.removeItemListener(this);
                    menuRate_1_2.setState(true);
                    menuRate_1_2.addItemListener(this);
                } else
                if((double)fRate > 4D)
                {
                    menuRate_8_1.removeItemListener(this);
                    menuRate_8_1.setState(true);
                    menuRate_8_1.addItemListener(this);
                } else
                if((double)fRate > 2D)
                {
                    menuRate_4_1.removeItemListener(this);
                    menuRate_4_1.setState(true);
                    menuRate_4_1.addItemListener(this);
                } else
                if((double)fRate > 1.0D)
                {
                    menuRate_2_1.removeItemListener(this);
                    menuRate_2_1.setState(true);
                    menuRate_2_1.addItemListener(this);
                } else
                {
                    menuRate_1_1.removeItemListener(this);
                    menuRate_1_1.setState(true);
                    menuRate_1_1.addItemListener(this);
                }
            }
        }
    }

    public void gainChange(GainChangeEvent event)
    {
        boolean boolMute = gainControl.getMute();
        buttonAudio.setValue(boolMute);
    }

    public void componentResized(ComponentEvent e)
    {
        validate();
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

    public void paint(Graphics g)
    {
        if(firstTime)
            findFrame();
        super.paint(g);
    }

    protected void findFrame()
    {
        synchronized(localLock)
        {
            if(firstTime)
            {
                firstTime = false;
                Component c;
                for(c = getParent(); !(c instanceof Frame) && c != null; c = c.getParent());
                if(c instanceof Frame)
                {
                    parentFrame = (Frame)c;
                    ((Frame)c).addWindowListener(wl = new WindowAdapter() {

                        public void windowClosing(WindowEvent we)
                        {
                            minicleanUp();
                        }

                    }
);
                }
            }
        }
    }

    public Insets getInsets()
    {
        Insets insets = new Insets(1, 0, 0, 0);
        return insets;
    }

    private void showPropsSheet()
    {
        if(propsSheet == null)
            try
            {
                propsSheet = new PropertySheet(parentFrame, player);
                if(isShowing())
                {
                    Point point = getLocationOnScreen();
                    Dimension dim = getSize();
                    point.y += dim.height;
                    propsSheet.setLocation(point);
                }
            }
            catch(Exception e)
            {
                propsSheet = null;
            }
        if(propsSheet != null)
            propsSheet.setVisible(true);
    }

    private void muteAudioTrack(TrackControl trackControl, boolean boolMute)
    {
        Object arrControls[] = trackControl.getControls();
        int nCount = arrControls.length;
        for(int i = 0; i < nCount; i++)
            if(arrControls[i] instanceof GainControl)
                ((GainControl)arrControls[i]).setMute(boolMute);

    }

    static final Color colorBackground = new Color(192, 192, 192);
    private static final String MENU_PROPERTIES = JMFI18N.getResource("mediaplayer.properties");
    private static final String MENU_RATE_1_4 = JMFI18N.getResource("mediaplayer.rate.1:4");
    private static final String MENU_RATE_1_2 = JMFI18N.getResource("mediaplayer.rate.1:2");
    private static final String MENU_RATE_1_1 = JMFI18N.getResource("mediaplayer.rate.1:1");
    private static final String MENU_RATE_2_1 = JMFI18N.getResource("mediaplayer.rate.2:1");
    private static final String MENU_RATE_4_1 = JMFI18N.getResource("mediaplayer.rate.4:1");
    private static final String MENU_RATE_8_1 = JMFI18N.getResource("mediaplayer.rate.8:1");
    private static final String MENU_MEDIA = JMFI18N.getResource("mediaplayer.menu.media");
    private static final String MENU_AUDIO = JMFI18N.getResource("mediaplayer.menu.audio");
    private static final String MENU_VIDEO = JMFI18N.getResource("mediaplayer.menu.video");
    Player player;
    Frame parentFrame;
    Container container;
    TransparentPanel panelLeft;
    TransparentPanel panelRight;
    TransparentPanel panelProgress;
    boolean boolAdded;
    ButtonComp buttonPlay;
    ButtonComp buttonStepBack;
    ButtonComp buttonStepFwd;
    AudioButton buttonAudio;
    ButtonComp buttonMedia;
    ProgressSlider progressSlider;
    CheckboxMenuItem menuItemCheck;
    PopupMenu menuPopup;
    WindowListener wl;
    private boolean firstTime;
    private boolean started;
    private Integer localLock;
    GainControlComponent audioControls;
    PropertySheet propsSheet;
    FramePositioningControl controlFrame;
    ProgressControl progressControl;
    GainControl gainControl;
    SliderRegionControl regionControl;
    String urlName;
    long lFrameStep;
    private CheckboxMenuItem menuRate_1_4;
    private CheckboxMenuItem menuRate_1_2;
    private CheckboxMenuItem menuRate_1_1;
    private CheckboxMenuItem menuRate_2_1;
    private CheckboxMenuItem menuRate_4_1;
    private CheckboxMenuItem menuRate_8_1;
    private Vector vectorTracksAudio;
    private Vector vectorTracksVideo;
    private int pausecnt;
    private boolean resetMediaTimeinPause;

}
