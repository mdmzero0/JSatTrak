// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FormatControl.java

package javax.media.control;

import javax.media.Control;
import javax.media.Format;

public interface FormatControl
    extends Control
{

    public abstract Format getFormat();

    public abstract Format setFormat(Format format);

    public abstract Format[] getSupportedFormats();

    public abstract boolean isEnabled();

    public abstract void setEnabled(boolean flag);
}
