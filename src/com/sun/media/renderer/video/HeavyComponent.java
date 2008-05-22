// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HeavyComponent.java

package com.sun.media.renderer.video;

import java.awt.*;

// Referenced classes of package com.sun.media.renderer.video:
//            BasicVideoRenderer

public class HeavyComponent extends Canvas
{

    public HeavyComponent()
    {
        bvr = null;
    }

    public void setRenderer(BasicVideoRenderer bvr)
    {
        this.bvr = bvr;
    }

    public synchronized void paint(Graphics g)
    {
        if(bvr != null)
            bvr.repaint();
    }

    public synchronized void update(Graphics g1)
    {
    }

    public Dimension getMinimumSize()
    {
        return new Dimension(1, 1);
    }

    public Dimension getPreferredSize()
    {
        if(bvr != null)
            return bvr.myPreferredSize();
        else
            return super.getPreferredSize();
    }

    public synchronized void addNotify()
    {
        super.addNotify();
        if(bvr != null)
            bvr.setAvailable(true);
    }

    public synchronized void removeNotify()
    {
        if(bvr != null)
            bvr.setAvailable(false);
        super.removeNotify();
    }

    BasicVideoRenderer bvr;
}
