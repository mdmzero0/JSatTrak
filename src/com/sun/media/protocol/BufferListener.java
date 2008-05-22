// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BufferListener.java

package com.sun.media.protocol;

import javax.media.protocol.DataSource;

public interface BufferListener
{

    public abstract void minThresholdReached(DataSource datasource);
}
