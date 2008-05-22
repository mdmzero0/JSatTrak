// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProgressBar.java

package com.sun.media.ui;

import java.awt.Component;
import javax.media.CachingControl;

class ProgressBarThread extends Thread
{

    public ProgressBarThread(Component progressBar, CachingControl cachingControl)
    {
        this.progressBar = progressBar;
        this.cachingControl = cachingControl;
        lengthContent = cachingControl.getContentLength();
    }

    public void run()
    {
        for(long lengthProgress = 0L; lengthProgress < lengthContent;)
        {
            try
            {
                Thread.sleep(300L);
            }
            catch(Exception exception) { }
            lengthProgress = cachingControl.getContentProgress();
            progressBar.repaint();
        }

    }

    private Component progressBar;
    private CachingControl cachingControl;
    private long lengthContent;
}
