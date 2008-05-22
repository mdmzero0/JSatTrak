// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Player.java

package javax.media;

import java.awt.Component;

// Referenced classes of package javax.media:
//            MediaHandler, Controller, IncompatibleTimeBaseException, GainControl

public interface Player
    extends MediaHandler, Controller
{

    public abstract Component getVisualComponent();

    public abstract GainControl getGainControl();

    public abstract Component getControlPanelComponent();

    public abstract void start();

    public abstract void addController(Controller controller)
        throws IncompatibleTimeBaseException;

    public abstract void removeController(Controller controller);
}
