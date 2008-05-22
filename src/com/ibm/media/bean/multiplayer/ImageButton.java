// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ImageButton.java

package com.ibm.media.bean.multiplayer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.FilteredImageSource;
import java.net.URL;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            ImageLabel, GrayFilter

public class ImageButton extends ImageLabel
{

    public ImageButton()
    {
        txtButton = false;
        mouseIsDown = false;
        darkness = 0xffafafaf;
        grayImage = null;
        enableEvents(144L);
        setBorders();
    }

    public ImageButton(String text, boolean txt, int width, int height)
    {
        txtButton = false;
        mouseIsDown = false;
        darkness = 0xffafafaf;
        grayImage = null;
        super.width = width;
        super.height = height;
        if(txt)
        {
            int length = text.length();
            this.text = new char[length];
            text.getChars(0, length, this.text, 0);
            txtButton = txt;
        }
        enableEvents(144L);
        setBorders();
    }

    public ImageButton(String imageURLString)
    {
        super(imageURLString);
        txtButton = false;
        mouseIsDown = false;
        darkness = 0xffafafaf;
        grayImage = null;
        enableEvents(144L);
        setBorders();
    }

    public ImageButton(URL imageURL)
    {
        super(imageURL);
        txtButton = false;
        mouseIsDown = false;
        darkness = 0xffafafaf;
        grayImage = null;
        enableEvents(144L);
        setBorders();
    }

    public ImageButton(URL imageDirectory, String imageFile)
    {
        super(imageDirectory, imageFile);
        txtButton = false;
        mouseIsDown = false;
        darkness = 0xffafafaf;
        grayImage = null;
        enableEvents(144L);
        setBorders();
    }

    public ImageButton(Image image)
    {
        super(image);
        txtButton = false;
        mouseIsDown = false;
        darkness = 0xffafafaf;
        grayImage = null;
        enableEvents(144L);
        setBorders();
    }

    public void waitForImage(boolean doLayout)
    {
        if(txtButton)
        {
            resize(super.width, super.height);
            super.doneLoading = true;
            return;
        } else
        {
            super.waitForImage(doLayout);
            return;
        }
    }

    public void paint(Graphics g)
    {
        if(!super.doneLoading)
            waitForImage(true);
        else
        if(!txtButton)
        {
            if(super.explicitSize)
                g.drawImage(super.image, super.border, super.border, super.width - 2 * super.border, super.height - 2 * super.border, this);
            else
                g.drawImage(super.image, super.border, super.border, this);
            drawRect(g, 0, 0, super.width - 1, super.height - 1, super.border, super.borderColor);
            if(grayImage == null)
                createGrayImage(g);
        } else
        {
            int tsize = (text.length * 9) / 2;
            g.drawChars(text, 0, text.length, super.width / 2 - tsize, super.height / 2 + 5);
        }
        drawBorder(true);
    }

    public void setActionCommand(String command)
    {
        actionCommand = command;
    }

    public String getActionCommand()
    {
        return actionCommand;
    }

    public synchronized void addActionListener(ActionListener l)
    {
        debug("[addActionListener]: " + l);
        actionListener = AWTEventMulticaster.add(actionListener, l);
        super.newEventsOnly = true;
    }

    public synchronized void removeActionListener(ActionListener l)
    {
        actionListener = AWTEventMulticaster.remove(actionListener, l);
    }

    protected void processEvent(AWTEvent e)
    {
        debug("[processEvent]: " + e);
        if(e instanceof ActionEvent)
        {
            processActionEvent((ActionEvent)e);
            return;
        }
        if(e instanceof MouseEvent)
        {
            processMouseEvent((MouseEvent)e);
            return;
        } else
        {
            super.processEvent(e);
            return;
        }
    }

    protected void processMouseEvent(MouseEvent e)
    {
        if(e.getID() == 502)
        {
            mouseIsDown = false;
            paint(getGraphics());
            processEvent(new ActionEvent(this, 1001, actionCommand));
        } else
        if(e.getID() == 501)
        {
            mouseIsDown = true;
            Graphics g = getGraphics();
            int border = getBorder();
            if(!txtButton)
                if(hasExplicitSize())
                    g.drawImage(getGrayImage(), border, border, getWidth() - 2 * border, getHeight() - 2 * border, this);
                else
                    g.drawImage(getGrayImage(), border, border, this);
            drawBorder(false);
        } else
        if(e.getID() == 505 && mouseIsDown)
            paint(getGraphics());
        super.processMouseEvent(e);
    }

    protected void processActionEvent(ActionEvent e)
    {
        debug("Action Event occurred.");
        if(actionListener != null)
            actionListener.actionPerformed(e);
    }

    protected void setText(String t)
    {
        if(txtButton)
        {
            text = new char[t.length()];
            t.getChars(0, t.length(), text, 0);
            paint(getGraphics());
            validate();
        }
    }

    public int getDarkness()
    {
        return darkness;
    }

    public void setDarkness(int darkness)
    {
        this.darkness = darkness;
    }

    public Image getGrayImage()
    {
        return grayImage;
    }

    public void setGrayImage(Image grayImage)
    {
        this.grayImage = grayImage;
    }

    public void drawBorder(boolean isUp)
    {
        Graphics g = getGraphics();
        if(g == null)
            return;
        g.setColor(getBorderColor());
        int left = 0;
        int top = 0;
        int width = getWidth();
        int height = getHeight();
        int border = getBorder();
        for(int i = 0; i < border; i++)
        {
            g.draw3DRect(left, top, width, height, isUp);
            left++;
            top++;
            width -= 2;
            height -= 2;
        }

    }

    private void setBorders()
    {
        setBorder(4);
        setBorderColor(defaultBorderColor);
    }

    private void createGrayImage(Graphics g)
    {
        java.awt.image.ImageFilter filter = new GrayFilter(darkness);
        java.awt.image.ImageProducer producer = new FilteredImageSource(getImage().getSource(), filter);
        grayImage = createImage(producer);
        int border = getBorder();
        if(hasExplicitSize())
            prepareImage(grayImage, getWidth() - 2 * border, getHeight() - 2 * border, this);
        else
            prepareImage(grayImage, this);
        super.paint(g);
    }

    protected static final int defaultBorderWidth = 4;
    protected static final Color defaultBorderColor = new Color(160, 160, 160);
    private char text[];
    private boolean txtButton;
    private boolean mouseIsDown;
    String actionCommand;
    transient ActionListener actionListener;
    transient MouseListener mouseListener;
    transient MouseMotionListener mouseMotionListener;
    private int darkness;
    private Image grayImage;

}
