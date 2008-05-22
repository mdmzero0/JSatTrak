// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPControl.java

package javax.media.rtp;

import javax.media.Control;
import javax.media.Format;

// Referenced classes of package javax.media.rtp:
//            ReceptionStats, GlobalReceptionStats

public interface RTPControl
    extends Control
{

    public abstract void addFormat(Format format, int i);

    public abstract ReceptionStats getReceptionStats();

    public abstract GlobalReceptionStats getGlobalStats();

    public abstract Format getFormat();

    public abstract Format[] getFormatList();

    public abstract Format getFormat(int i);
}
