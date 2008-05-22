// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Signed.java

package com.sun.media.rtp.util;


public final class Signed
{

    public Signed()
    {
    }

    public static long UnsignedInt(int signed)
    {
        return 0x100000000L + (long)signed;
    }
}
