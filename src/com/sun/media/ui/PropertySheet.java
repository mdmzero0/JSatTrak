// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PropertySheet.java

package com.sun.media.ui;

import com.sun.media.BasicPlayer;
import com.sun.media.util.JMFI18N;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

// Referenced classes of package com.sun.media.ui:
//            TabControl, UrlLabel, ColumnList

public class PropertySheet extends Dialog
    implements WindowListener, ActionListener
{

    public PropertySheet(Frame parent, Player player)
    {
        super(parent, JMFI18N.getResource("propertysheet.title"), false);
        vectorControlBitRate = new Vector(1);
        vectorLabelBitRate = new Vector(1);
        controlFrameRate = null;
        vectorTrackFormats = new Vector();
        nAudioTrackCount = 0;
        nVideoTrackCount = 0;
        vectorMiscControls = new Vector();
        labelDuration = null;
        labelPosition = null;
        labelBitRate = null;
        labelFrameRate = null;
        this.player = player;
        try
        {
            init();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void init()
        throws Exception
    {
        setLayout(new BorderLayout(5, 5));
        setBackground(Color.lightGray);
        Panel panel = createPanelButtons();
        add(panel, "South");
        panel = createPanelProperties();
        add(panel, "Center");
        Canvas canvas = new Canvas();
        add(canvas, "North");
        canvas = new Canvas();
        add(canvas, "East");
        canvas = new Canvas();
        add(canvas, "West");
        pack();
        addWindowListener(this);
        setResizable(false);
        Dimension dim = getPreferredSize();
        if(dim.width > 480)
            dim.width = 480;
        setBounds(100, 100, dim.width, dim.height);
        repaint();
    }

    private Panel createPanelProperties()
        throws Exception
    {
        TabControl tabControl = new TabControl(0);
        Panel panel = createPanelGeneral();
        tabControl.addPage(panel, JMFI18N.getResource("propertysheet.tab.general"));
        if(nVideoTrackCount > 0)
        {
            panel = createPanelVideo(vectorTrackFormats);
            tabControl.addPage(panel, JMFI18N.getResource("propertysheet.tab.video"));
        }
        if(nAudioTrackCount > 0)
        {
            panel = createPanelAudio(vectorTrackFormats);
            tabControl.addPage(panel, JMFI18N.getResource("propertysheet.tab.audio"));
        }
        if(!vectorMiscControls.isEmpty())
        {
            panel = createPanelMisc();
            tabControl.addPage(panel, JMFI18N.getResource("propertysheet.tab.misc"));
        }
        update();
        return tabControl;
    }

    private Panel createPanelGeneral()
        throws Exception
    {
        String strValue = null;
        Panel panelGeneral = new Panel(new BorderLayout());
        Panel panel = new Panel(new BorderLayout(8, 4));
        panelGeneral.add(panel, "North");
        Panel panelLabels = new Panel(new GridLayout(0, 1, 4, 4));
        panel.add(panelLabels, "West");
        Panel panelData = new Panel(new GridLayout(0, 1, 4, 4));
        panel.add(panelData, "Center");
        Label label;
        if(player instanceof BasicPlayer)
        {
            BasicPlayer playerBasic = (BasicPlayer)player;
            MediaLocator mediaLocator = playerBasic.getMediaLocator();
            if(mediaLocator != null)
                strValue = mediaLocator.toString();
            if(strValue != null)
            {
                label = new Label(JMFI18N.getResource("propertysheet.general.medialocation"), 2);
                panelLabels.add(label);
                UrlLabel labelUrl = new UrlLabel(strValue);
                panelData.add(labelUrl);
            }
            strValue = playerBasic.getContentType();
            if(strValue != null)
            {
                strValue = (new ContentDescriptor(strValue)).toString();
                label = new Label(JMFI18N.getResource("propertysheet.general.contenttype"), 2);
                panelLabels.add(label);
                label = new Label(strValue);
                panelData.add(label);
            }
        }
        label = new Label(JMFI18N.getResource("propertysheet.general.duration"), 2);
        panelLabels.add(label);
        labelDuration = new Label();
        panelData.add(labelDuration);
        label = new Label(JMFI18N.getResource("propertysheet.general.position"), 2);
        panelLabels.add(label);
        labelPosition = new Label();
        panelData.add(labelPosition);
        nAudioTrackCount = 0;
        nVideoTrackCount = 0;
        Control arrControls[] = player.getControls();
        for(int i = 0; i < arrControls.length; i++)
            if(arrControls[i] != null)
            {
                if((arrControls[i] instanceof FormatControl) && (!(arrControls[i] instanceof Owned) || !(((Owned)arrControls[i]).getOwner() instanceof SourceStream) && !(((Owned)arrControls[i]).getOwner() instanceof DataSource)))
                {
                    Format format = ((FormatControl)arrControls[i]).getFormat();
                    vectorTrackFormats.addElement(format);
                    if(format instanceof AudioFormat)
                        nAudioTrackCount++;
                    else
                    if(format instanceof VideoFormat)
                        nVideoTrackCount++;
                }
                if(!(arrControls[i] instanceof TrackControl))
                    if(arrControls[i] instanceof BitRateControl)
                    {
                        BitRateControl controlBitRateTemp = (BitRateControl)arrControls[i];
                        if((controlBitRateTemp instanceof Owned) && (((Owned)controlBitRateTemp).getOwner() instanceof Controller))
                        {
                            vectorControlBitRate.addElement(controlBitRateTemp);
                            label = new Label(JMFI18N.getResource("propertysheet.general.bitrate"), 2);
                            panelLabels.add(label);
                            labelBitRate = new Label();
                            vectorLabelBitRate.addElement(labelBitRate);
                            panelData.add(labelBitRate);
                        } else
                        {
                            vectorMiscControls.addElement(arrControls[i]);
                        }
                    } else
                    if(arrControls[i] instanceof FrameRateControl)
                    {
                        FrameRateControl controlFrameRateTemp = (FrameRateControl)arrControls[i];
                        if((controlFrameRateTemp instanceof Owned) && (((Owned)controlFrameRateTemp).getOwner() instanceof Controller))
                        {
                            controlFrameRate = controlFrameRateTemp;
                            label = new Label(JMFI18N.getResource("propertysheet.general.framerate"), 2);
                            panelLabels.add(label);
                            labelFrameRate = new Label();
                            panelData.add(labelFrameRate);
                        } else
                        {
                            vectorMiscControls.addElement(arrControls[i]);
                        }
                    } else
                    if(!(arrControls[i] instanceof GainControl) && !(arrControls[i] instanceof MonitorControl) && (!(arrControls[i] instanceof Owned) || !(((Owned)arrControls[i]).getOwner() instanceof CaptureDevice) && !(((Owned)arrControls[i]).getOwner() instanceof SourceStream) && !(((Owned)arrControls[i]).getOwner() instanceof DataSource)) && !(arrControls[i] instanceof CachingControl))
                        vectorMiscControls.addElement(arrControls[i]);
            }

        return panelGeneral;
    }

    private Panel createPanelVideo(Vector vectorFormats)
        throws Exception
    {
        String arrColumnNames[] = {
            JMFI18N.getResource("propertysheet.video.track"), JMFI18N.getResource("propertysheet.video.encoding"), JMFI18N.getResource("propertysheet.video.size"), JMFI18N.getResource("propertysheet.video.framerate")
        };
        Panel panelVideo = new Panel(new BorderLayout());
        columnListVideo = new ColumnList(arrColumnNames);
        String arrValues[] = new String[arrColumnNames.length];
        int nCount = vectorFormats.size();
        int nTrackIndex = 0;
        for(int i = 0; i < nCount; i++)
        {
            Object objectFormat = vectorFormats.elementAt(i);
            if(objectFormat instanceof VideoFormat)
            {
                VideoFormat formatVideo = (VideoFormat)objectFormat;
                nTrackIndex++;
                arrValues[0] = new String("" + nTrackIndex);
                arrValues[1] = formatVideo.getEncoding();
                Dimension dimSize = formatVideo.getSize();
                if(dimSize == null)
                    arrValues[2] = new String(STR_UNKNOWN);
                else
                    arrValues[2] = new String("" + dimSize.width + " x " + dimSize.height);
                float fValue = formatVideo.getFrameRate();
                if(fValue == -1F)
                    arrValues[3] = STR_UNKNOWN;
                else
                    arrValues[3] = "" + fValue;
                columnListVideo.addRow(arrValues);
            }
        }

        columnListVideo.setColumnWidthAsPreferred();
        panelVideo.add(columnListVideo, "Center");
        return panelVideo;
    }

    private Panel createPanelAudio(Vector vectorFormats)
        throws Exception
    {
        String arrColumnNames[] = {
            JMFI18N.getResource("propertysheet.audio.track"), JMFI18N.getResource("propertysheet.audio.encoding"), JMFI18N.getResource("propertysheet.audio.samplerate"), JMFI18N.getResource("propertysheet.audio.bitspersample"), JMFI18N.getResource("propertysheet.audio.channels")
        };
        Panel panelAudio = new Panel(new BorderLayout());
        columnListAudio = new ColumnList(arrColumnNames);
        String arrValues[] = new String[arrColumnNames.length];
        int nCount = vectorFormats.size();
        int nTrackIndex = 0;
        for(int i = 0; i < nCount; i++)
        {
            Object objectFormat = vectorFormats.elementAt(i);
            if(objectFormat instanceof AudioFormat)
            {
                AudioFormat formatAudio = (AudioFormat)objectFormat;
                nTrackIndex++;
                arrValues[0] = new String("" + nTrackIndex);
                arrValues[1] = formatAudio.getEncoding();
                double dValue = formatAudio.getSampleRate();
                if(dValue == -1D)
                    arrValues[2] = STR_UNKNOWN;
                else
                    arrValues[2] = "" + dValue;
                int nValue = formatAudio.getSampleSizeInBits();
                if(nValue == -1)
                    arrValues[3] = STR_UNKNOWN;
                else
                    arrValues[3] = "" + nValue;
                nValue = formatAudio.getChannels();
                if(nValue == -1)
                    arrValues[4] = STR_UNKNOWN;
                else
                if(nValue == 1)
                    arrValues[4] = "" + nValue + " (" + JMFI18N.getResource("propertysheet.audio.channels.mono") + ")";
                else
                if(nValue == 2)
                    arrValues[4] = "" + nValue + " (" + JMFI18N.getResource("propertysheet.audio.channels.stereo") + ")";
                else
                    arrValues[4] = "" + nValue;
                columnListAudio.addRow(arrValues);
            }
        }

        columnListAudio.setColumnWidthAsPreferred();
        panelAudio.add(columnListAudio, "Center");
        return panelAudio;
    }

    private Panel createPanelMisc()
        throws Exception
    {
        Panel panelMisc = new Panel(new BorderLayout(6, 6));
        Panel panel = panelMisc;
        int nSize = vectorMiscControls.size();
        for(int i = 0; i < nSize; i++)
        {
            Control control = (Control)vectorMiscControls.elementAt(i);
            Component comp = control.getControlComponent();
            if(comp != null && comp.getParent() == null)
            {
                Panel panelControl = new Panel(new BorderLayout(6, 6));
                panelControl.add(comp, "West");
                Panel panelNext = new Panel(new BorderLayout(6, 6));
                panelNext.add(panelControl, "North");
                panel.add(panelNext, "Center");
                panel = panelNext;
            }
        }

        return panelMisc;
    }

    private Panel createPanelButtons()
        throws Exception
    {
        Panel panelButtons = new Panel(new FlowLayout(2));
        Panel panel = new Panel(new GridLayout(1, 0, 6, 6));
        panelButtons.add(panel);
        buttonClose = new Button(JMFI18N.getResource("propertysheet.close"));
        buttonClose.addActionListener(this);
        panel.add(buttonClose);
        return panelButtons;
    }

    public void actionPerformed(ActionEvent e)
    {
        String strCmd = e.getActionCommand();
        if(strCmd.equals(buttonClose.getLabel()))
            setVisible(false);
    }

    public void windowOpened(WindowEvent windowevent)
    {
    }

    public void windowClosing(WindowEvent e)
    {
        setVisible(false);
    }

    public void windowClosed(WindowEvent windowevent)
    {
    }

    public void windowIconified(WindowEvent windowevent)
    {
    }

    public void windowDeiconified(WindowEvent windowevent)
    {
    }

    public void windowActivated(WindowEvent windowevent)
    {
    }

    public void windowDeactivated(WindowEvent windowevent)
    {
    }

    void update()
    {
        updateBitRate();
        updateFrameRate();
        updateMediaTime();
        updateDuration();
    }

    void updateDuration()
    {
        if(labelDuration != null)
        {
            Time timeDuration = player.getDuration();
            labelDuration.setText(formatTime(timeDuration));
        }
    }

    void updateBitRate()
    {
        if(vectorLabelBitRate.size() > 0)
        {
            for(int i = 0; i < vectorLabelBitRate.size(); i++)
            {
                Label labelBitRate = (Label)vectorLabelBitRate.elementAt(i);
                BitRateControl controlBitRate = (BitRateControl)vectorControlBitRate.elementAt(i);
                int bitRate = controlBitRate.getBitRate();
                labelBitRate.setText(Float.toString((float)bitRate / 1000F) + " " + JMFI18N.getResource("propertysheet.kbps"));
            }

        }
    }

    void updateFrameRate()
    {
        if(labelFrameRate != null && controlFrameRate != null)
        {
            float frameRate = controlFrameRate.getFrameRate();
            labelFrameRate.setText(Float.toString(frameRate) + " " + JMFI18N.getResource("propertysheet.fps"));
        }
    }

    void clearBRFR()
    {
        if(labelFrameRate != null)
            labelFrameRate.setText("0.0 " + JMFI18N.getResource("propertysheet.fps"));
        if(labelBitRate != null)
            labelBitRate.setText("0.0 " + JMFI18N.getResource("propertysheet.kbps"));
    }

    void updateMediaTime()
    {
        if(labelPosition != null)
        {
            Time timeMedia = player.getMediaTime();
            labelPosition.setText(formatTime(timeMedia));
        }
    }

    private String formatTime(Time time)
    {
        String strTime = new String(STR_UNKNOWN);
        if(time == null || time == Time.TIME_UNKNOWN || time == Duration.DURATION_UNKNOWN)
            return strTime;
        if(time == Duration.DURATION_UNBOUNDED)
        {
            return STR_UNBOUNDED;
        } else
        {
            long nano = time.getNanoseconds();
            int seconds = (int)(nano / 0x3b9aca00L);
            int hours = seconds / 3600;
            int minutes = (seconds - hours * 3600) / 60;
            seconds = seconds - hours * 3600 - minutes * 60;
            nano = (nano % 0x3b9aca00L) / 0x989680L;
            int hours10 = hours / 10;
            hours %= 10;
            int minutes10 = minutes / 10;
            minutes %= 10;
            int seconds10 = seconds / 10;
            seconds %= 10;
            long nano10 = nano / 10L;
            nano %= 10L;
            strTime = new String("" + hours10 + hours + ":" + minutes10 + minutes + ":" + seconds10 + seconds + "." + nano10 + nano);
            return strTime;
        }
    }

    private Player player;
    private Vector vectorControlBitRate;
    private Vector vectorLabelBitRate;
    private FrameRateControl controlFrameRate;
    private Vector vectorTrackFormats;
    private int nAudioTrackCount;
    private int nVideoTrackCount;
    private Vector vectorMiscControls;
    private Button buttonClose;
    private Label labelDuration;
    private Label labelPosition;
    private Label labelBitRate;
    private Label labelFrameRate;
    private ColumnList columnListAudio;
    private ColumnList columnListVideo;
    private static final String STR_UNKNOWN = JMFI18N.getResource("propertysheet.unknown");
    private static final String STR_UNBOUNDED = JMFI18N.getResource("propertysheet.unbounded");

}
