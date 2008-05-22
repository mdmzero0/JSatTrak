// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicPlayer.java

package com.sun.media;

import com.sun.media.util.MediaThread;

// Referenced classes of package com.sun.media:
//            BasicPlayer

class PlayThread extends MediaThread
{

    public PlayThread(BasicPlayer player)
    {
        this.player = player;
        setName(getName() + " (PlayThread)");
        useControlPriority();
    }

    public void run()
    {
        player.play();
    }

    BasicPlayer player;
}
