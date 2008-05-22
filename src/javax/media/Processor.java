// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Processor.java

package javax.media;

import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

// Referenced classes of package javax.media:
//            Player, NotConfiguredError, NotRealizedError

public interface Processor
    extends Player
{

    public abstract void configure();

    public abstract TrackControl[] getTrackControls()
        throws NotConfiguredError;

    public abstract ContentDescriptor[] getSupportedContentDescriptors()
        throws NotConfiguredError;

    public abstract ContentDescriptor setContentDescriptor(ContentDescriptor contentdescriptor)
        throws NotConfiguredError;

    public abstract ContentDescriptor getContentDescriptor()
        throws NotConfiguredError;

    public abstract DataSource getDataOutput()
        throws NotRealizedError;

    public static final int Configuring = 140;
    public static final int Configured = 180;
}
