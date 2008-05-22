// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VideoSizingControl.java

package com.sun.media.controls;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            NumericControl, BooleanControl

public interface VideoSizingControl
    extends Control
{

    public abstract boolean supportsAnyScale();

    public abstract Dimension setVideoSize(Dimension dimension);

    public abstract Dimension getVideoSize();

    public abstract Dimension getInputVideoSize();

    public abstract boolean supportsZoom();

    public abstract float[] getValidZoomFactors();

    public abstract NumericControl getZoomControl();

    public abstract boolean supportsClipping();

    public abstract Rectangle setClipRegion(Rectangle rectangle);

    public abstract Rectangle getClipRegion();

    public abstract BooleanControl getVideoMute();
}
