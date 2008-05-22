// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RandomAccess.java

package com.sun.media.datasink;


public interface RandomAccess
{

    public abstract void setEnabled(boolean flag);

    public abstract boolean write(long l, int i);
}
