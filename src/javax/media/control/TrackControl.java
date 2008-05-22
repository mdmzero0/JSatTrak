package javax.media.control;

import javax.media.*;

// Referenced classes of package javax.media.control:
//            FormatControl

public interface TrackControl
    extends FormatControl, Controls
{

    public abstract void setCodecChain(Codec acodec[])
        throws UnsupportedPlugInException, NotConfiguredError;

    public abstract void setRenderer(Renderer renderer)
        throws UnsupportedPlugInException, NotConfiguredError;
}
