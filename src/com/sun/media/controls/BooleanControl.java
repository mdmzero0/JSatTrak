// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BooleanControl.java

package com.sun.media.controls;


// Referenced classes of package com.sun.media.controls:
//            AtomicControl

public interface BooleanControl
    extends AtomicControl
{

    public abstract boolean getValue();

    public abstract boolean setValue(boolean flag);
}
