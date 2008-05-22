package javax.media;


// Referenced classes of package javax.media:
//            PlugIn, Format, Buffer

public interface Renderer
    extends PlugIn
{

    public abstract Format[] getSupportedInputFormats();

    public abstract Format setInputFormat(Format format);

    public abstract void start();

    public abstract void stop();

    public abstract int process(Buffer buffer);
}
