// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TabControl.java

package com.sun.media.ui;

import java.awt.*;

class TabField
{

    public TabField(Component compOwner, Panel panelPage, String strTitle, Image image)
    {
        this.panelPage = null;
        this.strTitle = null;
        this.image = null;
        dim = new Dimension();
        rect = new Rectangle();
        nRowIndex = 0;
        MARGIN_TAB_VERT = 5;
        MARGIN_TAB_HORZ = 8;
        this.compOwner = compOwner;
        this.panelPage = panelPage;
        this.strTitle = strTitle;
        this.image = image;
    }

    public void calculateTabDimension(FontMetrics fontMetrics)
    {
        dim.width = fontMetrics.stringWidth(strTitle);
        dim.height = fontMetrics.getHeight();
        if(image != null)
        {
            dim.width += image.getWidth(compOwner) + MARGIN_TAB_HORZ;
            dim.height = Math.max(image.getHeight(compOwner), dim.height);
        }
        dim.width += 2 * MARGIN_TAB_HORZ;
        dim.height += 2 * MARGIN_TAB_VERT;
        rect.width = dim.width;
        rect.height = dim.height;
    }

    public void drawTabTop(Graphics graphics)
    {
        int arrX[] = new int[5];
        int arrY[] = new int[5];
        graphics.setColor(COLOR_TAB_BG);
        arrX[0] = rect.x + 6;
        arrY[0] = rect.y + 2;
        arrX[1] = (rect.x + rect.width) - 0;
        arrY[1] = rect.y + 2;
        arrX[2] = (rect.x + rect.width) - 0;
        arrY[2] = (rect.y + rect.height) - 2;
        arrX[3] = rect.x + 2;
        arrY[3] = (rect.y + rect.height) - 2;
        arrX[4] = rect.x + 2;
        arrY[4] = rect.y + 6;
        graphics.fillPolygon(arrX, arrY, 5);
        graphics.setColor(COLOR_TAB_SHADOW_BOTTOM);
        graphics.drawLine(rect.x, (rect.y + rect.height) - 2, rect.x, rect.y + 6);
        graphics.drawLine(rect.x, rect.y + 6, rect.x + 6, rect.y);
        graphics.drawLine(rect.x + 6, rect.y, (rect.x + rect.width) - 1, rect.y);
        graphics.drawLine(rect.x + rect.width, rect.y + 1, rect.x + rect.width, (rect.y + rect.height) - 2);
        graphics.setColor(COLOR_TAB_SHADOW_TOP);
        graphics.drawLine(rect.x + 1, (rect.y + rect.height) - 3, rect.x + 1, rect.y + 6);
        graphics.drawLine(rect.x + 1, rect.y + 6, rect.x + 6, rect.y + 1);
        graphics.drawLine(rect.x + 6, rect.y + 1, (rect.x + rect.width) - 1, rect.y + 1);
    }

    public void drawTabLeft(Graphics graphics)
    {
        int arrX[] = new int[5];
        int arrY[] = new int[5];
        graphics.setColor(COLOR_TAB_BG);
        arrX[0] = rect.x + 2;
        arrY[0] = rect.y + 6;
        arrX[1] = rect.x + 2;
        arrY[1] = (rect.y + rect.height) - 0;
        arrX[2] = (rect.x + rect.width) - 2;
        arrY[2] = (rect.y + rect.height) - 0;
        arrX[3] = (rect.x + rect.width) - 2;
        arrY[3] = rect.y + 2;
        arrX[4] = rect.x + 6;
        arrY[4] = rect.y + 2;
        graphics.fillPolygon(arrX, arrY, 5);
        graphics.setColor(COLOR_TAB_SHADOW_BOTTOM);
        graphics.drawLine((rect.x + rect.width) - 2, rect.y, rect.x + 6, rect.y);
        graphics.drawLine(rect.x + 6, rect.y, rect.x, rect.y + 6);
        graphics.drawLine(rect.x, rect.y + 6, rect.x, (rect.y + rect.height) - 1);
        graphics.drawLine(rect.x + 1, rect.y + rect.height, (rect.x + rect.width) - 2, rect.y + rect.height);
        graphics.setColor(COLOR_TAB_SHADOW_TOP);
        graphics.drawLine((rect.x + rect.width) - 3, rect.y + 1, rect.x + 6, rect.y + 1);
        graphics.drawLine(rect.x + 6, rect.y + 1, rect.x + 1, rect.y + 6);
        graphics.drawLine(rect.x + 1, rect.y + 6, rect.x + 1, (rect.y + rect.height) - 1);
    }

