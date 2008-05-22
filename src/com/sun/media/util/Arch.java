// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Arch.java

package com.sun.media.util;


public class Arch
{

    public Arch()
    {
    }

    public static boolean isBigEndian()
    {
        return false;
    }

    public static boolean isLittleEndian()
    {
        return true;
    }

    public static int getAlignment()
    {
        return 1;
    }

    public static int getArch()
    {
        return 36;
    }

    public static final int SPARC = 1;
    public static final int UNIX = 2;
    public static final int WIN32 = 4;
    public static final int SOLARIS = 8;
    public static final int LINUX = 16;
    public static final int X86 = 32;
}
