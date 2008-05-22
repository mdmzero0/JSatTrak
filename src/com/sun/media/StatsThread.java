// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicPlayer.java

package com.sun.media;

import com.sun.media.util.LoopThread;

// Referenced classes of package com.sun.media:
//            BasicController, BasicPlayer

class StatsThread extends LoopThread
{

    public StatsThread(BasicPlayer p)
    {
        pausecount = -1;
        player = p;
    }

    protected boolean process()
    {
        try
        {
            Thread.currentThread();
            Thread.sleep(1000L);
        }
        catch(Exception e) { }
        if(!waitHereIfPaused())
            return false;
        if(player.getState() == 600)
        {
            pausecount = -1;
            player.updateStats();
        } else
        if(pausecount < 5)
        {
            pausecount++;
            player.updateStats();
        }
        return true;
    }

    BasicPlayer player;
    int pausecount;
}
