// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   QualityControl.java

package javax.media.control;

import javax.media.Control;

public interface QualityControl
    extends Control
{

    public abstract float getQuality();

    public abstract float setQuality(float f);

    public abstract float getPreferredQuality();

    public abstract boolean isTemporalSpatialTradeoffSupported();
}
