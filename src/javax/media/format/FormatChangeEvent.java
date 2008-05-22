// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FormatChangeEvent.java

package javax.media.format;

import javax.media.*;

public class FormatChangeEvent extends ControllerEvent
{

    public FormatChangeEvent(Controller source)
    {
        super(source);
    }

    public FormatChangeEvent(Controller source, Format oldFormat, Format newFormat)
    {
        super(source);
        this.oldFormat = oldFormat;
        this.newFormat = newFormat;
    }

    public Format getOldFormat()
    {
        return oldFormat;
    }

    public Format getNewFormat()
    {
        return newFormat;
    }

    protected Format oldFormat;
    protected Format newFormat;
}
