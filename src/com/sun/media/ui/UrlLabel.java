// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PropertySheet.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;

class UrlLabel extends Label
    implements ComponentListener
{

    public UrlLabel(String strLabel)
    {
        super(strLabel);
        this.strLabel = null;
        this.strLabel = strLabel;
        addComponentListener(this);
    }

    public synchronized void setText(String strLabel)
    {
        this.strLabel = strLabel;
        setInternalLabel();
    }

    public String getText()
    {
        return strLabel;
    }

    public String toString()
    {
        return strLabel;
    }

    private void setInternalLabel()
    {
        Rectangle rect = getBounds();
        java.awt.Font font = getFont();
        FontMetrics fontMetrics = getFontMetrics(font);
        String strLabel = this.strLabel;
        int nWidth = fontMetrics.stringWidth(strLabel);
        int nIndex1 = this.strLabel.lastIndexOf(File.separatorChar);
        for(int nIndex2 = nIndex1; nIndex2 >= 0 && nWidth > rect.width; nWidth = fontMetrics.stringWidth(strLabel))
        {
            nIndex2 = this.strLabel.lastIndexOf(File.separatorChar, nIndex2 - 1);
            if(nIndex2 < 0)
                strLabel = "..." + this.strLabel.substring(nIndex1);
            else
                strLabel = this.strLabel.substring(0, nIndex2 + 1) + "..." + this.strLabel.substring(nIndex1);
        }

        super.setText(strLabel);
    }

    public void componentResized(ComponentEvent event)
    {
        setInternalLabel();
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

    private String strLabel;
}
