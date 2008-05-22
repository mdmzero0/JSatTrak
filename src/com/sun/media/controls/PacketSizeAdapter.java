// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PacketSizeAdapter.java

package com.sun.media.controls;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import javax.media.Codec;
import javax.media.control.PacketSizeControl;

public class PacketSizeAdapter
    implements PacketSizeControl
{
    class PacketSizeListner
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                int newPacketSize = Integer.parseInt(tf.getText());
                System.out.println("newPacketSize " + newPacketSize);
                setPacketSize(newPacketSize);
            }
            catch(Exception exception) { }
            tf.setText(packetSize + "");
        }

        TextField tf;

        public PacketSizeListner(TextField source)
        {
            tf = source;
        }
    }


    public PacketSizeAdapter(Codec newOwner, int newPacketSize, boolean newIsSetable)
    {
        owner = null;
        component = null;
        CONTROL_STRING = "Packet Size";
        packetSize = newPacketSize;
        owner = newOwner;
        isSetable = newIsSetable;
    }

    public int setPacketSize(int numBytes)
    {
        return packetSize;
    }

    public int getPacketSize()
    {
        return packetSize;
    }

    public Component getControlComponent()
    {
        if(component == null)
        {
            Panel componentPanel = new Panel();
            componentPanel.setLayout(new BorderLayout());
            componentPanel.add("Center", new Label(CONTROL_STRING, 1));
            TextField tf = new TextField(packetSize + "", 5);
            tf.setEditable(isSetable);
            tf.addActionListener(new PacketSizeListner(tf));
            componentPanel.add("East", tf);
            componentPanel.invalidate();
            component = componentPanel;
        }
        return component;
    }

    protected Codec owner;
    protected boolean isSetable;
    protected int packetSize;
    Component component;
    String CONTROL_STRING;
}
