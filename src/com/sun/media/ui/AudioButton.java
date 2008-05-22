// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DefaultControlPanel.java

package com.sun.media.ui;

import java.awt.*;
import javax.media.GainControl;

// Referenced classes of package com.sun.media.ui:
//            ButtonComp, GainSlider

class AudioButton extends ButtonComp
{

    public AudioButton(GainControl gainControl)
    {
        super("Audio", "audio.gif", "audio-active.gif", "audio-pressed.gif", "audio-disabled.gif", "mute.gif", "mute-active.gif", "mute-pressed.gif", "mute-disabled.gif");
        sliderGain = null;
        this.gainControl = gainControl;
        setMousePopup(true);
    }

    protected void processMousePopup()
    {
        if(isShowing() && gainControl.getLevel() >= 0.0F)
        {
            if(sliderGain == null)
                sliderGain = new GainSlider(gainControl, getFrame());
            Dimension dim = getSize();
            Point point = getLocationOnScreen();
            point.y += dim.height;
            sliderGain.setLocation(point);
            sliderGain.setVisible(!sliderGain.isVisible());
        }
    }

    private Frame getFrame()
    {
        Frame frame = null;
        Component parent;
        for(parent = this; parent != null && !(parent instanceof Frame); parent = parent.getParent());
        if(parent != null && (parent instanceof Frame))
            frame = (Frame)parent;
        return frame;
    }

    public void dispose()
    {
        gainControl = null;
        if(sliderGain != null)
        {
            sliderGain.dispose();
            sliderGain = null;
        }
    }

    private GainControl gainControl;
    private GainSlider sliderGain;
}
