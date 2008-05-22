// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Controller.java

package javax.media;


// Referenced classes of package javax.media:
//            Time, Clock, Duration, Control, 
//            ControllerListener

public interface Controller
    extends Clock, Duration
{

    public abstract int getState();

    public abstract int getTargetState();

    public abstract void realize();

    public abstract void prefetch();

    public abstract void deallocate();

    public abstract void close();

    public abstract Time getStartLatency();

    public abstract Control[] getControls();

    public abstract Control getControl(String s);

    public abstract void addControllerListener(ControllerListener controllerlistener);

    public abstract void removeControllerListener(ControllerListener controllerlistener);

    public static final Time LATENCY_UNKNOWN = new Time(0x7fffffffffffffffL);
    public static final int Unrealized = 100;
    public static final int Realizing = 200;
    public static final int Realized = 300;
    public static final int Prefetching = 400;
    public static final int Prefetched = 500;
    public static final int Started = 600;

}
