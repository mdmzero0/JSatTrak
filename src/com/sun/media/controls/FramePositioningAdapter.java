// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FramePositioningAdapter.java

package com.sun.media.controls;

import com.sun.media.Reparentable;
import java.awt.Component;
import javax.media.*;
import javax.media.control.FramePositioningControl;
import javax.media.format.VideoFormat;

public class FramePositioningAdapter
    implements FramePositioningControl, Reparentable
{

    public static Track getMasterTrack(Track tracks[])
    {
        Track master = null;
        float rate = -1F;
        for(int i = 0; i < tracks.length; i++)
        {
            javax.media.Format f;
            if(tracks[i] != null && (f = tracks[i].getFormat()) != null && (f instanceof VideoFormat))
            {
                master = tracks[i];
                if((rate = ((VideoFormat)f).getFrameRate()) != -1F && rate != 0.0F)
                    return master;
            }
        }

        if(master != null && master.mapTimeToFrame(new Time(0L)) != 0x7fffffff)
            return master;
        else
            return null;
    }

    public FramePositioningAdapter(Player p, Track track)
    {
        master = null;
        frameStep = -1L;
        player = p;
        master = track;
        javax.media.Format f = track.getFormat();
        if(f instanceof VideoFormat)
        {
            float rate = ((VideoFormat)f).getFrameRate();
            if(rate != -1F && rate != 0.0F)
                frameStep = (long)(1E+009F / rate);
        }
    }

    public int seek(int frameNumber)
    {
        Time seekTo = master.mapFrameToTime(frameNumber);
        if(seekTo != null && seekTo != FramePositioningControl.TIME_UNKNOWN)
        {
            player.setMediaTime(seekTo);
            return master.mapTimeToFrame(seekTo);
        } else
        {
            return 0x7fffffff;
        }
    }

    public int skip(int framesToSkip)
    {
        if(frameStep != -1L)
        {
            long t = player.getMediaNanoseconds() + (long)framesToSkip * frameStep;
            player.setMediaTime(new Time(t));
            return framesToSkip;
        }
        int currentFrame = master.mapTimeToFrame(player.getMediaTime());
        if(currentFrame != 0 && currentFrame != 0x7fffffff)
        {
            int newFrame = seek(currentFrame + framesToSkip);
            return newFrame - currentFrame;
        } else
        {
            return 0x7fffffff;
        }
    }

    public Time mapFrameToTime(int frameNumber)
    {
        return master.mapFrameToTime(frameNumber);
    }

    public int mapTimeToFrame(Time mediaTime)
    {
        return master.mapTimeToFrame(mediaTime);
    }

    public Component getControlComponent()
    {
        return null;
    }

    public Object getOwner()
    {
        if(owner == null)
            return this;
        else
            return owner;
    }

    public void setOwner(Object newOwner)
    {
        owner = newOwner;
    }

    Object owner;
    Player player;
    Track master;
    long frameStep;
}
