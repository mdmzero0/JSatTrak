// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GainControlComponent.java

package com.sun.media.ui;

import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import javax.media.*;

// Referenced classes of package com.sun.media.ui:
//            ButtonComp

public class GainControlComponent extends Container
    implements GainChangeListener
{
    class MuteButton extends ButtonComp
    {

        public void action()
        {
            if(gain != null)
                gain.setMute(!gain.getMute());
        }

        public MuteButton()
        {
            super("Mute audio", "audio.gif", "audio-active.gif", "audio-pressed.gif", "audio-disabled.gif", "mute.gif", "mute-active.gif", "mute-pressed.gif", "audio-disabled.gif");
        }
    }

    class VolumeButton extends ButtonComp
    {

        public void action()
        {
            if(gain != null)
            {
                float level = gain.getLevel() + increment;
                if(level < 0.0F)
                    level = 0.0F;
                else
                if(level > 1.0F)
                    level = 1.0F;
                gain.setLevel(level);
                gain.setMute(false);
            }
        }

        public void mousePressed(MouseEvent e)
        {
            super.mousePressed(e);
            if(repeater == null)
            {
                repeater = new Thread() {

                    public void run()
                    {
                        if(gain != null)
                        {
                            float lastLevel = gain.getLevel();
                            for(int unchangedCount = 0; mouseDown && unchangedCount < 5;)
                                try
                                {
                                    Thread.sleep(100L);
                                    try
                                    {
                                        action();
                                    }
                                    catch(Exception ex)
                                    {
                                        mouseDown = false;
                                    }
                                    float newLevel = gain.getLevel();
                                    if(lastLevel == newLevel)
                                    {
                                        unchangedCount++;
                                    } else
                                    {
                                        lastLevel = newLevel;
                                        unchangedCount = 0;
                                    }
                                }
                                catch(InterruptedException ex)
                                {
                                    unchangedCount = 10;
                                }

                        }
                    }

                }
;
                repeater.start();
            }
        }

        public void mouseReleased(MouseEvent e)
        {
            super.mouseReleased(e);
            if(repeater != null)
            {
                Thread killIt = repeater;
                repeater = null;
                boolean permission = true;
                if(GainControlComponent.securityPrivelege)
                {
                    if(GainControlComponent.jmfSecurity != null)
                        try
                        {
                            GainControlComponent.jmfSecurity.requestPermission(m, cl, args, 16);
                            m[0].invoke(cl[0], args[0]);
                        }
                        catch(Exception ex)
                        {
                            permission = false;
                        }
                } else
                {
                    permission = false;
                }
                if(permission)
                    killIt.interrupt();
            }
        }

        public void setEnabled(boolean enabled)
        {
            if(enabled != isEnabled())
            {
                super.setEnabled(enabled);
                mouseActivity();
            }
        }

        protected float increment;
        Thread repeater;


        public VolumeButton(String imgNormal, String imgActive, String imgDown, String imgDisabled, String tip, float increment)
        {
            super(tip, imgNormal, imgActive, imgDown, imgDisabled, imgNormal, imgActive, imgDown, imgDisabled);
            repeater = null;
            this.increment = increment;
        }
    }


    public GainControlComponent(GainControl gain)
    {
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        this.gain = null;
        muteButton = null;
        upButton = null;
        downButton = null;
        fUseVolumeControl = true;
        GridBagConstraints gbc = new GridBagConstraints();
        this.gain = gain;
        gain.addGainChangeListener(this);
        GridBagLayout gbl;
        setLayout(gbl = new GridBagLayout());
        if(canChangeVolume())
        {
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridheight = 2;
            muteButton = new MuteButton();
            add(muteButton);
            gbl.setConstraints(muteButton, gbc);
            gbc.gridx = 1;
            gbc.gridheight = 1;
            upButton = new VolumeButton("volumeUp.gif", "volumeUp-active.gif", "volumeUp-pressed.gif", "volumeUp-disabled.gif", "Increase volume", 0.05F);
            add(upButton);
            gbl.setConstraints(upButton, gbc);
            gbc.gridy = 1;
            downButton = new VolumeButton("volumeDown.gif", "volumeDown-active.gif", "volumeDown-pressed.gif", "volumeDown-disabled.gif", "Decrease volume", -0.05F);
            add(downButton);
            gbl.setConstraints(downButton, gbc);
        } else
        {
            fUseVolumeControl = false;
            muteButton = new MuteButton();
            add(muteButton);
        }
    }

    public void gainChange(GainChangeEvent e)
    {
        if(fUseVolumeControl)
        {
            float level = e.getLevel();
            upButton.setEnabled(level < 1.0F);
            downButton.setEnabled(level > 0.0F);
        }
        muteButton.setValue(e.getMute());
    }

    protected boolean canChangeVolume()
    {
        return gain != null && gain.getLevel() >= 0.0F;
    }

    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    protected GainControl gain;
    protected MuteButton muteButton;
    protected VolumeButton upButton;
    protected VolumeButton downButton;
    protected boolean fUseVolumeControl;
    protected static final float VolumeIncrement = 0.05F;
    protected static final int RepeatDelay = 100;

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
    }





}
