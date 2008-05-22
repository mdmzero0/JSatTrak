// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicSourceModule.java

package com.sun.media;

import javax.media.Track;

// Referenced classes of package com.sun.media:
//            BasicOutputConnector, BasicConnector

class MyOutputConnector extends BasicOutputConnector
{

    public MyOutputConnector(Track track)
    {
        this.track = track;
        super.format = track.getFormat();
    }

    public String toString()
    {
        return super.toString() + ": " + getFormat();
    }

    protected Track track;
}
