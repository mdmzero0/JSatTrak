// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AudioControl.java

package com.sun.media.controls;


// Referenced classes of package com.sun.media.controls:
//            GroupControl, AtomicControl, NumericControl

public interface AudioControl
    extends GroupControl
{

    public abstract AtomicControl getOutputPort();

    public abstract NumericControl getTreble();

    public abstract NumericControl getBass();

    public abstract NumericControl getBalance();
}
