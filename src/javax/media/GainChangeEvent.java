package javax.media;


// Referenced classes of package javax.media:
//            MediaEvent, GainControl

public class GainChangeEvent extends MediaEvent
{

    GainControl eventSrc;
    boolean newMute;
    float newDB;
    float newLevel;

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
}
