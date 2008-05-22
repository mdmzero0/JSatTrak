// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VideoFormatChooser.java

package com.sun.media.ui;

import com.sun.media.util.JMFI18N;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import java.util.Hashtable;

// Referenced classes of package com.sun.media.ui:
//            VideoSize

class VideoSizeControl extends Panel
    implements ItemListener, ComponentListener
{

    public VideoSizeControl()
    {
        this(null);
    }

    public VideoSizeControl(VideoSize sizeVideoDefault)
    {
        htSizes = new Hashtable();
        this.sizeVideoDefault = null;
        this.sizeVideoDefault = sizeVideoDefault;
        init();
    }

    public void setEnabled(boolean boolEnable)
    {
        super.setEnabled(boolEnable);
        comboSize.setEnabled(boolEnable);
        textWidth.setEnabled(boolEnable);
        textHeight.setEnabled(boolEnable);
        labelX.setEnabled(boolEnable);
        if(boolEnable)
            updateFields();
    }

    public void addActionListener(ActionListener listener)
    {
        this.listener = listener;
    }

    public VideoSize getVideoSize()
    {
        String strItem = comboSize.getSelectedItem();
        Object objSize = htSizes.get(strItem);
        VideoSize sizeVideo;
        if(objSize == null || !(objSize instanceof VideoSize) || strItem.equals(CUSTOM_STRING))
        {
            int nWidth;
            try
            {
                nWidth = Integer.valueOf(textWidth.getText()).intValue();
            }
            catch(Exception exception)
            {
                nWidth = 0;
            }
            int nHeight;
            try
            {
                nHeight = Integer.valueOf(textHeight.getText()).intValue();
            }
            catch(Exception exception)
            {
                nHeight = 0;
            }
            sizeVideo = new VideoSize(nWidth, nHeight);
        } else
        {
            sizeVideo = (VideoSize)objSize;
        }
        return sizeVideo;
    }

    public void addItem(VideoSize sizeVideo)
    {
        String strItem;
        if(sizeVideo == null)
        {
            sizeVideo = new VideoSize(-1, -1);
            strItem = CUSTOM_STRING;
        } else
        {
            strItem = sizeVideo.toString();
        }
        if(htSizes.containsKey(strItem))
            return;
        comboSize.addItem(strItem);
        htSizes.put(strItem, sizeVideo);
        if(comboSize.getItemCount() == 1)
            updateFields();
    }

    public void removeAll()
    {
        comboSize.removeAll();
        htSizes = new Hashtable();
        updateFields();
    }

    public void select(VideoSize sizeVideo)
    {
        if(sizeVideo == null)
            comboSize.select(CUSTOM_STRING);
        else
            comboSize.select(sizeVideo.toString());
        updateFields();
    }

    public void select(int nIndex)
    {
        comboSize.select(nIndex);
        updateFields();
    }

    public int getItemCount()
    {
        return comboSize.getItemCount();
    }

    private void init()
    {
        setLayout(new GridLayout(0, 1, 4, 4));
        comboSize = new Choice();
        comboSize.addItem(CUSTOM_STRING);
        comboSize.addItemListener(this);
        add(comboSize);
        panelCustom = new Panel(null);
        panelCustom.addComponentListener(this);
        add(panelCustom);
        if(sizeVideoDefault == null)
            textWidth = new TextField(3);
        else
            textWidth = new TextField("" + ((Dimension) (sizeVideoDefault)).width, 3);
        panelCustom.add(textWidth, "Center");
        labelX = new Label("x", 1);
        panelCustom.add(labelX, "West");
        if(sizeVideoDefault == null)
            textHeight = new TextField(3);
        else
            textHeight = new TextField("" + ((Dimension) (sizeVideoDefault)).height, 3);
        panelCustom.add(textHeight, "Center");
        updateFields();
    }

    private void updateFields()
    {
        String strItem = comboSize.getSelectedItem();
        boolean boolEnable;
        if(strItem == null || strItem.equals(CUSTOM_STRING))
        {
            boolEnable = true;
        } else
        {
            VideoSize sizeVideo = (VideoSize)htSizes.get(strItem);
            textWidth.setText("" + ((Dimension) (sizeVideo)).width);
            textHeight.setText("" + ((Dimension) (sizeVideo)).height);
            boolEnable = false;
        }
        textWidth.setEnabled(boolEnable);
        textHeight.setEnabled(boolEnable);
        labelX.setEnabled(boolEnable);
    }

    private void resizeCustomFields()
    {
        Dimension dimPanel = panelCustom.getSize();
        Dimension dimLabelX = labelX.getPreferredSize();
        int nWidth = (dimPanel.width - dimLabelX.width) / 2;
        textWidth.setBounds(0, 0, nWidth, dimPanel.height);
        labelX.setBounds(nWidth, 0, dimLabelX.width, dimPanel.height);
        textHeight.setBounds(nWidth + dimLabelX.width, 0, nWidth, dimPanel.height);
    }

    public void itemStateChanged(ItemEvent event)
    {
        Object objectSource = event.getSource();
        if(objectSource != comboSize)
            return;
        updateFields();
        if(listener != null)
        {
            ActionEvent eventAction = new ActionEvent(this, 1001, "Size Changed");
            listener.actionPerformed(eventAction);
        }
    }

    public void componentResized(ComponentEvent event)
    {
        resizeCustomFields();
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

    private Choice comboSize;
    private Panel panelCustom;
    private TextField textWidth;
    private TextField textHeight;
    private Label labelX;
    private Hashtable htSizes;
    private VideoSize sizeVideoDefault;
    private ActionListener listener;
    public static final String ACTION_SIZE_CHANGED = "Size Changed";
    static final String CUSTOM_STRING = JMFI18N.getResource("formatchooser.custom");

}
