// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StringControl.java

package com.sun.media.controls;


// Referenced classes of package com.sun.media.controls:
//            AtomicControl

public interface StringControl
    extends AtomicControl
{

    public abstract String setValue(String s);

    public abstract String getValue();

    public abstract String setTitle(String s);

    public abstract String getTitle();
}
