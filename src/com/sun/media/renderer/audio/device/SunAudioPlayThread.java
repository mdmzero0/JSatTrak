// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SunAudioOutput.java

package com.sun.media.renderer.audio.device;

import com.sun.media.util.LoopThread;

// Referenced classes of package com.sun.media.renderer.audio.device:
//            SunAudioOutput

class SunAudioPlayThread extends LoopThread
{

    SunAudioPlayThread()
    {
        sunAudioInternalDelay = -1;
        setName(getName() + ": " + this);
    }

    void setStream(SunAudioOutput s)
    {
        sunAudioOutput = s;
    }

    public void resetSampleCountTime()
    {
        initialTime = System.currentTimeMillis();
    }

    public synchronized void start()
    {
        currentTime = System.currentTimeMillis();
        super.start();
    }

    public void setInternalDelay(int delay)
    {
        if(delay >= 0)
            sunAudioInternalDelay = delay;
        sunAudioOutput.setPaddingLength(sunAudioInternalDelay * 2);
    }

    protected boolean process()
    {
        try
        {
            SunAudioPlayThread _tmp = this;
            Thread.sleep(50L);
        }
        catch(InterruptedException e) { }
        if(sunAudioInternalDelay >= 0)
        {
            currentTime = System.currentTimeMillis();
            samplesUpdated = (int)((currentTime - initialTime) * 8L);
            if(samplesUpdated >= 0 && !sunAudioOutput.paused)
            {
                int tmpSamplesPlayed = sunAudioOutput.sunAudioInitialCount + samplesUpdated;
                if(tmpSamplesPlayed > sunAudioOutput.samplesPlayed && tmpSamplesPlayed <= sunAudioOutput.sunAudioFinalCount && tmpSamplesPlayed - sunAudioInternalDelay > sunAudioOutput.samplesPlayed)
                    sunAudioOutput.samplesPlayed = tmpSamplesPlayed - sunAudioInternalDelay;
            }
        }
        return true;
    }

    long initialTime;
    long currentTime;
    int samplesUpdated;
    int sunAudioInternalDelay;
    SunAudioOutput sunAudioOutput;
}
