// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CachingControlEvent.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerEvent, CachingControl, Controller

public class CachingControlEvent extends ControllerEvent
{

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

    CachingControl control;
    long progress;
}
