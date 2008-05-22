// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProgressBar.java

package com.sun.media.ui;

import java.awt.*;
import javax.media.CachingControl;

// Referenced classes of package com.sun.media.ui:
//            Slider, ProgressBarThread, DefaultControlPanel

public class ProgressBar extends Slider
{

    public ProgressBar(CachingControl cc)
    {
        this.cc = null;
        this.cc = cc;
        setGrabberVisible(false);
        setBackground(DefaultControlPanel.colorBackground);
        threadUpdate = new ProgressBarThread(this, cc);
        threadUpdate.start();
    }

    public void update(Graphics g)
    {
        paint(g);
    }

    public void paint(Graphics g)
    {
        if(cc == null)
        {
            super.paint(g);
        } else
        {
            long len = cc.getContentLength();
            long progress = cc.getContentProgress();
            if(len < 1L)
                return;
            if(progress > len)
                len = progress;
            setDisplayPercent((int)((100L * progress) / len));
            super.paint(g);
        }
    }

    private CachingControl cc;
    private Color cb;
    private Color cd;
    private Color cm;
    private ProgressBarThread threadUpdate;
}
