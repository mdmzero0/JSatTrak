// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ColumnList.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

// Referenced classes of package com.sun.media.ui:
//            ColumnData, RowData

public class ColumnList extends Canvas
    implements MouseListener, FocusListener, KeyListener, ComponentListener
{

    public ColumnList(String arrColumnNames[])
    {
        vectorColumns = new Vector();
        vectorRows = new Vector();
        boolFocus = false;
        boolSetColumnWidthAsPreferred = false;
        nScrollPosHorz = 0;
        nScrollPosVert = 0;
        nCurrentIndex = 0;
        nVisibleRows = 1;
        fontHeader = new Font("Dialog", 0, 12);
        fontItem = new Font("Dialog", 0, 12);
        int nCount = arrColumnNames.length;
        for(int i = 0; i < nCount; i++)
        {
            ColumnData column = new ColumnData(arrColumnNames[i], 3);
            vectorColumns.addElement(column);
        }

        try
        {
            init();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void addRow(Object arrValues[])
    {
        RowData rowData = new RowData(arrValues);
        vectorRows.addElement(rowData);
        repaint();
    }

    public void removeRow(int nRowIndex)
    {
        vectorRows.removeElementAt(nRowIndex);
        repaint();
    }

    public void setCellValue(Object value, int nRowIndex, int nColumnIndex)
    {
        RowData rowData = (RowData)vectorRows.elementAt(nRowIndex);
        rowData.setValue(value, nColumnIndex);
        repaint();
    }

    public void setColumnWidth(int nWidth, int nColumnIndex)
    {
        ColumnData columnData = (ColumnData)vectorColumns.elementAt(nColumnIndex);
        columnData.nWidth = nWidth;
        repaint();
    }

    public void setColumnWidth(int nWidth)
    {
        int nCount = vectorColumns.size();
        for(int i = 0; i < nCount; i++)
        {
            ColumnData columnData = (ColumnData)vectorColumns.elementAt(i);
            columnData.nWidth = nWidth;
        }

        repaint();
    }

    public void setColumnWidthAsPreferred(int nColumnIndex)
    {
        ColumnData columnData = (ColumnData)vectorColumns.elementAt(nColumnIndex);
        int nWidth = getPreferredColumnWidth(nColumnIndex);
        columnData.nWidth = nWidth;
        repaint();
    }

    public void setColumnWidthAsPreferred()
    {
        int nCount = vectorColumns.size();
        int nWidthTotal = 0;
        for(int i = 0; i < nCount; i++)
        {
            ColumnData columnData = (ColumnData)vectorColumns.elementAt(i);
            int nWidth = getPreferredColumnWidth(i);
            columnData.nWidth = nWidth;
            nWidthTotal += nWidth;
        }

        Rectangle rect = getBounds();
        if(rect.width < 1)
            boolSetColumnWidthAsPreferred = true;
        rect.width -= 2;
        if(rect.width > nWidthTotal)
        {
            int nWidthExtra = (rect.width - nWidthTotal) / nCount;
            nWidthTotal = rect.width;
            for(int i = 0; i < nCount; i++)
            {
                ColumnData columnData = (ColumnData)vectorColumns.elementAt(i);
                if(i < nCount - 1)
                    columnData.nWidth += nWidthExtra;
                else
                    columnData.nWidth = nWidthTotal;
                nWidthTotal -= columnData.nWidth;
            }

        }
        repaint();
    }

    public Dimension getPreferredSize()
    {
        Dimension dim = new Dimension();
        dim.height += nHeightHeader;
        int nHeight = nHeightRow;
        nHeight *= vectorRows.size();
        dim.height += nHeight;
        int nCount = vectorColumns.size();
        for(int i = 0; i < nCount; i++)
        {
            int nWidth = getPreferredColumnWidth(i);
            dim.width += nWidth;
        }

        dim.width += 3;
        dim.height += 3;
        return dim;
    }

    public boolean isFocusTraversable()
    {
        return true;
    }

    public void update(Graphics g)
    {
        Rectangle rectClient = getBounds();
        Image image = createImage(rectClient.width, rectClient.height);
        Graphics graphics;
        if(image != null)
            graphics = image.getGraphics();
        else
            graphics = g;
        paint(graphics);
        if(image != null)
            g.drawImage(image, 0, 0, this);
    }

    public void paint(Graphics graphics)
    {
        Rectangle rectClient = getBounds();
        rectClient.x = 0;
        rectClient.y = 0;
        Rectangle rect = new Rectangle(rectClient);
        super.paint(graphics);
        graphics.setColor(COLOR_SHADOW_BOTTOM);
        graphics.drawRect(rect.x, rect.y, rect.width - 2, rect.height - 2);
        graphics.setColor(COLOR_SHADOW_TOP);
        graphics.drawLine((rect.x + rect.width) - 1, rect.y + 1, (rect.x + rect.width) - 1, (rect.y + rect.height) - 1);
        graphics.drawLine(rect.x + 1, (rect.y + rect.height) - 1, (rect.x + rect.width) - 1, (rect.y + rect.height) - 1);
        int nColCount = vectorColumns.size();
        int nRowCount = vectorRows.size();
        rect.x++;
        rect.y++;
        int nStartX = rect.x;
        FontMetrics fontMetrics = getFontMetrics(fontHeader);
        graphics.setFont(fontHeader);
        int nHeight = fontMetrics.getHeight();
        rect.height = nHeightHeader;
        for(int i = nScrollPosHorz; i < nColCount; i++)
        {
            ColumnData columnData = (ColumnData)vectorColumns.elementAt(i);
            rect.width = columnData.nWidth;
            if(rect.x + rect.width > (rectClient.x + rectClient.width) - 1)
                rect.width = (rectClient.x + rectClient.width) - 1 - rect.x;
            graphics.setColor(COLOR_HEADER_BG);
            graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
            graphics.setColor(COLOR_SHADOW_TOP);
            graphics.drawLine(rect.x, rect.y, rect.x, (rect.y + rect.height) - 2);
            graphics.drawLine(rect.x, rect.y, (rect.x + rect.width) - 2, rect.y);
            graphics.setColor(COLOR_SHADOW_BOTTOM);
            graphics.drawLine((rect.x + rect.width) - 1, rect.y + 1, (rect.x + rect.width) - 1, (rect.y + rect.height) - 1);
            graphics.drawLine(rect.x + 1, (rect.y + rect.height) - 1, (rect.x + rect.width) - 1, (rect.y + rect.height) - 1);
            String strValue = columnData.strName;
            int nLength = strValue.length();
            int nWidth;
            for(nWidth = fontMetrics.stringWidth(strValue); nWidth > rect.width - 12 && nLength > 0; nWidth = fontMetrics.stringWidth(strValue))
            {
                nLength--;
                strValue = strValue.substring(0, nLength) + "...";
            }

            int nX = rect.x + (rect.width - nWidth) / 2;
            int nY = (rect.y + rect.height) - (rect.height - nHeight) / 2 - fontMetrics.getMaxDescent();
            graphics.setColor(getForeground());
            graphics.drawString(strValue, nX, nY);
            rect.x += rect.width;
        }

        Font font = getFont();
        fontMetrics = getFontMetrics(font);
        graphics.setFont(font);
        nHeight = fontMetrics.getHeight();
        rect.y += rect.height;
        rect.height = nHeightRow;
        for(int j = nScrollPosVert; j < nRowCount; j++)
        {
            rect.x = nStartX;
            if(j == nCurrentIndex)
            {
                rect.width = rectClient.width - 3;
                graphics.setColor(COLOR_SEL_BG);
                graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
                graphics.setColor(COLOR_SEL_FG);
                if(boolFocus)
                {
                    drawDottedLine(graphics, rect.x, rect.y, (rect.x + rect.width) - 1, rect.y);
                    drawDottedLine(graphics, rect.x, (rect.y + rect.height) - 1, (rect.x + rect.width) - 1, (rect.y + rect.height) - 1);
                }
            } else
            {
                graphics.setColor(getForeground());
            }
            RowData rowData = (RowData)vectorRows.elementAt(j);
            for(int i = nScrollPosHorz; i < nColCount; i++)
            {
                ColumnData columnData = (ColumnData)vectorColumns.elementAt(i);
                rect.width = columnData.nWidth;
                if(rect.x + rect.width > (rectClient.x + rectClient.width) - 1)
                    rect.width = (rectClient.x + rectClient.width) - 1 - rect.x;
                String strValue = rowData.getValue(i).toString();
                int nLength = strValue.length();
                for(int nWidth = fontMetrics.stringWidth(strValue); nWidth > rect.width - 12 && nLength > 0; nWidth = fontMetrics.stringWidth(strValue))
                {
                    nLength--;
                    strValue = strValue.substring(0, nLength) + "...";
                }

                int nX = rect.x + 6;
                int nY = (rect.y + rect.height) - (rect.height - nHeight) / 2 - fontMetrics.getMaxDescent();
                graphics.drawString(strValue, nX, nY);
                rect.x += rect.width;
            }

            rect.y += rect.height;
        }

    }

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public void mousePressed(MouseEvent event)
    {
        int x = event.getX();
        int y = event.getY();
        y -= 1 + nHeightHeader;
        if(y >= 0)
        {
            int nIndex = y / nHeightRow;
            if(nIndex >= 0 && nIndex < vectorRows.size() - nScrollPosVert)
                nCurrentIndex = nIndex + nScrollPosVert;
        }
        requestFocus();
        repaint();
    }

    public void mouseReleased(MouseEvent mouseevent)
    {
    }

    public void mouseEntered(MouseEvent mouseevent)
    {
    }

    public void mouseExited(MouseEvent mouseevent)
    {
    }

    public void keyTyped(KeyEvent keyevent)
    {
    }

    public void keyPressed(KeyEvent event)
    {
        int nKeyCode = event.getKeyCode();
        int nIndex = nCurrentIndex;
        if(nKeyCode == 40)
            nIndex++;
        else
        if(nKeyCode == 38)
            nIndex--;
        else
        if(nKeyCode == 36)
            nIndex = 0;
        else
        if(nKeyCode == 35)
            nIndex = vectorRows.size() - 1;
        else
        if(nKeyCode == 33)
            nIndex -= nVisibleRows;
        else
        if(nKeyCode == 34)
            nIndex += nVisibleRows;
        if(nIndex > vectorRows.size() - 1)
            nIndex = vectorRows.size() - 1;
        if(nIndex < 0)
            nIndex = 0;
        if(nIndex != nCurrentIndex)
        {
            nCurrentIndex = nIndex;
            if(nScrollPosVert + nVisibleRows < nCurrentIndex)
                nScrollPosVert = (nCurrentIndex - nVisibleRows) + 1;
            if(nScrollPosVert > nCurrentIndex)
                nScrollPosVert = nCurrentIndex;
            repaint();
        }
    }

    public void keyReleased(KeyEvent keyevent)
    {
    }

    public void focusGained(FocusEvent event)
    {
        if(boolFocus)
        {
            return;
        } else
        {
            boolFocus = true;
            repaint();
            return;
        }
    }

    public void focusLost(FocusEvent event)
    {
        if(!boolFocus)
        {
            return;
        } else
        {
            boolFocus = false;
            repaint();
            return;
        }
    }

    public void componentResized(ComponentEvent event)
    {
        if(boolSetColumnWidthAsPreferred)
        {
            boolSetColumnWidthAsPreferred = false;
            setColumnWidthAsPreferred();
        }
        Rectangle rect = getBounds();
        rect.height -= 3 + nHeightHeader;
        nVisibleRows = rect.height / nHeightRow;
        if(nVisibleRows < 1)
            nVisibleRows = 1;
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

    private void init()
        throws Exception
    {
        setFont(fontItem);
        computeHeights();
        setBackground(Color.white);
        addMouseListener(this);
        addKeyListener(this);
        addFocusListener(this);
        addComponentListener(this);
    }

    private int getPreferredColumnWidth(int nColumnIndex)
    {
        ColumnData columnData = (ColumnData)vectorColumns.elementAt(nColumnIndex);
        FontMetrics fontMetrics = getFontMetrics(fontHeader);
        String strValue = columnData.strName;
        int nWidthMax = fontMetrics.stringWidth(strValue) + 12 + 2;
        Font font = getFont();
        fontMetrics = getFontMetrics(font);
        int nCount = vectorRows.size();
        for(int i = 0; i < nCount; i++)
        {
            RowData rowData = (RowData)vectorRows.elementAt(i);
            strValue = rowData.getValue(nColumnIndex).toString();
            int nWidth = fontMetrics.stringWidth(strValue) + 12;
            nWidthMax = Math.max(nWidthMax, nWidth);
        }

        return nWidthMax;
    }

    private void computeHeights()
    {
        FontMetrics fontMetrics = getFontMetrics(fontHeader);
        nHeightHeader = fontMetrics.getHeight();
        nHeightHeader += 2;
        nHeightHeader += 4;
        Font font = getFont();
        fontMetrics = getFontMetrics(font);
        nHeightRow = fontMetrics.getHeight();
        nHeightRow += 4;
    }

    private void drawDottedLine(Graphics graphics, int nX1, int nY1, int nX2, int nY2)
    {
        if(nX1 == nX2 && nY1 == nY2)
        {
            drawDot(graphics, nX1, nY1);
            return;
        }
        if(nX1 > nX2)
        {
            int nX = nX1;
            nX1 = nX2;
            nX2 = nX;
        }
        if(nY1 > nY2)
        {
            int nY = nY1;
            nY1 = nY2;
            nY2 = nY;
        }
        if(nX2 - nX1 > nY2 - nY1)
        {
            double dDiv = (double)(nY2 - nY1) / (double)(nX2 - nX1);
            for(int nX = nX1; nX <= nX2; nX++)
            {
                int nY = (int)Math.rint((double)nY1 + (double)(nX - nX1) * dDiv);
                drawDot(graphics, nX, nY);
            }

        } else
        {
            double dDiv = (nX2 - nX1) / (nY2 - nY1);
            for(int nY = nY1; nY <= nY2; nY++)
            {
                int nX = (int)Math.rint((double)nX1 + (double)(nY - nY1) * dDiv);
                drawDot(graphics, nX, nY);
            }

        }
    }

    private void drawDot(Graphics graphics, int nX, int nY)
    {
        if((nX + nY) % 2 == 0)
            graphics.drawLine(nX, nY, nX, nY);
    }

    public static final int TYPE_INTEGER = 1;
    public static final int TYPE_DOUBLE = 2;
    public static final int TYPE_STRING = 3;
    public static final int TYPE_DATE = 4;
    private static final int MARGIN_VERT = 2;
    private static final int MARGIN_HORZ = 6;
    private static final Color COLOR_HEADER_BG;
    private static final Color COLOR_HEADER_FG;
    private static final Color COLOR_SHADOW_TOP;
    private static final Color COLOR_SHADOW_BOTTOM;
    private static final Color COLOR_SEL_BG;
    private static final Color COLOR_SEL_FG;
    private Vector vectorColumns;
    private Vector vectorRows;
    private boolean boolFocus;
    private boolean boolSetColumnWidthAsPreferred;
    private int nScrollPosHorz;
    private int nScrollPosVert;
    private int nCurrentIndex;
    private int nVisibleRows;
    private Font fontHeader;
    private Font fontItem;
    private int nHeightHeader;
    private int nHeightRow;

    static 
    {
        COLOR_HEADER_BG = Color.lightGray;
        COLOR_HEADER_FG = Color.black;
        COLOR_SHADOW_TOP = Color.white;
        COLOR_SHADOW_BOTTOM = Color.darkGray;
        COLOR_SEL_BG = Color.white;
        COLOR_SEL_FG = Color.black;
    }
}
