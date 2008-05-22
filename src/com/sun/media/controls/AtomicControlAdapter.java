// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AtomicControlAdapter.java

package com.sun.media.controls;

import java.awt.Component;
import java.util.Vector;
import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            ControlChangeListener, ControlChangeEvent, AtomicControl

public class AtomicControlAdapter
    implements AtomicControl
{

    public AtomicControlAdapter(Component c, boolean def, Control parent)
    {
        component = null;
        listeners = null;
        isdefault = false;
        this.parent = null;
        enabled = true;
        component = c;
        isdefault = def;
        this.parent = parent;
    }

    public boolean isDefault()
    {
        return isdefault;
    }

    public void setVisible(boolean flag)
    {
    }

    public boolean getVisible()
    {
        return true;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        if(component != null)
            component.setEnabled(enabled);
        informListeners();
    }

    public boolean getEnabled()
    {
        return enabled;
    }

    public void setParent(Control p)
    {
        parent = p;
    }

    public Control getParent()
    {
        return parent;
    }

    public void addControlChangeListener(ControlChangeListener ccl)
    {
        if(listeners == null)
            listeners = new Vector();
        if(ccl != null)
            listeners.addElement(ccl);
    }

    public void removeControlChangeListener(ControlChangeListener ccl)
    {
        if(listeners != null && ccl != null)
            listeners.removeElement(ccl);
    }

    public void informListeners()
    {
        if(listeners != null)
        {
            for(int i = 0; i < listeners.size(); i++)
            {
                ControlChangeListener ccl = (ControlChangeListener)listeners.elementAt(i);
                ccl.controlChanged(new ControlChangeEvent(this));
            }

        }
    }

    public String getTip()
    {
        return null;
    }

    public void setTip(String s)
    {
    }

    public Component getControlComponent()
    {
        return component;
    }

    public boolean isReadOnly()
    {
        return false;
    }

    protected Component component;
    private Vector listeners;
    protected boolean isdefault;
    protected Control parent;
    protected boolean enabled;
}
