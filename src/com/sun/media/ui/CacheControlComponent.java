// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CacheControlComponent.java

package com.sun.media.ui;

import com.sun.media.util.JMFI18N;
import java.awt.*;
import javax.media.*;

// Referenced classes of package com.sun.media.ui:
//            BufferedPanel, ProgressBar, DefaultControlPanel, BasicComp, 
//            ButtonComp

public class CacheControlComponent extends BufferedPanel
{
    class CancelButton extends ButtonComp
    {

        public void action()
        {
            super.action();
            if(player == null);
            if(xtdctrl != null)
                if(super.state)
                    xtdctrl.pauseDownload();
                else
                    xtdctrl.resumeDownload();
        }

        public CancelButton()
        {
            super("Suspend download", "pause.gif", "pause-active.gif", "pause-pressed.gif", "pause-disabled.gif", "play.gif", "play-active.gif", "play-pressed.gif", "play-disabled.gif");
        }
    }


    public CacheControlComponent(CachingControl ctrl, Player player)
    {
        this.ctrl = null;
        xtdctrl = null;
        this.player = null;
        cancelButton = null;
        progressBar = null;
        this.ctrl = ctrl;
        this.player = player;
        if(ctrl instanceof ExtendedCachingControl)
            xtdctrl = (ExtendedCachingControl)ctrl;
        setBackground(DefaultControlPanel.colorBackground);
        setBackgroundTile(BasicComp.fetchImage("texture3.gif"));
        GridBagLayout gbl;
        setLayout(gbl = new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 0;
        Label label = new Label(JMFI18N.getResource("mediaplayer.download"), 1);
        add(label);
        gbl.setConstraints(label, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        cancelButton = new CancelButton();
        add(cancelButton);
        gbl.setConstraints(cancelButton, gbc);
        gbc.gridx++;
        progressBar = new ProgressBar(ctrl);
        add(progressBar);
        gbl.setConstraints(progressBar, gbc);
    }

    public void addNotify()
    {
        super.addNotify();
        setSize(getPreferredSize());
    }

    public Component getProgressBar()
    {
        return progressBar;
    }

    protected CachingControl ctrl;
    private ExtendedCachingControl xtdctrl;
    protected Player player;
    protected ButtonComp cancelButton;
    protected ProgressBar progressBar;

}
