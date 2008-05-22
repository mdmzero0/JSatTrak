// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DTWinAdapter.java

package com.ibm.media.bean.multiplayer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;

public class DTWinAdapter extends WindowAdapter
{

    public DTWinAdapter(boolean close)
    {
        doExit = false;
        doExit = close;
    }

    public void windowClosing(WindowEvent evt)
    {
        Frame f = (Frame)evt.getSource();
        f.setVisible(false);
        if(doExit)
        {
            f.dispose();
            System.exit(0);
        }
    }

    boolean doExit;
}
