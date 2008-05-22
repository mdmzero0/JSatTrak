package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, CachingControl, Controller

public class CachingControlEvent extends ControllerEvent
{

    CachingControl control;
    long progress;

    public CachingControlEvent(Controller from, CachingControl cacheControl, long progress)
    {
        super(from);
        control = cacheControl;
        this.progress = progress;
    }

    public CachingControl getCachingControl()
    {
        return control;
    }

    public long getContentProgress()
    {
        return progress;
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + super.eventSrc + ",cachingControl=" + control + ",progress=" + progress + "]";
    }
}
