// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicComp.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import javax.media.Control;

// Referenced classes of package com.sun.media.ui:
//            ImageLib

public class BasicComp extends Container
{

    protected BasicComp(String label)
    {
        this.label = null;
        al = null;
        control = null;
        this.label = label;
    }

    public void setActionListener(ActionListener al)
    {
        this.al = al;
    }

    protected void informListener()
    {
        if(al != null)
            al.actionPerformed(new ActionEvent(this, 1001, label));
    }

    public static synchronized Image fetchImage(String name)
    {
        Image image = null;
        byte bits[] = ImageLib.getImage(name);
        if(bits == null)
            return null;
        image = Toolkit.getDefaultToolkit().createImage(bits);
        try
        {
            MediaTracker imageTracker = new MediaTracker(panel);
            imageTracker.addImage(image, 0);
            imageTracker.waitForID(0);
        }
        catch(InterruptedException e)
        {
            System.err.println("ImageLoader: Interrupted at waitForID");
        }
        return image;
    }

    public String getLabel()
    {
        return label;
    }

    protected String label;
    private ActionListener al;
    static Panel panel = new Panel();
    Control control;
    int width;
    int height;

}
