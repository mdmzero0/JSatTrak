// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Reparentable.java

package com.sun.media;

import javax.media.Owned;

public interface Reparentable
    extends Owned
{

    public abstract void setOwner(Object obj);
}
