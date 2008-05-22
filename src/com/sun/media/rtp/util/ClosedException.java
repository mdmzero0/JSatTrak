// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ClosedException.java

package com.sun.media.rtp.util;

import java.io.IOException;

public class ClosedException extends IOException
{

    public ClosedException()
    {
    }

    public ClosedException(String m)
    {
        super(m);
    }
}
