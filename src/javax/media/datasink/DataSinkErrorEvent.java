// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSinkErrorEvent.java

package javax.media.datasink;

import javax.media.DataSink;

// Referenced classes of package javax.media.datasink:
//            DataSinkEvent

public class DataSinkErrorEvent extends DataSinkEvent
{

    public DataSinkErrorEvent(DataSink from)
    {
        super(from);
    }

    public DataSinkErrorEvent(DataSink from, String reason)
    {
        super(from, reason);
    }
}
