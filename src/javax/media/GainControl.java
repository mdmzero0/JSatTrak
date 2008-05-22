// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GainControl.java

package javax.media;


// Referenced classes of package javax.media:
//            Control, GainChangeListener

public interface GainControl
    extends Control
{

    public abstract void setMute(boolean flag);

    public abstract boolean getMute();

    public abstract float setDB(float f);

    public abstract float getDB();

    public abstract float setLevel(float f);

    public abstract float getLevel();

    public abstract void addGainChangeListener(GainChangeListener gainchangelistener);

    public abstract void removeGainChangeListener(GainChangeListener gainchangelistener);
}
