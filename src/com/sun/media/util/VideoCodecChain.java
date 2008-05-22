// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VideoCodecChain.java

package com.sun.media.util;

import java.awt.Component;
import javax.media.Format;
import javax.media.format.*;
import javax.media.renderer.VideoRenderer;

// Referenced classes of package com.sun.media.util:
//            CodecChain

public class VideoCodecChain extends CodecChain
{

    public VideoCodecChain(VideoFormat vf)
        throws UnsupportedFormatException
    {
        java.awt.Dimension size = vf.getSize();
        VideoFormat inputFormat = vf;
        if(size == null || vf == null)
            throw new UnsupportedFormatException(vf);
        if(!buildChain(vf))
            throw new UnsupportedFormatException(vf);
        else
            return;
    }

    boolean isRawFormat(Format format)
    {
        return (format instanceof RGBFormat) || (format instanceof YUVFormat) || format.getEncoding() != null && (format.getEncoding().equalsIgnoreCase("jpeg") || format.getEncoding().equalsIgnoreCase("mpeg"));
    }

    public Component getControlComponent()
    {
        if(super.renderer instanceof VideoRenderer)
            return ((VideoRenderer)super.renderer).getComponent();
        else
            return null;
    }
}
