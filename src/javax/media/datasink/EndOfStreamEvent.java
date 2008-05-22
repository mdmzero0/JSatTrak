// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   EndOfStreamEvent.java

package javax.media.datasink;

import javax.media.DataSink;

// Referenced classes of package javax.media.datasink:
//            DataSinkEvent

public class EndOfStreamEvent extends DataSinkEvent
{

    public EndOfStreamEvent(DataSink from)
    {
        super(from);
    }

    public EndOfStreamEvent(DataSink from, String reason)
    {
        super(from, reason);
    }
}
