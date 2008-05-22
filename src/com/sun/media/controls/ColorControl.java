// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ColorControl.java

package com.sun.media.controls;


// Referenced classes of package com.sun.media.controls:
//            GroupControl, NumericControl, BooleanControl

public interface ColorControl
    extends GroupControl
{

    public abstract NumericControl getBrightness();

    public abstract NumericControl getContrast();

    public abstract NumericControl getSaturation();

    public abstract NumericControl getHue();

    public abstract BooleanControl getGrayscale();
}
