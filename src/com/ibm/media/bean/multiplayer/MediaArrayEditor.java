// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaArrayEditor.java

package com.ibm.media.bean.multiplayer;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.Serializable;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            JMFUtil, DTMsgBox

public class MediaArrayEditor extends PropertyEditorSupport
    implements PropertyEditor, Serializable, ActionListener, ItemListener
{

    public MediaArrayEditor()
    {
        support = new PropertyChangeSupport(this);
        guiP = new Panel();
        media = new List(10);
        button = new List(10);
        mediaURL = new Label(JMFUtil.getBIString("MEDIA_URL"));
        buttonURL = new Label(JMFUtil.getBIString("BUTTON_IMAGE_URL"));
        add = new Button(JMFUtil.getBIString("ADD"));
        del = new Button(JMFUtil.getBIString("DELETE"));
        up = new Button(JMFUtil.getBIString("UP"));
        down = new Button(JMFUtil.getBIString("DOWN"));
        one = new Label("1", 1);
        two = new Label("2", 1);
        three = new Label("3", 1);
        four = new Label("4", 1);
        five = new Label("5", 1);
        six = new Label("6", 1);
        seven = new Label("7", 1);
        eight = new Label("8", 1);
        nine = new Label("9", 1);
        ten = new Label("10", 1);
        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        addPanel = null;
        mediaChoice = new Choice();
        buttonChoice = new Choice();
        mediaField = new TextField(20);
        buttonField = new TextField(20);
        codebase = "codebase/";
        http = "http://";
        fileS = "file:///";
        browseString = "...";
        mediaBrowse = new Button(browseString);
        buttonBrowse = new Button(browseString);
        browseDir = ".";
        pan = null;
    }

    public Component getCustomEditor()
    {
        if(pan == null)
            pan = createGuiPanel();
        if(pan.getSize().width > 600)
            pan.setSize(600, pan.getSize().height);
        return pan;
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
        if(newValue != null)
        {
            initString = new StringBuffer("new String[] {\"");
            for(int i = 0; i < newValue.length; i++)
            {
                initString.append(JMFUtil.convertString(newValue[i]));
                if(i + 1 != newValue.length)
                    initString.append("\",\"");
            }

            initString.append("\"}");
        }
        return initString.toString() + ",this";
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

    public boolean isPaintable()
    {
        return true;
    }

    public void setValue(Object val)
    {
        String newStrng = "";
        oldValue = (String[])newValue;
        if(val instanceof String)
            newValue = JMFUtil.parseStringIntoArray((String)val);
        else
            newValue = (String[])val;
        newStrng = JMFUtil.parseArrayIntoString(newValue);
        doDebug("firePropertyChange in MediaNames: " + newStrng);
        support.firePropertyChange("mediaNames", null, newValue);
    }

    public Object getValue()
    {
        return newValue;
    }

    public Panel createGuiPanel()
    {
        guiP.setLayout(gridbag);
        guiP.setBackground(Color.lightGray);
        guiP.setForeground(Color.black);
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        mediaURL.setAlignment(1);
        guiP.add(mediaURL, c);
        c.gridx = 7;
        c.gridy = 1;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        buttonURL.setAlignment(1);
        guiP.add(buttonURL, c);
        refreshLists();
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(one, c);
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(two, c);
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(three, c);
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(four, c);
        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(five, c);
        c.gridx = 1;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(six, c);
        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(seven, c);
        c.gridx = 1;
        c.gridy = 9;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(eight, c);
        c.gridx = 1;
        c.gridy = 10;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(nine, c);
        c.gridx = 1;
        c.gridy = 11;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = 10;
        c.fill = 2;
        c.weightx = 0.0D;
        c.weighty = 0.0D;
        guiP.add(ten, c);
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 5;
        c.gridheight = 10;
        c.anchor = 10;
        c.fill = 1;
        c.weightx = 1.0D;
        c.weighty = 1.0D;
        media.addItemListener(this);
        guiP.add(media, c);
        c.gridx = 7;
        c.gridy = 2;
        c.gridwidth = 5;
        c.gridheight = 10;
        c.anchor = 10;
        c.fill = 1;
        c.weightx = 1.0D;
        c.weighty = 1.0D;
        button.addItemListener(this);
        guiP.add(button, c);
        c.gridx = 2;
        c.gridy = 12;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.anchor = 13;
        c.fill = 2;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        c.insets = new Insets(5, 0, 0, 5);
        mediaChoice.addItemListener(this);
        mediaChoice.add(codebase);
        mediaChoice.add(http);
        mediaChoice.add(fileS);
        mediaBrowse.addActionListener(this);
        mediaBrowse.setActionCommand("browseMedia");
        mediaBrowse.setEnabled(false);
        Component comp1[] = {
            mediaChoice, mediaField, mediaBrowse
        };
        Panel p1 = JMFUtil.doGridbagLayout2(comp1, 3, 2);
        guiP.add(p1, c);
        c.gridx = 7;
        c.gridy = 12;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.anchor = 13;
        c.fill = 2;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        c.insets = new Insets(5, 0, 0, 5);
        buttonChoice.addItemListener(this);
        buttonChoice.add(codebase);
        buttonChoice.add(http);
        buttonChoice.add(fileS);
        buttonBrowse.addActionListener(this);
        buttonBrowse.setActionCommand("browseButton");
        buttonBrowse.setEnabled(false);
        Component comp2[] = {
            buttonChoice, buttonField, buttonBrowse
        };
        Panel p2 = JMFUtil.doGridbagLayout2(comp2, 3, 2);
        guiP.add(p2, c);
        c.gridx = 2;
        c.gridy = 13;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.anchor = 16;
        c.fill = 2;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        add.setActionCommand("add");
        add.addActionListener(this);
        add.setEnabled(true);
        guiP.add(add, c);
        c.gridx = 7;
        c.gridy = 13;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.anchor = 18;
        c.fill = 2;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        del.setActionCommand("del");
        del.addActionListener(this);
        del.setEnabled(false);
        guiP.add(del, c);
        c.gridx = 2;
        c.gridy = 14;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.anchor = 16;
        c.fill = 2;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        up.setActionCommand("up");
        up.addActionListener(this);
        up.setEnabled(false);
        guiP.add(up, c);
        c.gridx = 7;
        c.gridy = 14;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.anchor = 18;
        c.fill = 2;
        c.weightx = 1.0D;
        c.weighty = 0.0D;
        down.setActionCommand("down");
        down.addActionListener(this);
        down.setEnabled(false);
        guiP.add(down, c);
        return guiP;
    }

    private void refreshLists()
    {
        media.removeAll();
        button.removeAll();
        if(newValue != null)
        {
            for(int i = 0; i < newValue.length; i += 2)
            {
                media.add(newValue[i]);
                button.add(newValue[i + 1]);
            }

        }
        media.setSize(75, 232);
        button.setSize(75, 232);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("add"))
            processAdd();
        else
        if(e.getActionCommand().equals("del"))
            deleteGroup();
        else
        if(e.getActionCommand().equals("up"))
            moveSelectedUp();
        else
        if(e.getActionCommand().equals("down"))
            moveSelectedDown();
        else
        if(e.getActionCommand().equals("browseMedia"))
            getFile(mediaField);
        else
        if(e.getActionCommand().equals("browseButton"))
            getFile(buttonField);
    }

    public void itemStateChanged(ItemEvent e)
    {
        Object o1 = e.getItemSelectable();
        if(o1 instanceof List)
        {
            List l1 = (List)o1;
            int i = ((Integer)e.getItem()).intValue();
            int state = e.getStateChange();
            if(l1 == media || l1 == button)
            {
                List l2;
                if(l1 == media)
                    l2 = button;
                else
                    l2 = media;
                if(state == 1)
                {
                    l2.select(i);
                    del.setEnabled(true);
                    enableUpDown(i);
                } else
                if(state == 2)
                {
                    l2.deselect(i);
                    down.setEnabled(false);
                    up.setEnabled(false);
                    del.setEnabled(false);
                }
            }
        } else
        if(o1 instanceof Choice)
        {
            Choice c1 = (Choice)o1;
            String protocol = c1.getSelectedItem();
            if(c1 == mediaChoice)
            {
                if(protocol.equals(fileS))
                    mediaBrowse.setEnabled(true);
                else
                    mediaBrowse.setEnabled(false);
            } else
            if(c1 == buttonChoice)
                if(protocol.equals(fileS))
                    buttonBrowse.setEnabled(true);
                else
                    buttonBrowse.setEnabled(false);
        }
    }

    private void processAdd()
    {
        StringBuffer mText = new StringBuffer();
        StringBuffer bText = new StringBuffer();
        String mString = mediaField.getText();
        String bString = buttonField.getText();
        String bChoice = buttonChoice.getSelectedItem();
        String mChoice = mediaChoice.getSelectedItem();
        if(mString.equals(""))
        {
            DTMsgBox.createAndGo(JMFUtil.getBIString("JMF_MultiPlayer"), JMFUtil.getBIString("NOVALUE"));
            return;
        }
        if(!mChoice.equals(codebase))
        {
            mText.append(mChoice);
            mText.append(mString);
        } else
        {
            mText.append(mString);
        }
        if(!bString.equals(""))
        {
            if(!bChoice.equals(codebase))
            {
                bText.append(bChoice);
                bText.append(bString);
            } else
            {
                bText.append(bString);
            }
        } else
        {
            bText.append("");
        }
        int l = 0;
        if(newValue != null)
            l = newValue.length;
        doDebug("Old length = " + l);
        String temp[] = new String[l + 2];
        JMFUtil.copyStringArray(newValue, temp);
        temp[l] = mText.toString();
        temp[l + 1] = bText.toString();
        media.add(temp[l]);
        button.add(temp[l + 1]);
        mediaField.setText("");
        buttonField.setText("");
        setValue(temp);
    }

    private void moveSelectedUp()
    {
        int i = media.getSelectedIndex();
        if(i != 0)
        {
            String mURL = media.getSelectedItem();
            String bURL = button.getSelectedItem();
            media.remove(i);
            button.remove(i);
            media.add(mURL, i - 1);
            button.add(bURL, i - 1);
            media.select(i - 1);
            button.select(i - 1);
            newValue[i * 2] = newValue[(i - 1) * 2];
            newValue[i * 2 + 1] = newValue[(i - 1) * 2 + 1];
            newValue[(i - 1) * 2] = mURL;
            newValue[(i - 1) * 2 + 1] = bURL;
            setValue(newValue);
            enableUpDown(i - 1);
        }
    }

    private void moveSelectedDown()
    {
        int i = media.getSelectedIndex();
        if(i != media.getItemCount() - 1)
        {
            String mURL = media.getSelectedItem();
            String bURL = button.getSelectedItem();
            media.remove(i);
            button.remove(i);
            media.add(mURL, i + 1);
            button.add(bURL, i + 1);
            media.select(i + 1);
            button.select(i + 1);
            newValue[i * 2] = newValue[(i + 1) * 2];
            newValue[i * 2 + 1] = newValue[(i + 1) * 2 + 1];
            newValue[(i + 1) * 2] = mURL;
            newValue[(i + 1) * 2 + 1] = bURL;
            setValue(newValue);
            enableUpDown(i + 1);
        }
    }

    private void enableUpDown(int i)
    {
        if(i < media.getItemCount() - 1)
            down.setEnabled(true);
        else
            down.setEnabled(false);
        if(i > 0)
            up.setEnabled(true);
        else
            up.setEnabled(false);
    }

    private void deleteGroup()
    {
        String temp[] = null;
        int j = media.getSelectedIndex();
        int index = j * 2;
        int newSize = 0;
        if(newValue != null)
        {
            newSize = newValue.length - 2;
            doDebug("NewValue size = " + newSize);
            if(newSize > 0)
            {
                temp = new String[newSize];
                JMFUtil.copyShortenStringArray(newValue, temp, index, 2);
            }
        }
        doDebug("Temp = " + temp);
        media.remove(j);
        button.remove(j);
        if(media.getItemCount() > j + 1)
        {
            media.select(j);
            button.select(j);
            enableUpDown(j);
        } else
        if(media.getItemCount() > 0)
        {
            media.select(j - 1);
            button.select(j - 1);
            enableUpDown(j - 1);
        } else
        {
            enableUpDown(-1);
            del.setEnabled(false);
        }
        setValue(temp);
    }

    Frame getFrame(Component comp)
    {
        java.awt.Point p = comp.getLocationOnScreen();
        Frame f = new Frame();
        f.setLocation(p);
        return f;
    }

    private void getFile(TextField tf)
    {
        doDebug("getFile " + tf);
        String title;
        if(tf == mediaField)
            title = JMFUtil.getBIString("SET_MEDIA_LOCATION");
        else
            title = JMFUtil.getBIString("SET_BUTTON_LOCATION");
        FileDialog fd = new FileDialog(getFrame(guiP), title, 0);
        fd.setDirectory(browseDir);
        fd.setTitle(title);
        fd.show();
        String filename = fd.getFile();
        if(fd.getDirectory() != null)
            browseDir = fd.getDirectory();
        if(filename != null && fd.getDirectory() != null)
            filename = fd.getDirectory() + filename;
        if(filename != null)
        {
            filename = filename.replace('\\', '/');
            tf.setText(filename);
        }
    }

    private void doDebug(String s1)
    {
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        support.removePropertyChangeListener(listener);
    }

    public String[] getTags()
    {
        return null;
    }

    PropertyChangeSupport support;
    Panel guiP;
    transient String oldValue[];
    transient String newValue[];
    List media;
    List button;
    Label mediaURL;
    Label buttonURL;
    Button add;
    Button del;
    Button up;
    Button down;
    Label one;
    Label two;
    Label three;
    Label four;
    Label five;
    Label six;
    Label seven;
    Label eight;
    Label nine;
    Label ten;
    GridBagLayout gridbag;
    GridBagConstraints c;
    Panel addPanel;
    Choice mediaChoice;
    Choice buttonChoice;
    TextField mediaField;
    TextField buttonField;
    String codebase;
    String http;
    String fileS;
    private String browseString;
    private Button mediaBrowse;
    private Button buttonBrowse;
    private String browseDir;
    Panel pan;
}
