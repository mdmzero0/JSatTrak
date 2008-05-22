// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VFlowLayout.java

package com.sun.media.controls;

import java.awt.*;

public class VFlowLayout
    implements LayoutManager
{

    public VFlowLayout()
    {
        this(0);
    }

    public VFlowLayout(int v)
    {
        minWidth = 0;
        minHeight = 0;
        preferredWidth = 0;
        preferredHeight = 0;
        sizeUnknown = true;
        gap = v;
    }

    public void addLayoutComponent(String s, Component component)
    {
    }

    public void removeLayoutComponent(Component component)
    {
    }

    private void setSizes(Container parent)
    {
        int nComps = parent.countComponents();
        Dimension d = null;
        preferredWidth = 0;
        preferredHeight = 0;
        minWidth = 0;
        minHeight = 0;
        for(int i = 0; i < nComps; i++)
        {
            Component c = parent.getComponent(i);
            if(c.isVisible())
            {
                d = c.preferredSize();
                minWidth = Math.max(c.minimumSize().width, minWidth);
                preferredWidth = Math.max(c.preferredSize().width, preferredWidth);
                minHeight += c.minimumSize().height + gap;
                preferredHeight += c.preferredSize().height + gap;
            }
        }

    }

    public Dimension preferredLayoutSize(Container parent)
    {
        Dimension dim = new Dimension(0, 0);
        int nComps = parent.countComponents();
        setSizes(parent);
        Insets insets = parent.insets();
        dim.width = preferredWidth + insets.left + insets.right;
        dim.height = preferredHeight + insets.top + insets.bottom;
        sizeUnknown = false;
        return dim;
    }

    public Dimension minimumLayoutSize(Container parent)
    {
        Dimension dim = new Dimension(0, 0);
        int nComps = parent.countComponents();
        setSizes(parent);
        Insets insets = parent.insets();
        dim.width = minWidth + insets.left + insets.right;
        dim.height = minHeight + insets.top + insets.bottom;
        sizeUnknown = false;
        return dim;
    }

    public void layoutContainer(Container parent)
    {
        Insets insets = parent.insets();
        int maxWidth = parent.size().width - (insets.left + insets.right);
        int maxHeight = parent.size().height - (insets.top + insets.bottom);
        int nComps = parent.countComponents();
        if(sizeUnknown)
            setSizes(parent);
        int previousWidth = 0;
        int previousHeight = 0;
        int x = 0;
        int y = insets.top + gap / 2;
        int rowh = 0;
        int start = 0;
        int yFudge = 0;
        boolean oneColumn = false;
        if(sizeUnknown)
            setSizes(parent);
        if(maxHeight > preferredHeight)
            yFudge = (maxHeight - preferredHeight) / nComps;
        for(int i = 0; i < nComps; i++)
        {
            Component c = parent.getComponent(i);
            if(c.isVisible())
            {
                Dimension d = c.preferredSize();
                if(i != 0)
                    y += previousHeight + yFudge + gap;
                else
                    y += previousHeight + (yFudge + gap) / 2;
                c.reshape(0, y, maxWidth, d.height);
                previousWidth = d.width;
                previousHeight = d.height;
            }
        }

    }

    public String toString()
    {
        return getClass().getName() + "[gap=" + gap + "]";
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1999.";
    private int gap;
    private int minWidth;
    private int minHeight;
    private int preferredWidth;
    private int preferredHeight;
    private boolean sizeUnknown;
}
