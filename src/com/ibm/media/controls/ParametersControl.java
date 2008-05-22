// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ParametersControl.java

package com.ibm.media.controls;

import java.awt.Component;
import java.util.Hashtable;
import javax.media.Control;

public class ParametersControl
    implements Control
{

    public ParametersControl()
    {
        parameters = new Hashtable();
    }

    public String get(String param)
    {
        return (String)parameters.get(param);
    }

    public void set(String param, String value)
    {
        parameters.remove(param);
        parameters.put(param, value);
    }

    public Component getControlComponent()
    {
        return null;
    }

    Hashtable parameters;
}
