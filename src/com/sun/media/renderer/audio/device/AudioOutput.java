// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AudioOutput.java

package com.sun.media.renderer.audio.device;

import javax.media.format.AudioFormat;

public interface AudioOutput
{

    public abstract boolean initialize(AudioFormat audioformat, int i);

    public abstract void dispose();

    public abstract void pause();

    public abstract void resume();

    public abstract void drain();

    public abstract void flush();

    public abstract long getMediaNanoseconds();

    public abstract void setGain(double d);

    public abstract double getGain();

    public abstract void setMute(boolean flag);

    public abstract boolean getMute();

    public abstract float setRate(float f);

    public abstract float getRate();

    public abstract int bufferAvailable();

    public abstract int write(byte abyte0[], int i, int j);
}
