// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicRendererModule.java

package com.sun.media;

import com.sun.media.util.LoopThread;
import com.sun.media.util.MediaThread;

// Referenced classes of package com.sun.media:
//            BasicRendererModule

class RenderThread extends LoopThread
{

    public RenderThread(BasicRendererModule module)
    {
        this.module = module;
        setName(getName() + ": " + module.renderer);
        useVideoPriority();
    }

    protected boolean process()
    {
        return module.doProcess();
    }

    BasicRendererModule module;
}
