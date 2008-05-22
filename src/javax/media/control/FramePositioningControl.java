// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FramePositioningControl.java

package javax.media.control;

import javax.media.*;

public interface FramePositioningControl
    extends Control
{

    public abstract int seek(int i);

    public abstract int skip(int i);

    public abstract Time mapFrameToTime(int i);

    public abstract int mapTimeToFrame(Time time);

    public static final Time TIME_UNKNOWN = Track.TIME_UNKNOWN;
    public static final int FRAME_UNKNOWN = 0x7fffffff;

}
