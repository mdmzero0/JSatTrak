// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ButtonComp.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.*;

// Referenced classes of package com.sun.media.ui:
//            BasicComp

public class ButtonComp extends BasicComp
    implements MouseListener
{
    class ContPressThread extends Thread
    {

        public void setDelayedPress(long lMills)
        {
            boolDelayedPress = true;
            this.lMills = lMills;
        }

        public void stopNormaly()
        {
            boolContinueRun = false;
        }

        public void run()
        {
            if(boolDelayedPress)
                boolIgnoreFirst = false;
            else
                boolIgnoreFirst = true;
            while(boolContinueRun) 
            {
                try
                {
                    Thread.sleep(lMills);
                }
                catch(Exception exception) { }
                if(button != null && !boolIgnoreFirst)
                    button.processContPress();
                boolIgnoreFirst = false;
                if(boolDelayedPress)
                    boolContinueRun = false;
            }
            boolDelayedPress = false;
            lMills = 250L;
        }

        protected ButtonComp button;
        protected boolean boolContinueRun;
        protected boolean boolIgnoreFirst;
        protected boolean boolDelayedPress;
        protected long lMills;

        public ContPressThread(ButtonComp button)
        {
            this.button = null;
            boolContinueRun = true;
            boolIgnoreFirst = true;
            boolDelayedPress = false;
            lMills = 500L;
            this.button = button;
        }
    }


    public ButtonComp(String label, String imgNormal0, String imgActive0, String imgDown0, String imgDisabled0, String imgNormal1, String imgActive1, 
            String imgDown1, String imgDisabled1)
    {
        super(label);
        state = false;
        mouseIn = false;
        mouseDown = false;
        mouseUp = false;
        mouseClick = false;
        visualState = 1;
        menuPopup = null;
        threadContPress = null;
        boolContPress = false;
        boolPopup = false;
        boolDoAction = false;
        imageNormal = new Image[2];
        imageActive = new Image[2];
        imageDown = new Image[2];
        imageDisabled = new Image[2];
        imageNormal[0] = BasicComp.fetchImage(imgNormal0);
        imageNormal[1] = BasicComp.fetchImage(imgNormal1);
        imageActive[0] = BasicComp.fetchImage(imgActive0);
        imageActive[1] = BasicComp.fetchImage(imgActive1);
        imageDown[0] = BasicComp.fetchImage(imgDown0);
        imageDown[1] = BasicComp.fetchImage(imgDown1);
        imageDisabled[0] = BasicComp.fetchImage(imgDisabled0);
        imageDisabled[1] = BasicComp.fetchImage(imgDisabled1);
        width = imageNormal[0].getWidth(this);
        height = imageNormal[0].getHeight(this);
        visualState = 1;
        setSize(width, height);
        setVisible(true);
        addMouseListener(this);
    }

    public void mouseActivity()
    {
        if(isEnabled())
        {
            if(mouseIn)
            {
                if(mouseDown)
                {
                    visualState = 4;
                    if(mouseUp)
                    {
                        action();
                        visualState = 2;
                    }
                } else
                {
                    visualState = 2;
                }
            } else
            {
                visualState = 1;
            }
        } else
        {
            visualState = 8;
        }
        repaint();
    }

    public void action()
    {
        if(!boolDoAction)
        {
            return;
        } else
        {
            state = !state;
            informListener();
            return;
        }
    }

    public void paint(Graphics g)
    {
        int index = state ? 1 : 0;
        Image image = null;
        switch(visualState)
        {
        case 1: // '\001'
            image = imageNormal[index];
            break;

        case 2: // '\002'
            image = imageActive[index];
            break;

        case 4: // '\004'
            image = imageDown[index];
            break;

        case 8: // '\b'
            image = imageDisabled[index];
            break;
        }
        if(image != null)
            g.drawImage(image, 0, 0, this);
    }

    public void setEnabled(boolean value)
    {
        super.setEnabled(value);
        if(!value)
            visualState = 8;
        else
        if(mouseIn)
        {
            if(mouseDown)
                visualState = 4;
            else
                visualState = 2;
        } else
        {
            visualState = 1;
        }
        repaint();
    }

    public boolean getValue()
    {
        return state;
    }

    public void setValue(boolean newState)
    {
        if(state != newState)
        {
            state = newState;
            repaint();
        }
    }

    public void setPopupMenu(PopupMenu menuPopup)
    {
        if(menuPopup != null)
        {
            setMousePopup(true);
            this.menuPopup = menuPopup;
            add(menuPopup);
        } else
        if(this.menuPopup != null)
        {
            setMousePopup(false);
            remove(this.menuPopup);
            this.menuPopup = null;
        }
    }

    public void setMousePopup(boolean boolPopup)
    {
        this.boolPopup = boolPopup;
    }

    public void setContMousePress(boolean boolSet)
    {
        boolContPress = boolSet;
    }

    public void mouseEntered(MouseEvent e)
    {
        mouseIn = true;
        mouseActivity();
    }

    public void mouseExited(MouseEvent e)
    {
        mouseIn = false;
        mouseActivity();
        if(threadContPress != null)
        {
            threadContPress.stopNormaly();
            threadContPress = null;
        }
    }

    public void mousePressed(MouseEvent e)
    {
        int modifier = e.getModifiers();
        if((modifier & 8) == 0 && (modifier & 4) == 0)
        {
            mouseDown = true;
            mouseUp = false;
            mouseActivity();
            if(boolContPress || boolPopup)
            {
                if(threadContPress != null)
                    threadContPress.stopNormaly();
                threadContPress = new ContPressThread(this);
                if(boolPopup)
                    threadContPress.setDelayedPress(1000L);
                threadContPress.start();
            }
            boolDoAction = true;
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        int modifier = e.getModifiers();
        if((modifier & 8) == 0 && (modifier & 4) == 0)
        {
            mouseUp = true;
            mouseActivity();
            mouseUp = false;
            mouseDown = false;
            if(threadContPress != null)
            {
                threadContPress.stopNormaly();
                threadContPress = null;
            }
        }
    }

    public void mouseClicked(MouseEvent e)
    {
        int modifier = e.getModifiers();
        if((modifier & 8) == 0 && (modifier & 4) == 0)
        {
            mouseClick = true;
            mouseActivity();
            mouseClick = false;
        }
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(width, height);
    }

    protected void processMouseEvent(MouseEvent event)
    {
        super.processMouseEvent(event);
        if(event.isPopupTrigger())
            processMousePopup();
    }

    protected void processMousePopup()
    {
        if(menuPopup != null)
            menuPopup.show(this, 0, height);
    }

    protected void processContPress()
    {
        if(boolContPress)
            informListener();
        else
        if(boolPopup && mouseIn && mouseDown)
        {
            boolDoAction = false;
            processMousePopup();
        }
    }

    Image imageNormal[];
    Image imageActive[];
    Image imageDown[];
    Image imageDisabled[];
    static final int NORMAL = 1;
    static final int ACTIVE = 2;
    static final int DOWN = 4;
    static final int DISABLED = 8;
    int width;
    int height;
    boolean state;
    boolean mouseIn;
    boolean mouseDown;
    boolean mouseUp;
    boolean mouseClick;
    int visualState;
    private PopupMenu menuPopup;
    private ContPressThread threadContPress;
    private boolean boolContPress;
    private boolean boolPopup;
    private boolean boolDoAction;
    private static final int POPUP_DELAY = 1000;
}
