// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GainControlAdapter.java

package com.sun.media.controls;

import java.awt.Component;
import java.util.Vector;
import javax.media.*;

public class GainControlAdapter
    implements GainControl
{

    public GainControlAdapter()
    {
        listeners = null;
        DefLevel = 0.4F;
        dB = 0.0F;
        level = DefLevel;
    }

    public GainControlAdapter(float defLevel)
    {
        listeners = null;
        DefLevel = 0.4F;
        dB = 0.0F;
        level = DefLevel;
        DefLevel = defLevel;
        level = defLevel;
    }

    public GainControlAdapter(boolean mute)
    {
        listeners = null;
        DefLevel = 0.4F;
        dB = 0.0F;
        level = DefLevel;
        muteState = mute;
        setLevel(DefLevel);
    }

    public void setMute(boolean mute)
    {
        if(muteState != mute)
        {
            muteState = mute;
            informListeners();
        }
    }

    public boolean getMute()
    {
        return muteState;
    }

    public float setDB(float gain)
    {
        if(dB != gain)
        {
            dB = gain;
            float mult = (float)Math.pow(10D, (double)dB / 20D);
            level = mult * DefLevel;
            if((double)level < 0.0D)
                setLevel(0.0F);
            else
            if((double)level > 1.0D)
            {
                setLevel(1.0F);
            } else
            {
                setLevel(level);
                informListeners();
            }
        }
        return dB;
    }

    public float getDB()
    {
        return dB;
    }

    public float setLevel(float level)
    {
        if((double)level < 0.0D)
            level = 0.0F;
        if((double)level > 1.0D)
            level = 1.0F;
        if(this.level != level)
        {
            this.level = level;
            float mult = level / DefLevel;
            dB = (float)((Math.log((double)mult != 0.0D ? mult : 0.0001D) / Math.log(10D)) * 20D);
            informListeners();
        }
        return this.level;
    }

    public float getLevel()
    {
        return level;
    }

    public synchronized void addGainChangeListener(GainChangeListener listener)
    {
        if(listener != null)
        {
            if(listeners == null)
                listeners = new Vector();
            listeners.addElement(listener);
        }
    }

    public synchronized void removeGainChangeListener(GainChangeListener listener)
    {
        if(listener != null && listeners != null)
            listeners.removeElement(listener);
    }

    public Component getControlComponent()
    {
        return null;
    }

    protected synchronized void informListeners()
    {
        if(listeners != null)
        {
            GainChangeEvent gce = new GainChangeEvent(this, muteState, dB, level);
            for(int i = 0; i < listeners.size(); i++)
            {
                GainChangeListener gcl = (GainChangeListener)listeners.elementAt(i);
                gcl.gainChange(gce);
            }

        }
    }

    private Vector listeners;
    private boolean muteState;
    private Component component;
    private float DefLevel;
    private float dB;
    private float level;
}
