// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LinksArrayEditor.java

package com.ibm.media.bean.multiplayer;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.Serializable;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            JMFUtil, DTMsgBox

public class LinksArrayEditor extends PropertyEditorSupport
    implements PropertyEditor, Serializable, ActionListener, ItemListener
{

    public LinksArrayEditor()
    {
        support = new PropertyChangeSupport(this);
        linksPanel = new Panel();
        mediaGroup = new List(5);
        related = new List(5);
        start = new List(5);
        stop = new List(5);
        relatedField = new TextField(20);
        startField = new TextField(3);
        stopField = new TextField(3);
        mediaNumField = new TextField(3);
        addLink = new Button(JMFUtil.getBIString("ADD"));
        delLink = new Button(JMFUtil.getBIString("DELETE"));
        mediaLabel = new Label(JMFUtil.getBIString("MEDIA_GROUP"));
        linkLabel = new Label(JMFUtil.getBIString("RELATED_LINK_URL"));
        startLabel = new Label(JMFUtil.getBIString("START_TIME"));
        stopLabel = new Label(JMFUtil.getBIString("STOP_TIME"));
        pan = null;
        addLinkC = "addLink";
        delLinkC = "delLink";
    }

    public Component getCustomEditor()
    {
        if(pan == null)
            pan = createGuiPanel();
        return pan;
    }

    public void paintValue(Graphics g, Rectangle r)
    {
        g.setColor(Color.black);
        g.draw3DRect(1, 1, r.width - 2, r.height - 2, true);
        g.setColor(Color.black);
        g.setFont(new Font("Helevetica", 1, 9));
        g.drawString(getAsText(), 5, r.height / 2 + 5);
    }

    public boolean supportsCustomEditor()
    {
        return true;
    }

    public void setAsText(String s)
    {
        setValue(JMFUtil.parseStringIntoArray(s));
    }

    public String getAsText()
    {
        return JMFUtil.parseArrayIntoString(newValue);
    }

    public String getJavaInitializationString()
    {
        StringBuffer initString = new StringBuffer("");
        if(newValue == null)
            return "null";
        initString = new StringBuffer("new String[] {\"");
        for(int i = 0; i < newValue.length; i++)
        {
            initString.append(JMFUtil.convertString(newValue[i]));
            if(i + 1 != newValue.length)
                initString.append("\",\"");
        }

        initString.append("\"}");
        return initString.toString();
    }

    public boolean isPaintable()
    {
        return true;
    }

    public void setValue(Object val)
    {
        oldValue = newValue;
        if(val instanceof String)
            newValue = JMFUtil.parseStringIntoArray((String)val);
        else
            newValue = (String[])val;
        firePropertyChange();
        support.firePropertyChange("links", null, newValue);
    }

    public Object getValue()
    {
        return newValue;
    }

    public Panel createGuiPanel()
    {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        linksPanel.setLayout(gridbag);
        linksPanel.setBackground(Color.lightGray);
        linksPanel.setForeground(Color.black);
        delLink.setEnabled(false);
        linksPanel.setLayout(gridbag);
        refreshLists();
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 17;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        linksPanel.add(mediaLabel, c);
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.anchor = 17;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        linksPanel.add(linkLabel, c);
        c.gridx = 5;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 17;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        linksPanel.add(startLabel, c);
        c.gridx = 6;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 17;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        linksPanel.add(stopLabel, c);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 5;
        c.anchor = 17;
        c.fill = 3;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        mediaGroup.addItemListener(this);
        linksPanel.add(mediaGroup, c);
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 3;
        c.gridheight = 5;
        c.anchor = 17;
        c.fill = 1;
        c.weightx = 1.0D;
        c.weighty = 1.0D;
        related.addItemListener(this);
        linksPanel.add(related, c);
        c.gridx = 5;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 5;
        c.anchor = 17;
        c.fill = 3;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        start.addItemListener(this);
        linksPanel.add(start, c);
        c.gridx = 6;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 5;
        c.anchor = 17;
        c.fill = 3;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        stop.addItemListener(this);
        linksPanel.add(stop, c);
        c.gridx = 1;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(1, 1, 1, 1);
        c.anchor = 10;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        linksPanel.add(mediaNumField, c);
        c.gridx = 2;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(1, 1, 1, 1);
        c.anchor = 17;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        linksPanel.add(new Label("http://"), c);
        c.gridx = 3;
        c.gridy = 7;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.insets = new Insets(1, 1, 1, 1);
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 1.0D;
        linksPanel.add(relatedField, c);
        c.gridx = 5;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(1, 1, 1, 1);
        c.anchor = 10;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        linksPanel.add(startField, c);
        c.gridx = 6;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(1, 1, 1, 1);
        c.anchor = 10;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        linksPanel.add(stopField, c);
        c.gridx = 1;
        c.gridy = 9;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(1, 1, 1, 1);
        c.anchor = 10;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        addLink.addActionListener(this);
        addLink.setActionCommand(addLinkC);
        linksPanel.add(addLink, c);
        c.gridx = 2;
        c.gridy = 9;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(1, 1, 1, 1);
        c.anchor = 10;
        c.fill = 0;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        delLink.addActionListener(this);
        delLink.setActionCommand(delLinkC);
        linksPanel.add(delLink, c);
        return linksPanel;
    }

    private void refreshLists()
    {
        mediaGroup.removeAll();
        related.removeAll();
        start.removeAll();
        stop.removeAll();
        if(newValue != null)
        {
            for(int i = 0; i < newValue.length; i += 4)
            {
                mediaGroup.add(newValue[i]);
                related.add(newValue[i + 1]);
                start.add(newValue[i + 2]);
                stop.add(newValue[i + 3]);
            }

        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals(addLinkC))
            processAdd();
        else
        if(e.getActionCommand().equals(delLinkC))
            deleteLink();
    }

    public void itemStateChanged(ItemEvent e)
    {
        Object o1 = e.getItemSelectable();
        if(o1 instanceof List)
        {
            List l1 = (List)o1;
            int i = ((Integer)e.getItem()).intValue();
            int state = e.getStateChange();
            if(l1 == related || l1 == start || l1 == stop || l1 == mediaGroup)
            {
                l1 = related;
                List l2 = start;
                List l3 = stop;
                List l4 = mediaGroup;
                if(state == 1)
                {
                    l1.select(i);
                    l2.select(i);
                    l3.select(i);
                    l4.select(i);
                    delLink.setEnabled(true);
                } else
                if(state == 2)
                {
                    l1.deselect(i);
                    l2.deselect(i);
                    l3.deselect(i);
                    l4.deselect(i);
                    delLink.setEnabled(false);
                }
            }
        }
    }

    private void processAdd()
    {
        StringBuffer relatedBuffer = new StringBuffer("http://");
        String mString = mediaNumField.getText();
        String rString = relatedField.getText();
        String startString = startField.getText();
        String stopString = stopField.getText();
        if(startString.equals("") && stopString.equals(""))
        {
            startString = new String("0");
            stopString = new String("0");
        }
        if(mString.equals("") || rString.equals("") || startString.equals("") || stopString.equals(""))
        {
            DTMsgBox.createAndGo(JMFUtil.getBIString("JMF_MultiPlayer"), JMFUtil.getBIString("NOVALUE"));
            return;
        }
        long startTime;
        long stopTime;
        try
        {
            startTime = Long.parseLong(startString);
            stopTime = Long.parseLong(stopString);
        }
        catch(NumberFormatException e)
        {
            DTMsgBox.createAndGo(JMFUtil.getBIString("JMF_MultiPlayer"), JMFUtil.getBIString("TIMES") + ": " + JMFUtil.getBIString("0orGreater"));
            return;
        }
        int mediaIndex;
        try
        {
            mediaIndex = Integer.parseInt(mString);
        }
        catch(NumberFormatException e)
        {
            DTMsgBox.createAndGo(JMFUtil.getBIString("JMF_MultiPlayer"), JMFUtil.getBIString("INDEX") + ": " + JMFUtil.getBIString("1orGreater"));
            return;
        }
        if(startTime < 0L || stopTime < 0L)
        {
            DTMsgBox.createAndGo(JMFUtil.getBIString("JMF_MultiPlayer"), JMFUtil.getBIString("TIMES") + ": " + JMFUtil.getBIString("0orGreater"));
            return;
        }
        if(mediaIndex < 1)
        {
            DTMsgBox.createAndGo(JMFUtil.getBIString("JMF_MultiPlayer"), JMFUtil.getBIString("INDEX") + ": " + JMFUtil.getBIString("1orGreater"));
            return;
        }
        relatedBuffer.append(rString);
        if(duplicate(mString, relatedBuffer.toString(), startString, stopString))
        {
            DTMsgBox.createAndGo(JMFUtil.getBIString("JMF_MultiPlayer"), JMFUtil.getBIString("DUPLICATE_LINK"));
            return;
        }
        int l = 0;
        if(newValue != null)
            l = newValue.length;
        String temp[] = new String[l + 4];
        JMFUtil.copyStringArray(newValue, temp);
        temp[l] = mString;
        temp[l + 1] = relatedBuffer.toString();
        temp[l + 2] = startString;
        temp[l + 3] = stopString;
        mediaGroup.add(temp[l]);
        related.add(temp[l + 1]);
        start.add(temp[l + 2]);
        stop.add(temp[l + 3]);
        mediaNumField.setText("");
        relatedField.setText("");
        startField.setText("");
        stopField.setText("");
        setValue(temp);
    }

    private boolean duplicate(String media, String relatedS, String startS, String stopS)
    {
        for(int i = 0; i < mediaGroup.getItemCount(); i++)
            if(media.equals(mediaGroup.getItem(i)) & relatedS.equals(related.getItem(i)) & startS.equals(start.getItem(i)) & stopS.equals(stop.getItem(i)))
                return true;

        return false;
    }

    private void deleteLink()
    {
        String temp[] = null;
        int j = mediaGroup.getSelectedIndex();
        int index = j * 4;
        int newSize = 0;
        if(newValue != null)
        {
            newSize = newValue.length - 4;
            if(newSize > 4)
            {
                temp = new String[newSize];
                JMFUtil.copyShortenStringArray(newValue, temp, index, 4);
            }
        }
        mediaGroup.remove(j);
        related.remove(j);
        start.remove(j);
        stop.remove(j);
        delLink.setEnabled(false);
        setValue(temp);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        support.removePropertyChangeListener(listener);
    }

    PropertyChangeSupport support;
    Panel linksPanel;
    transient String oldValue[];
    transient String newValue[];
    List mediaGroup;
    List related;
    List start;
    List stop;
    TextField relatedField;
    TextField startField;
    TextField stopField;
    TextField mediaNumField;
    Button addLink;
    Button delLink;
    Label mediaLabel;
    Label linkLabel;
    Label startLabel;
    Label stopLabel;
    Panel pan;
    String addLinkC;
    String delLinkC;
}
