// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MpegParser.java

package com.ibm.media.parser.video;

import javax.media.MediaException;

class BadDataException extends MediaException
{

    BadDataException()
    {
    }

    BadDataException(String reason)
    {
        super(reason);
    }
}
