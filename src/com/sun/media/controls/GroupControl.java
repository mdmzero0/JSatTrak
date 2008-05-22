// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GroupControl.java

package com.sun.media.controls;

import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            AtomicControl

public interface GroupControl
    extends AtomicControl
{

    public abstract Control[] getControls();
}
