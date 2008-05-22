package javax.media;


// Referenced classes of package javax.media:
//            Controls, ResourceUnavailableException

public interface PlugIn
    extends Controls
{

    public static final int BUFFER_PROCESSED_OK = 0;
    public static final int BUFFER_PROCESSED_FAILED = 1;
    public static final int INPUT_BUFFER_NOT_CONSUMED = 2;
    public static final int OUTPUT_BUFFER_NOT_FILLED = 4;
    public static final int PLUGIN_TERMINATED = 8;

    public abstract String getName();

    public abstract void open()
        throws ResourceUnavailableException;

    public abstract void close();

    public abstract void reset();
}
