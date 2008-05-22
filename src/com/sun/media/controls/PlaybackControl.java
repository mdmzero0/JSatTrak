// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PlaybackControl.java

package com.sun.media.controls;


// Referenced classes of package com.sun.media.controls:
//            GroupControl, BooleanControl, ActionControl, NumericControl

public interface PlaybackControl
    extends GroupControl
{

    public abstract BooleanControl getPlay();

    public abstract BooleanControl getStop();

    public abstract ActionControl getStepForward();

    public abstract ActionControl getStepBackward();

    public abstract NumericControl getPlayRate();

    public abstract NumericControl getSeek();
}
