// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DTMsgBox.java

package com.ibm.media.bean.multiplayer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            DTFrame, JMFUtil

public class DTMsgBox extends DTFrame
    implements ActionListener
{

    public DTMsgBox()
    {
        super(JMFUtil.getBIString("JMF_MultiPlayer"));
        msgArea = new TextArea(8, 280);
        nullMsg = " ";
        isCleared = true;
        initGUI();
    }

    public DTMsgBox(String title)
    {
        super(title);
        msgArea = new TextArea(8, 280);
        nullMsg = " ";
        isCleared = true;
        initGUI();
    }

    public DTMsgBox(String title, String msg)
    {
        super(title);
        msgArea = new TextArea(8, 280);
        nullMsg = " ";
        isCleared = true;
        if(msg != null)
            msgArea.setText(msg);
        initGUI();
    }

    private void initGUI()
    {
        Button o = new Button(JMFUtil.getBIString("OK"));
        Button c = new Button(JMFUtil.getBIString("CLEAR"));
        o.setActionCommand(JMFUtil.getBIString("OK"));
        o.addActionListener(this);
        c.setActionCommand(JMFUtil.getBIString("CLEAR"));
        c.addActionListener(this);
        Panel p = new Panel();
        p.setLayout(new GridLayout(1, 2, 2, 0));
        p.add(o);
        p.add(c);
        msgArea.setEditable(false);
        msgArea.setBackground(Color.white);
        setLayout(new BorderLayout(10, 5));
        add("Center", msgArea);
        add("South", p);
        setLocation(500, 10);
        setSize(465, 240);
    }

    public void go()
    {
        setVisible(true);
    }

    public void go(String msg)
    {
        if(msg == null || msg.length() == 0)
            msg = nullMsg;
        if(isCleared)
            msgArea.append(" \n" + msg);
        else
            msgArea.append("\n \n" + msg);
        isCleared = false;
        setVisible(true);
    }

    public void go(String msg, boolean wait)
    {
        if(wait)
            setCursor(new Cursor(3));
        else
            setCursor(new Cursor(0));
        if(msg == null || msg.length() == 0)
            msg = nullMsg;
        if(isCleared)
            msgArea.append(" \n" + msg);
        else
            msgArea.append("\n \n" + msg);
        isCleared = false;
        setVisible(true);
    }

    public void go(String title, String msg)
    {
        setTitle(title);
        if(msg == null || msg.length() == 0)
            msg = nullMsg;
        if(isCleared)
            msgArea.append(" \n" + msg);
        else
            msgArea.append("\n \n" + msg);
        isCleared = false;
        setVisible(true);
    }

    public static void createAndGo(String msg)
    {
        DTMsgBox msgBox = new DTMsgBox();
        msgBox.go(msg);
    }

    public static void createAndGo(String title, String msg)
    {
        DTMsgBox msgBox = new DTMsgBox(title, msg);
        msgBox.setVisible(true);
    }

    public void actionPerformed(ActionEvent evt)
    {
        String s = evt.getActionCommand();
        if(s.equals(JMFUtil.getBIString("CLEAR")))
        {
            msgArea.setText("");
            isCleared = true;
        } else
        if(s.equals(JMFUtil.getBIString("OK")))
            setVisible(false);
    }

    private TextArea msgArea;
    private String nullMsg;
    private boolean isCleared;
}
