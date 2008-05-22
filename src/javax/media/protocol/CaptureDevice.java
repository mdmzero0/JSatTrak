package javax.media.protocol;

import java.io.IOException;
import javax.media.CaptureDeviceInfo;
import javax.media.control.FormatControl;

public interface CaptureDevice
{

    public abstract CaptureDeviceInfo getCaptureDeviceInfo();

    public abstract FormatControl[] getFormatControls();

    public abstract void connect()
        throws IOException;

    public abstract void disconnect();

    public abstract void start()
        throws IOException;

    public abstract void stop()
        throws IOException;
}
