// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CachingControl.java

package javax.media;

import java.awt.Component;

// Referenced classes of package javax.media:
//            Control

public interface CachingControl
    extends Control
{

    public abstract boolean isDownloading();

    public abstract long getContentLength();

    public abstract long getContentProgress();

    public abstract Component getProgressBarComponent();

    public abstract Component getControlComponent();

    public static final long LENGTH_UNKNOWN = 0x7fffffffffffffffL;
}
