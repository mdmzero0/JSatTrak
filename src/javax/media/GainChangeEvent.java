// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GainChangeEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            MediaEvent, GainControl

public class GainChangeEvent extends MediaEvent
{

    public GainChangeEvent(GainControl from, boolean mute, float dB, float level)
    {
        super(from);
        eventSrc = from;
        newMute = mute;
        newDB = dB;
        newLevel = level;
    }

    public Object getSource()
    {
        return eventSrc;
    }

    public GainControl getSourceGainControl()
    {
        return eventSrc;
    }

    public float getDB()
    {
        return newDB;
    }

    public float getLevel()
    {
        return newLevel;
    }

    public boolean getMute()
    {
        return newMute;
    }

    GainControl eventSrc;
    boolean newMute;
    float newDB;
    float newLevel;
}
