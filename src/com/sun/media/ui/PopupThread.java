// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DefaultControlPanel.java

package com.sun.media.ui;

import java.awt.Component;
import java.awt.Window;

class PopupThread extends Thread
{

    public PopupThread(Window window)
    {
        nTimeCounter = 3;
        boolRun = true;
        this.window = window;
    }

    public void resetCounter(int nTimeCounter)
    {
        this.nTimeCounter = nTimeCounter;
    }

    public void stopNormaly()
    {
        boolRun = false;
    }

    public void run()
    {
        while(boolRun) 
        {
            if(nTimeCounter < 1)
                window.setVisible(false);
            try
            {
                Thread.sleep(1000L);
            }
            catch(Exception exception) { }
            nTimeCounter--;
        }
    }

    private Window window;
    private int nTimeCounter;
    private boolean boolRun;
}
