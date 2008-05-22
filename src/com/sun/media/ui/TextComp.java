// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TextComp.java

package com.sun.media.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Referenced classes of package com.sun.media.ui:
//            BasicComp

public class TextComp extends BasicComp
    implements ActionListener
{

    public TextComp(String label, String initial, int size, boolean mutable)
    {
        super(label);
        value = initial;
        this.size = size;
        this.mutable = mutable;
        setLayout(new BorderLayout());
        Label lab = new Label(label, 0);
        add("West", lab);
        if(!mutable)
        {
            compLabel = new Label(initial, 0);
            add("Center", compLabel);
        } else
        {
            compText = new TextField(initial, size);
            add("Center", compText);
            compText.addActionListener(this);
        }
    }

    public float getFloatValue()
    {
        value = getValue();
        try
        {
            float retVal = Float.valueOf(value).floatValue();
            return retVal;
        }
        catch(NumberFormatException nfe)
        {
            return 0.0F;
        }
    }

    public int getIntValue()
    {
        value = getValue();
        try
        {
            int retVal = Integer.valueOf(value).intValue();
            return retVal;
        }
        catch(NumberFormatException nfe)
        {
            return 0;
        }
    }

    public String getValue()
    {
        if(mutable)
            return compText.getText();
        else
            return compLabel.getText();
    }

    public void setValue(String s)
    {
        value = s;
        if(mutable)
            compText.setText(s);
        else
            compLabel.setText(s);
        repaint();
    }

    public void actionPerformed(ActionEvent ae)
    {
        informListener();
    }

    String value;
    int size;
    boolean mutable;
    Label compLabel;
    TextField compText;
}
