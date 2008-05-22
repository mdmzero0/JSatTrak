// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProgressControlAdapter.java

package com.sun.media.controls;

import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            AtomicControlAdapter, ProgressControl, StringControl

public class ProgressControlAdapter extends AtomicControlAdapter
    implements ProgressControl
{

    public ProgressControlAdapter(StringControl frameRate, StringControl bitRate, StringControl videoProps, StringControl audioProps, StringControl videoCodec, StringControl audioCodec)
    {
        super(null, true, null);
        controls = null;
        frc = null;
        brc = null;
        vpc = null;
        apc = null;
        ac = null;
        vc = null;
        frc = frameRate;
        brc = bitRate;
        vpc = videoProps;
        apc = audioProps;
        vc = videoCodec;
        ac = audioCodec;
    }

    public StringControl getFrameRate()
    {
        return frc;
    }

    public StringControl getBitRate()
    {
        return brc;
    }

    public StringControl getAudioProperties()
    {
        return apc;
    }

    public StringControl getVideoProperties()
    {
        return vpc;
    }

    public StringControl getVideoCodec()
    {
        return vc;
    }

    public StringControl getAudioCodec()
    {
        return ac;
    }

    public Control[] getControls()
    {
        if(controls == null)
        {
            controls = new Control[6];
            controls[0] = frc;
            controls[1] = brc;
            controls[2] = vpc;
            controls[3] = apc;
            controls[4] = ac;
            controls[5] = vc;
        }
        return controls;
    }

    Control controls[];
    StringControl frc;
    StringControl brc;
    StringControl vpc;
    StringControl apc;
    StringControl ac;
    StringControl vc;
}
