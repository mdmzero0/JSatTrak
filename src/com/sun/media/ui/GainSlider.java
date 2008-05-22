// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DefaultControlPanel.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.*;
import javax.media.*;

// Referenced classes of package com.sun.media.ui:
//            PopupThread, BasicComp

class GainSlider extends Window
    implements GainChangeListener, MouseListener, MouseMotionListener, FocusListener
{

    public GainSlider(GainControl gainControl)
    {
        this(gainControl, new Frame());
    }

    public GainSlider(GainControl gainControl, Frame frame)
    {
        super(frame);
        imageGrabber = null;
        dimGrabber = new Dimension();
        boolFocus = false;
        pressed = false;
        threadPopup = null;
        imageBackground = null;
        this.gainControl = gainControl;
        try
        {
            init();
        }
        catch(Exception exception) { }
    }

    public void dispose()
    {
        gainControl = null;
    }

    public void addNotify()
    {
        super.addNotify();
        Insets insets = getInsets();
        setSize(80 + insets.left + insets.right, 20 + insets.top + insets.bottom);
    }

    private void init()
        throws Exception
    {
        gainControl.addGainChangeListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setLayout(null);
        imageBackground = BasicComp.fetchImage("texture3.gif");
        buttonFocus = new Button("Focus");
        buttonFocus.setBounds(-100, -100, 80, 24);
        add(buttonFocus);
        buttonFocus.addFocusListener(this);
        imageGrabber = BasicComp.fetchImage("grabber.gif");
        setBackground(Color.lightGray);
        setSize(80, 20);
    }

    public void setVisible(boolean boolVisible)
    {
        super.setVisible(boolVisible);
        if(boolVisible)
        {
            buttonFocus.requestFocus();
            if(threadPopup != null)
                threadPopup.stopNormaly();
            threadPopup = new PopupThread(this);
            threadPopup.resetCounter(3);
            threadPopup.start();
        } else
        if(threadPopup != null)
            threadPopup.stopNormaly();
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
        paintBackground(graphics);
        Dimension dimSize = getSize();
        Insets insets = getInsets();
        Rectangle rect = new Rectangle(insets.left, insets.top, dimSize.width - insets.left - insets.right, dimSize.height - insets.top - insets.bottom);
        graphics.setColor(getBackground());
        graphics.draw3DRect(rect.x, rect.y, rect.width - 1, rect.height - 1, true);
        graphics.draw3DRect(rect.x + 4, (rect.y + rect.height / 2) - 2, rect.width - 9, 3, false);
        if(dimGrabber.width < 1)
            dimGrabber.width = imageGrabber.getWidth(this);
        if(dimGrabber.height < 1)
            dimGrabber.height = imageGrabber.getHeight(this);
        float levelGain = gainControl.getLevel();
        int x = rect.x + (int)(2.0F + levelGain * (float)(rect.width - 5 - dimGrabber.width));
        int y = rect.y + (rect.height - dimGrabber.height) / 2;
        graphics.drawImage(imageGrabber, x, y, this);
    }

    private void paintBackground(Graphics graphics)
    {
        Dimension dimSize = getSize();
        if(imageBackground == null)
        {
            graphics.setColor(getBackground());
            graphics.fillRect(0, 0, dimSize.width, dimSize.height);
        } else
        {
            Rectangle rectTile = new Rectangle(0, 0, imageBackground.getWidth(this), imageBackground.getHeight(this));
            Rectangle rectClip = graphics.getClipBounds();
            for(; rectTile.y < dimSize.height; rectTile.y += rectTile.height)
            {
                while(rectTile.x < dimSize.width) 
                {
                    if(rectClip == null || rectClip.intersects(rectTile))
                        graphics.drawImage(imageBackground, rectTile.x, rectTile.y, this);
                    rectTile.x += rectTile.width;
                }
                rectTile.x = 0;
            }

        }
    }

    public void gainChange(GainChangeEvent event)
    {
        repaint();
    }

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public void mousePressed(MouseEvent event)
    {
        if(threadPopup != null)
            threadPopup.resetCounter(3);
        Point pointMouse = event.getPoint();
        setLevelToMouse(pointMouse);
        pressed = true;
    }

    public void mouseReleased(MouseEvent event)
    {
        pressed = false;
        if(!boolFocus)
            setVisible(false);
    }

    public void mouseEntered(MouseEvent event)
    {
        boolFocus = true;
        if(threadPopup != null)
            threadPopup.stopNormaly();
    }

    public void mouseExited(MouseEvent event)
    {
        if(boolFocus && !pressed)
            setVisible(false);
        boolFocus = false;
    }

    public void mouseDragged(MouseEvent event)
    {
        if(threadPopup != null)
            threadPopup.resetCounter(3);
        Point pointMouse = event.getPoint();
        setLevelToMouse(pointMouse);
    }

    public void mouseMoved(MouseEvent mouseevent)
    {
    }

    public void focusLost(FocusEvent focusevent)
    {
    }

    public void focusGained(FocusEvent focusevent)
    {
    }

    private void setLevelToMouse(Point pointMouse)
    {
        if(gainControl == null)
            return;
        Dimension dimSize = getSize();
        Insets insets = getInsets();
        int nPos = pointMouse.x - 2 - insets.left;
        int nWidth = dimSize.width - insets.left - insets.right - 5;
        if(nPos > nWidth)
            nPos = nWidth;
        if(nPos < 0)
            nPos = 0;
        float levelGain = (float)nPos / (float)nWidth;
        gainControl.setMute(false);
        gainControl.setLevel(levelGain);
    }

    private GainControl gainControl;
    private Image imageGrabber;
    private Dimension dimGrabber;
    private Button buttonFocus;
    private boolean boolFocus;
    private boolean pressed;
    private PopupThread threadPopup;
    private Image imageBackground;
    private static final int WIDTH = 80;
    private static final int HEIGHT = 20;
}
