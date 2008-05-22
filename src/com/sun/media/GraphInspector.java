// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GraphInspector.java

package com.sun.media;

import javax.media.*;

public interface GraphInspector
{

    public abstract boolean detailMode();

    public abstract boolean verify(Codec codec, Format format, Format format1);

    public abstract boolean verify(Renderer renderer, Format format);

    public abstract boolean verify(Multiplexer multiplexer, Format aformat[]);

    public abstract void verifyInputFailed(PlugIn plugin, Format format);

    public abstract void verifyOutputFailed(PlugIn plugin, Format format);
}
