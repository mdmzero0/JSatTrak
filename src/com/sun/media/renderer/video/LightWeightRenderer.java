// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LightWeightRenderer.java

package com.sun.media.renderer.video;


// Referenced classes of package com.sun.media.renderer.video:
//            AWTRenderer

public class LightWeightRenderer extends AWTRenderer
{

    public LightWeightRenderer()
    {
        super("LightWeight Renderer");
    }

    public boolean isLightWeight()
    {
        return true;
    }

    private static final String MyName = "LightWeight Renderer";
}