    public void drawCurrentTabTop(Graphics graphics)
    {
        int arrX[] = new int[5];
        int arrY[] = new int[5];
        graphics.setColor(COLOR_BG);
        arrX[0] = rect.x + 6;
        arrY[0] = rect.y + 2;
        arrX[1] = (rect.x + rect.width) - 0;
        arrY[1] = rect.y + 2;
        arrX[2] = (rect.x + rect.width) - 0;
        arrY[2] = (rect.y + rect.height) - 0;
        arrX[3] = rect.x + 2;
        arrY[3] = (rect.y + rect.height) - 0;
        arrX[4] = rect.x + 2;
        arrY[4] = rect.y + 6;
        graphics.fillPolygon(arrX, arrY, 5);
        graphics.setColor(COLOR_SHADOW_BOTTOM);
        graphics.drawLine(rect.x, (rect.y + rect.height) - 2, rect.x, rect.y + 6);
        graphics.drawLine(rect.x, rect.y + 6, rect.x + 6, rect.y);
        graphics.drawLine(rect.x + 6, rect.y, (rect.x + rect.width) - 1, rect.y);
        graphics.drawLine(rect.x + rect.width, rect.y + 1, rect.x + rect.width, (rect.y + rect.height) - 2);
        graphics.setColor(COLOR_SHADOW_TOP);
        graphics.drawLine(rect.x + 1, (rect.y + rect.height) - 2, rect.x + 1, rect.y + 6);
        graphics.drawLine(rect.x + 1, rect.y + 6, rect.x + 6, rect.y + 1);
        graphics.drawLine(rect.x + 6, rect.y + 1, (rect.x + rect.width) - 1, rect.y + 1);
    }

    public void drawCurrentTabLeft(Graphics graphics)
    {
        int arrX[] = new int[5];
        int arrY[] = new int[5];
        graphics.setColor(COLOR_BG);
        arrX[0] = rect.x + 2;
        arrY[0] = rect.y + 6;
        arrX[1] = rect.x + 2;
        arrY[1] = (rect.y + rect.height) - 0;
        arrX[2] = (rect.x + rect.width) - 0;
        arrY[2] = (rect.y + rect.height) - 0;
        arrX[3] = (rect.x + rect.width) - 0;
        arrY[3] = rect.y + 2;
        arrX[4] = rect.x + 6;
        arrY[4] = rect.y + 2;
        graphics.fillPolygon(arrX, arrY, 5);
        graphics.setColor(COLOR_SHADOW_BOTTOM);
        graphics.drawLine((rect.x + rect.width) - 2, rect.y, rect.x + 6, rect.y);
        graphics.drawLine(rect.x + 6, rect.y, rect.x, rect.y + 6);
        graphics.drawLine(rect.x, rect.y + 6, rect.x, (rect.y + rect.height) - 1);
        graphics.drawLine(rect.x + 1, rect.y + rect.height, (rect.x + rect.width) - 2, rect.y + rect.height);
        graphics.setColor(COLOR_SHADOW_TOP);
        graphics.drawLine((rect.x + rect.width) - 2, rect.y + 1, rect.x + 6, rect.y + 1);
        graphics.drawLine(rect.x + 6, rect.y + 1, rect.x + 1, rect.y + 6);
        graphics.drawLine(rect.x + 1, rect.y + 6, rect.x + 1, (rect.y + rect.height) - 1);
    }

    Panel panelPage;
    String strTitle;
    Image image;
    Dimension dim;
    Rectangle rect;
    int nRowIndex;
    Component compOwner;
    int MARGIN_TAB_VERT;
    int MARGIN_TAB_HORZ;
    public static final Color COLOR_BG;
    public static final Color COLOR_FG;
    public static final Color COLOR_SHADOW_TOP;
    public static final Color COLOR_SHADOW_BOTTOM;
    public static final Color COLOR_TAB_BG = new Color(128, 128, 128);
    public static final Color COLOR_TAB_FG;
    public static final Color COLOR_TAB_SHADOW_TOP;
    public static final Color COLOR_TAB_SHADOW_BOTTOM;

    static 
    {
        COLOR_BG = Color.lightGray;
        COLOR_FG = Color.black;
        COLOR_SHADOW_TOP = Color.white;
        COLOR_SHADOW_BOTTOM = Color.darkGray;
        COLOR_TAB_FG = Color.black;
        COLOR_TAB_SHADOW_TOP = Color.lightGray;
        COLOR_TAB_SHADOW_BOTTOM = Color.darkGray;
    }
}
