// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Duration.java

package javax.media;


// Referenced classes of package javax.media:
//            Time

public interface Duration
{

    public abstract Time getDuration();

    public static final Time DURATION_UNBOUNDED = new Time(0x7fffffffffffffffL);
    public static final Time DURATION_UNKNOWN = new Time(0x7ffffffffffffffeL);

}
