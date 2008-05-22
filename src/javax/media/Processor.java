package javax.media;

import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

// Referenced classes of package javax.media:
//            Player, NotConfiguredError, NotRealizedError

public interface Processor
    extends Player
{

    public static final int Configuring = 140;
    public static final int Configured = 180;

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
}
