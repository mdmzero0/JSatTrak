// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Positionable.java

package javax.media.protocol;

import javax.media.Time;

public interface Positionable
{

    public abstract Time setPosition(Time time, int i);

    public abstract boolean isRandomAccess();

    public static final int RoundUp = 1;
    public static final int RoundDown = 2;
    public static final int RoundNearest = 3;
}
