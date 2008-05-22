// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DefaultControlPanel.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

class TransparentPanel extends Container
    implements ComponentListener
{

    public TransparentPanel()
    {
        addComponentListener(this);
    }

    public TransparentPanel(LayoutManager mgrLayout)
    {
        setLayout(mgrLayout);
        addComponentListener(this);
    }

    public void componentResized(ComponentEvent e)
    {
        doLayout();
        repaint();
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
}
