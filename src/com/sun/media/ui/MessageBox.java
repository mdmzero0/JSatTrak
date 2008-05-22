// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MessageBox.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MessageBox extends Frame
{

    public MessageBox(String title, String message)
    {
        super(title);
        setLayout(new BorderLayout());
        Panel bottom = new Panel();
        bottom.setLayout(new FlowLayout());
        Button ok = new Button("Grrr!!!");
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae)
            {
                dispose();
            }

        }
);
        bottom.add(ok);
        add("Center", new Label(message, 1));
        add("South", bottom);
        setVisible(true);
    }

    public void addNotify()
    {
        super.addNotify();
        pack();
    }
}
