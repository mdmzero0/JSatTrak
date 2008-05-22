// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PortControl.java

package javax.media.control;

import javax.media.Control;

public interface PortControl
    extends Control
{

    public abstract int setPorts(int i);

    public abstract int getPorts();

    public abstract int getSupportedPorts();

    public static final int MICROPHONE = 1;
    public static final int LINE_IN = 2;
    public static final int SPEAKER = 4;
    public static final int HEADPHONE = 8;
    public static final int LINE_OUT = 16;
    public static final int COMPACT_DISC = 32;
    public static final int SVIDEO = 64;
    public static final int COMPOSITE_VIDEO = 128;
    public static final int TV_TUNER = 256;
    public static final int COMPOSITE_VIDEO_2 = 512;
}
