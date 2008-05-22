// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JMFSecurity.java

package com.sun.media;

import java.lang.reflect.Method;

public interface JMFSecurity
{

    public abstract String getName();

    public abstract void requestPermission(Method amethod[], Class aclass[], Object aobj[][], int i)
        throws SecurityException;

    public abstract void requestPermission(Method amethod[], Class aclass[], Object aobj[][], int i, String s)
        throws SecurityException;

    public abstract boolean isLinkPermissionEnabled();

    public abstract void permissionFailureNotification(int i);

    public abstract void loadLibrary(String s)
        throws UnsatisfiedLinkError;

    public static final int READ_PROPERTY = 1;
    public static final int READ_FILE = 2;
    public static final int WRITE_FILE = 4;
    public static final int DELETE_FILE = 8;
    public static final int THREAD = 16;
    public static final int THREAD_GROUP = 32;
    public static final int LINK = 64;
    public static final int CONNECT = 128;
    public static final int TOP_LEVEL_WINDOW = 256;
    public static final int MULTICAST = 512;
}
