// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UnsupportedFormatException.java

package javax.media.format;

import javax.media.Format;
import javax.media.MediaException;

public class UnsupportedFormatException extends MediaException
{

    public UnsupportedFormatException(Format unsupportedFormat)
    {
        failedFormat = unsupportedFormat;
    }

    public UnsupportedFormatException(String message, Format unsupportedFormat)
    {
        super(message);
        failedFormat = unsupportedFormat;
    }

    public Format getFailedFormat()
    {
        return failedFormat;
    }

    Format failedFormat;
}
