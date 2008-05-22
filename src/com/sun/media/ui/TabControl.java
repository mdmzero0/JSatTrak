// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TabControl.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

// Referenced classes of package com.sun.media.ui:
//            TabField

public class TabControl extends Panel
    implements MouseListener, FocusListener, KeyListener, ComponentListener
{

    public TabControl()
    {
        this(0);
    }

    public TabControl(int nAlignment)
    {
        layoutCard = new CardLayout();
        nCurrentPage = -1;
        boolFocus = false;
        this.nAlignment = 0;
        MARGIN_PAGE_VERT = 6;
        MARGIN_PAGE_HORZ = 6;
        strPageToShowAfterPaint = null;
        cursorNormal = new Cursor(0);
        cursorWait = new Cursor(3);
        vectorTabs = new Vector();
        nTabHeightMax = 1;
        nTabWidthMax = 1;
        nRowCount = 1;
        this.nAlignment = nAlignment;
        try
        {
            init();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void init()
        throws Exception
    {
        setLayout(new BorderLayout());
        addComponentListener(this);
        addMouseListener(this);
        buttonFocus = new Button("Focus");
        add(buttonFocus);
        buttonFocus.addKeyListener(this);
        buttonFocus.addFocusListener(this);
        panelPageContainer = new Panel(layoutCard);
        add(panelPageContainer, "Center");
        Font fontOld = panelPageContainer.getFont();
        if(fontOld == null)
            fontOld = new Font("Dialog", 0, 12);
        Font font = new Font("Dialog", 0, 12);
        setFont(font);
        panelPageContainer.setFont(fontOld);
        setBackground(TabField.COLOR_BG);
        panelPageContainer.setBackground(TabField.COLOR_BG);
    }

    public int addPage(Panel panelPage, String strTitle)
    {
        int nIndex = addPage(panelPage, strTitle, null);
        return nIndex;
    }

    public int addPage(Panel panelPage, String strTitle, Image image)
    {
        int nIndex = vectorTabs.size();
        TabField tabField = new TabField(this, panelPage, strTitle, image);
        vectorTabs.addElement(tabField);
        panelPageContainer.add(panelPage, strTitle);
        if(nIndex == 0)
        {
            nCurrentPage = 0;
            layoutCard.show(panelPageContainer, strTitle);
        }
        tabField.calculateTabDimension(getFontMetrics(getFont()));
        nTabHeightMax = Math.max(tabField.dim.height, nTabHeightMax);
        nTabWidthMax = Math.max(tabField.dim.width, nTabWidthMax);
        recalculateTabs();
        repaint();
        return nIndex;
    }

    public int setPageImage(Panel panelPage, Image imageTab)
    {
        int nIndex = findPage(panelPage);
        if(nIndex < 0 || nIndex >= vectorTabs.size())
            return nIndex;
        TabField tabField = (TabField)vectorTabs.elementAt(nIndex);
        if(tabField.image == imageTab)
            return nIndex;
        tabField.image = imageTab;
        nTabHeightMax = 1;
        nTabWidthMax = 1;
        int nCount = vectorTabs.size();
        for(int i = 0; i < nCount; i++)
        {
            tabField = (TabField)vectorTabs.elementAt(i);
            tabField.calculateTabDimension(getFontMetrics(getFont()));
            nTabHeightMax = Math.max(tabField.dim.height, nTabHeightMax);
            nTabWidthMax = Math.max(tabField.dim.width, nTabWidthMax);
        }

        recalculateTabs();
        repaint();
        return nIndex;
    }

    public Dimension getPreferredSize()
    {
        Dimension dim = super.getPreferredSize();
        if(nAlignment == 1)
        {
            dim.height = Math.max(dim.height, nTabHeightMax * vectorTabs.size() + 1);
        } else
        {
            int nRowWidth = 0;
            for(int i = 0; i < vectorTabs.size(); i++)
            {
                TabField tabField = (TabField)vectorTabs.elementAt(i);
                nRowWidth += tabField.dim.width;
            }

            dim.width = Math.max(dim.width, nRowWidth + 1);
        }
        return dim;
    }

    public Insets getInsets()
    {
        Insets insets = super.getInsets();
        if(nAlignment == 1)
            insets = new Insets(insets.top + MARGIN_PAGE_VERT, ((insets.left + nRowCount * nTabWidthMax) - 2) + MARGIN_PAGE_HORZ, insets.bottom + MARGIN_PAGE_VERT, insets.right + MARGIN_PAGE_HORZ);
        else
            insets = new Insets(((insets.top + nRowCount * nTabHeightMax) - 2) + MARGIN_PAGE_VERT, insets.left + MARGIN_PAGE_HORZ, insets.bottom + MARGIN_PAGE_VERT, insets.right + MARGIN_PAGE_HORZ);
        return insets;
    }

    public void update(Graphics g)
    {
        Rectangle rectClient = getBounds();
        if(rectClient.width < 1 || rectClient.height < 1)
            return;
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
        super.paint(graphics);
        Rectangle rectClient = getBounds();
        rectClient.x = 0;
        rectClient.y = 0;
        Rectangle rect = new Rectangle(rectClient);
        if(nAlignment == 1)
        {
            rect.x += nTabWidthMax * nRowCount - 2;
            rect.width -= nTabWidthMax * nRowCount - 2;
        } else
        {
            rect.y += nTabHeightMax * nRowCount - 2;
            rect.height -= nTabHeightMax * nRowCount - 2;
        }
        rect.width--;
        rect.height--;
        graphics.setColor(TabField.COLOR_SHADOW_BOTTOM);
        graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
        graphics.setColor(TabField.COLOR_SHADOW_TOP);
        graphics.drawLine(rect.x + 1, rect.y + 1, rect.x + 1, (rect.y + rect.height) - 2);
        graphics.drawLine(rect.x + 1, rect.y + 1, (rect.x + rect.width) - 2, rect.y + 1);
        Font fontNormal = getFont();
        Font fontBold = fontNormal;
        int nSize = vectorTabs.size();
        for(int i = nSize - 1; i >= 0; i--)
        {
            TabField tabField = (TabField)vectorTabs.elementAt(i);
            if(i == nCurrentPage)
            {
                if(nAlignment == 1)
                    tabField.drawCurrentTabLeft(graphics);
                else
                    tabField.drawCurrentTabTop(graphics);
            } else
            if(nAlignment == 1)
                tabField.drawTabLeft(graphics);
            else
                tabField.drawTabTop(graphics);
            graphics.setColor(getForeground());
            Rectangle rectString = new Rectangle(tabField.rect);
            if(nAlignment == 1)
                rectString.width = nTabWidthMax;
            else
                rectString.height = nTabHeightMax;
            if(tabField.image != null)
            {
                int nWidth = tabField.image.getWidth(this);
                rectString.x += nWidth + tabField.MARGIN_TAB_HORZ;
                rectString.width -= nWidth + tabField.MARGIN_TAB_HORZ;
            }
            rectString.y++;
            FontMetrics fontMetrics;
            int nX;
            int nY;
            if(i == nCurrentPage)
            {
                graphics.setFont(fontBold);
                fontMetrics = graphics.getFontMetrics(fontBold);
                if(boolFocus)
                {
                    int nWidth = fontMetrics.stringWidth(tabField.strTitle) + 6;
                    int nHeight = fontMetrics.getHeight() + 1;
                    nX = rectString.x + (rectString.width - nWidth) / 2;
                    nY = rectString.y + (nTabHeightMax - 2 - nHeight) / 2 + 1;
                    drawDottedRectangle(graphics, nX, nY, nWidth, nHeight);
                }
            } else
            {
                graphics.setFont(fontNormal);
                fontMetrics = graphics.getFontMetrics(fontNormal);
            }
            nX = rectString.x + (rectString.width - fontMetrics.stringWidth(tabField.strTitle)) / 2;
            nY = (rectString.y + rectString.height) - (rectString.height - fontMetrics.getHeight()) / 2 - fontMetrics.getMaxDescent();
            nY--;
            if(i != nCurrentPage)
            {
                nX++;
                nY++;
            }
            graphics.drawString(tabField.strTitle, nX, nY);
            if(tabField.image != null)
            {
                int nHeight = tabField.image.getHeight(this);
                nX = tabField.rect.x + tabField.MARGIN_TAB_HORZ;
                nY = tabField.rect.y + (nTabHeightMax - nHeight) / 2;
                if(i != nCurrentPage)
                {
                    nX++;
                    nY++;
                }
                graphics.drawImage(tabField.image, nX, nY, this);
            }
        }

        if(strPageToShowAfterPaint != null)
        {
            layoutCard.show(panelPageContainer, strPageToShowAfterPaint);
            strPageToShowAfterPaint = null;
            setCursor(cursorNormal);
        }
    }

    private void drawDottedRectangle(Graphics graphics, int nX, int nY, int nWidth, int nHeight)
    {
        drawDottedLine(graphics, nX, nY, (nX + nWidth) - 1, nY);
        drawDottedLine(graphics, (nX + nWidth) - 1, nY, (nX + nWidth) - 1, (nY + nHeight) - 1);
        drawDottedLine(graphics, (nX + nWidth) - 1, (nY + nHeight) - 1, nX, (nY + nHeight) - 1);
        drawDottedLine(graphics, nX, (nY + nHeight) - 1, nX, nY);
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

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public void mousePressed(MouseEvent event)
    {
        int x = event.getX();
        int y = event.getY();
        int nTabCount = vectorTabs.size();
        for(int i = 0; i < nTabCount; i++)
        {
            TabField tabField = (TabField)vectorTabs.elementAt(i);
            if(!tabField.rect.contains(x, y))
                continue;
            buttonFocus.requestFocus();
            nCurrentPage = i;
            strPageToShowAfterPaint = tabField.strTitle;
            setCursor(cursorWait);
            repaint();
            break;
        }

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
        int nIndex = nCurrentPage;
        int nKeyCode = event.getKeyCode();
        if(nKeyCode == 40 || nKeyCode == 39)
            nIndex++;
        else
        if(nKeyCode == 38 || nKeyCode == 37)
            nIndex--;
        if(nIndex >= vectorTabs.size())
            nIndex = vectorTabs.size() - 1;
        if(nIndex < 0)
            nIndex = 0;
        if(nCurrentPage != nIndex)
        {
            nCurrentPage = nIndex;
            TabField tabField = (TabField)vectorTabs.elementAt(nIndex);
            strPageToShowAfterPaint = tabField.strTitle;
            setCursor(cursorWait);
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
        recalculateTabs();
        doLayout();
        panelPageContainer.validate();
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

    private void recalculateTabs()
    {
        int nRowSize = 1;
        Rectangle rectClient = getBounds();
        rectClient.x = 0;
        rectClient.y = 0;
        if(rectClient.width < 1 || rectClient.height < 1)
            return;
        int nTabCount = vectorTabs.size();
        if(nAlignment == 1)
        {
            nRowSize = rectClient.height / nTabHeightMax;
            nRowCount = ((nTabCount + nRowSize) - 1) / nRowSize;
            int nOffsetX = nRowCount * nTabWidthMax;
            int nOffsetY = 0;
            for(int i = 0; i < nTabCount; i++)
            {
                if(i % nRowSize == 0)
                {
                    nOffsetX -= nTabWidthMax;
                    nOffsetY = 0;
                }
                TabField tabField = (TabField)vectorTabs.elementAt(i);
                tabField.rect.x = nOffsetX;
                tabField.rect.y = nOffsetY;
                tabField.rect.width = nTabWidthMax * (i / nRowSize + 1);
                tabField.rect.height = nTabHeightMax;
                tabField.nRowIndex = i / nRowSize;
                if(tabField.nRowIndex > 0)
                    tabField.rect.width -= 2;
                nOffsetY += nTabHeightMax;
            }

        } else
        {
            nRowCount = 1;
            int nRowWidth = 0;
            for(int i = 0; i < nTabCount; i++)
            {
                TabField tabField = (TabField)vectorTabs.elementAt(i);
                if(nRowWidth + tabField.dim.width > rectClient.width)
                {
                    nRowWidth = 0;
                    nRowCount++;
                }
                nRowWidth += tabField.dim.width;
            }

            int nOffsetX = 0;
            int nOffsetY = nRowCount * nTabHeightMax;
            int nRowIndex = 0;
            int j = 0;
            for(int i = 0; i < nTabCount; i++)
            {
                TabField tabField;
                if(i == j)
                {
                    nOffsetX = 0;
                    nOffsetY -= nTabHeightMax;
                    nRowWidth = 0;
                    for(j = i; j < nTabCount; j++)
                    {
                        tabField = (TabField)vectorTabs.elementAt(j);
                        if(j > i && nRowWidth + tabField.dim.width > rectClient.width)
                            break;
                        nRowWidth += tabField.dim.width;
                        tabField.nRowIndex = nRowIndex;
                    }

                    nRowSize = j - i;
                    nRowIndex++;
                }
                tabField = (TabField)vectorTabs.elementAt(i);
                tabField.rect.x = nOffsetX;
                tabField.rect.y = nOffsetY;
                tabField.rect.width = tabField.dim.width;
                if(nRowCount > 1 && nRowIndex < nRowCount)
                {
                    tabField.rect.width += (rectClient.width - nRowWidth - 1) / nRowSize;
                    tabField.rect.width += j - i <= (rectClient.width - nRowWidth - 1) % nRowSize ? 1 : 0;
                }
                tabField.rect.height = nTabHeightMax * nRowIndex;
                if(tabField.nRowIndex > 0)
                    tabField.rect.height -= 2;
                nOffsetX += tabField.rect.width;
            }

        }
        repaint();
    }

    private int findPage(Panel panelPage)
    {
        int i;
        for(i = vectorTabs.size() - 1; i >= 0; i--)
        {
            TabField tabField = (TabField)vectorTabs.elementAt(i);
            if(tabField.panelPage == panelPage)
                break;
        }

        return i;
    }

    public static final int ALIGN_TOP = 0;
    public static final int ALIGN_LEFT = 1;
    private Panel panelPageContainer;
    private CardLayout layoutCard;
    private int nCurrentPage;
    private Button buttonFocus;
    private boolean boolFocus;
    private int nAlignment;
    private int MARGIN_PAGE_VERT;
    private int MARGIN_PAGE_HORZ;
    private String strPageToShowAfterPaint;
    private Cursor cursorNormal;
    private Cursor cursorWait;
    private Vector vectorTabs;
    private int nTabHeightMax;
    private int nTabWidthMax;
    private int nRowCount;
}
