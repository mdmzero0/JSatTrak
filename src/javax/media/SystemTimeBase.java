// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SystemTimeBase.java

package javax.media;


// Referenced classes of package javax.media:
//            Time, TimeBase

public final class SystemTimeBase
    implements TimeBase
{

    public SystemTimeBase()
    {
    }

    public Time getTime()
    {
        return new Time(getNanoseconds());
    }

    public long getNanoseconds()
    {
        return System.currentTimeMillis() * 0xf4240L - offset;
    }

    static long offset = System.currentTimeMillis() * 0xf4240L;

}
