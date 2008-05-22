// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TrackControl.java

package javax.media.control;

import javax.media.*;

// Referenced classes of package javax.media.control:
//            FormatControl

public interface TrackControl
    extends FormatControl, Controls
{

    public abstract void setCodecChain(Codec acodec[])
        throws UnsupportedPlugInException, NotConfiguredError;

    public abstract void setRenderer(Renderer renderer)
        throws UnsupportedPlugInException, NotConfiguredError;
}
