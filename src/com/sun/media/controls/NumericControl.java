// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NumericControl.java

package com.sun.media.controls;


// Referenced classes of package com.sun.media.controls:
//            AtomicControl

public interface NumericControl
    extends AtomicControl
{

    public abstract float getLowerLimit();

    public abstract float getUpperLimit();

    public abstract float getValue();

    public abstract float setValue(float f);

    public abstract float getDefaultValue();

    public abstract float setDefaultValue(float f);

    public abstract float getGranularity();

    public abstract boolean isLogarithmic();

    public abstract float getLogarithmicBase();
}
