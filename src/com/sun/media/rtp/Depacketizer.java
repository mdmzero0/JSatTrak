// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Depacketizer.java

package com.sun.media.rtp;

import javax.media.*;

public interface Depacketizer
    extends PlugIn
{

    public abstract Format[] getSupportedInputFormats();

    public abstract Format setInputFormat(Format format);

    public abstract Format parse(Buffer buffer);

    public static final int DEPACKETIZER = 6;
}
