// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FormatAdapter.java

package com.sun.media.controls;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.media.Format;
import javax.media.control.FormatControl;

public class FormatAdapter
    implements FormatControl, ActionListener
{

    public FormatAdapter(Format format, Format supported[], boolean enabled, boolean formattable, boolean enableable)
    {
        currentFormat = format;
        supportedFormats = supported;
        this.enabled = enabled;
        this.formattable = formattable;
        this.enableable = enableable;
    }

    public Format getFormat()
    {
        return currentFormat;
    }

    public Format setFormat(Format newFormat)
    {
        if(formattable)
            currentFormat = newFormat;
        return currentFormat;
    }

    public Format[] getSupportedFormats()
    {
        return supportedFormats;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean newEnable)
    {
        if(enableable)
            enabled = newEnable;
    }

    protected String getName()
    {
        return "Format";
    }

    public Component getControlComponent()
    {
        return null;
    }

    public void actionPerformed(ActionEvent actionevent)
    {
    }

    protected Format currentFormat;
    protected Format supportedFormats[];
    protected boolean enabled;
    protected boolean formattable;
    protected boolean enableable;
}
