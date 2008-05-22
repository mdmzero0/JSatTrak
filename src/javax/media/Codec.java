package javax.media;


// Referenced classes of package javax.media:
//            PlugIn, Format, Buffer

public interface Codec
    extends PlugIn
{

    public abstract Format[] getSupportedInputFormats();

    public abstract Format[] getSupportedOutputFormats(Format format);

    public abstract Format setInputFormat(Format format);

    public abstract Format setOutputFormat(Format format);

    public abstract int process(Buffer buffer, Buffer buffer1);
}
