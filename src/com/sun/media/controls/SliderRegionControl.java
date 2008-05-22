// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SliderRegionControl.java

package com.sun.media.controls;


// Referenced classes of package com.sun.media.controls:
//            AtomicControl

public interface SliderRegionControl
    extends AtomicControl
{

    public abstract long setMaxValue(long l);

    public abstract long getMaxValue();

    public abstract long setMinValue(long l);

    public abstract long getMinValue();

    public abstract boolean isEnable();

    public abstract void setEnable(boolean flag);
}
