// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProgressControl.java

package com.sun.media.controls;


// Referenced classes of package com.sun.media.controls:
//            GroupControl, StringControl

public interface ProgressControl
    extends GroupControl
{

    public abstract StringControl getFrameRate();

    public abstract StringControl getBitRate();

    public abstract StringControl getVideoProperties();

    public abstract StringControl getVideoCodec();

    public abstract StringControl getAudioCodec();

    public abstract StringControl getAudioProperties();
}
