// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MSHeavyComponent.java

package com.sun.media.renderer.video;

import com.ms.awt.HeavyComponent;

// Referenced classes of package com.sun.media.renderer.video:
//            HeavyComponent

public class MSHeavyComponent extends com.sun.media.renderer.video.HeavyComponent
    implements HeavyComponent
{

    public MSHeavyComponent()
    {
    }

    public boolean needsHeavyPeer()
    {
        return true;
    }
}
