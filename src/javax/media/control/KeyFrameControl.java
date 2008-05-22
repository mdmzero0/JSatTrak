// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   KeyFrameControl.java

package javax.media.control;

import javax.media.Control;

public interface KeyFrameControl
    extends Control
{

    public abstract int setKeyFrameInterval(int i);

    public abstract int getKeyFrameInterval();

    public abstract int getPreferredKeyFrameInterval();
}
