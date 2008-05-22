// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ToolTip.java

package com.sun.media.ui;

import java.awt.*;

public class ToolTip extends Window
{

    public ToolTip(String strText)
    {
        super(new Frame());
        arrStrings = null;
        arrStrings = new String[1];
        arrStrings[0] = new String(strText);
        Font font = new Font("Helvetica", 0, 10);
        setFont(font);
        resizePopup();
    }

    public ToolTip(String arrStrings[])
    {
        super(new Frame());
        this.arrStrings = null;
        arrStrings = new String[arrStrings.length];
        for(int i = 0; i < arrStrings.length; i++)
            arrStrings[i] = new String(arrStrings[i]);

        Font font = new Font("Helvetica", 0, 10);
        setFont(font);
        resizePopup();
    }

    public void setText(String strText)
    {
        arrStrings = new String[1];
        arrStrings[0] = new String(strText);
        resizePopup();
        repaint();
    }

    public void paint(Graphics graphics)
    {
        Rectangle rect = getBounds();
        Font font = getFont();
        FontMetrics fontMetrics = getFontMetrics(font);
        graphics.setColor(new Color(255, 255, 192));
        graphics.fillRect(0, 0, rect.width, rect.height);
        graphics.setColor(Color.black);
        graphics.drawRect(0, 0, rect.width - 1, rect.height - 1);
        int nX = 4;
        int nY = 2 + fontMetrics.getAscent();
        int nHeight = fontMetrics.getHeight();
        for(int i = 0; i < arrStrings.length; i++)
        {
            graphics.drawString(arrStrings[i], nX, nY);
            nY += nHeight;
        }

    }

    private void resizePopup()
    {
        int nWidth = 0;
        int nHeight = 0;
        Rectangle rect = getBounds();
        Font font = getFont();
        FontMetrics fontMetrics = getFontMetrics(font);
        for(int i = 0; i < arrStrings.length; i++)
        {
            int nWidthText = fontMetrics.stringWidth(arrStrings[i]);
            nWidth = Math.max(nWidth, nWidthText);
        }

        nHeight = fontMetrics.getHeight() * arrStrings.length;
        rect.width = nWidth + 8;
        rect.height = nHeight + 4;
        Dimension dim = getSize();
        if(dim.height != rect.height || rect.width > dim.width || rect.width < dim.width / 2)
            setBounds(rect);
    }

    private static final int MARGIN_HORZ = 4;
    private static final int MARGIN_VERT = 2;
    private String arrStrings[];
}
