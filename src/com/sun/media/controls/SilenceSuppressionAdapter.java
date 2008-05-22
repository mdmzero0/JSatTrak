// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SilenceSuppressionAdapter.java

package com.sun.media.controls;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.media.Codec;
import javax.media.control.SilenceSuppressionControl;

public class SilenceSuppressionAdapter
    implements SilenceSuppressionControl
{
    class SilenceSuppresionAdapterListener
        implements ItemListener
    {

        public void itemStateChanged(ItemEvent e)
        {
            try
            {
                boolean newSilenceSuppression = cb.getState();
                setSilenceSuppression(silenceSuppression);
            }
            catch(Exception exception) { }
            cb.setState(silenceSuppression);
        }

        Checkbox cb;

        public SilenceSuppresionAdapterListener(Checkbox source)
        {
            cb = source;
        }
    }


    public SilenceSuppressionAdapter(Codec newOwner, boolean newSilenceSuppression, boolean newIsSetable)
    {
        owner = null;
        silenceSuppression = false;
        component = null;
        CONTROL_STRING = "Silence Suppression";
        silenceSuppression = newSilenceSuppression;
        owner = newOwner;
        isSetable = newIsSetable;
    }

    public boolean getSilenceSuppression()
    {
        return silenceSuppression;
    }

    public boolean setSilenceSuppression(boolean newSilenceSuppression)
    {
        return silenceSuppression;
    }

    public boolean isSilenceSuppressionSupported()
    {
        return silenceSuppression;
    }

    public Component getControlComponent()
    {
        if(component == null)
        {
            Panel componentPanel = new Panel();
            componentPanel.setLayout(new BorderLayout());
            componentPanel.add("Center", new Label(CONTROL_STRING, 1));
            Checkbox cb = new Checkbox(null, null, silenceSuppression);
            cb.setEnabled(isSetable);
            cb.addItemListener(new SilenceSuppresionAdapterListener(cb));
            componentPanel.add("East", cb);
            componentPanel.invalidate();
            component = componentPanel;
        }
        return component;
    }

    protected Codec owner;
    protected boolean silenceSuppression;
    protected boolean isSetable;
    Component component;
    String CONTROL_STRING;
}
