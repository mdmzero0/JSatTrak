// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AudioCodecChain.java

package com.sun.media.util;

import com.sun.media.ui.GainControlComponent;
import java.awt.Component;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;

// Referenced classes of package com.sun.media.util:
//            CodecChain

public class AudioCodecChain extends CodecChain
{

    public AudioCodecChain(AudioFormat input)
        throws UnsupportedFormatException
    {
        gainComp = null;
        AudioFormat af = input;
        if(!buildChain(input))
        {
            throw new UnsupportedFormatException(input);
        } else
        {
            super.renderer.close();
            super.firstBuffer = false;
            return;
        }
    }

    public Component getControlComponent()
    {
        if(gainComp != null)
            return gainComp;
        Control c = (Control)super.renderer.getControl("javax.media.GainControl");
        if(c != null)
            gainComp = new GainControlComponent((GainControl)c);
        return gainComp;
    }

    public void reset()
    {
    }

    Component gainComp;
}
