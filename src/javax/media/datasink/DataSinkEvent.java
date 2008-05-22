// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSinkEvent.java

package javax.media.datasink;

import java.util.EventObject;
import javax.media.DataSink;
import javax.media.MediaEvent;

public class DataSinkEvent extends MediaEvent
{

    public DataSinkEvent(DataSink from)
    {
        super(from);
        message = new String("");
    }

    public DataSinkEvent(DataSink from, String reason)
    {
        super(from);
        message = new String(reason);
    }

    public DataSink getSourceDataSink()
    {
        return (DataSink)getSource();
    }

    public String toString()
    {
        return getClass().getName() + "[source=" + getSource() + "] message: " + message;
    }

    private String message;
}
