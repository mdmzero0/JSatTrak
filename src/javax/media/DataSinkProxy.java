package javax.media;


// Referenced classes of package javax.media:
//            MediaProxy, MediaLocator

public interface DataSinkProxy
    extends MediaProxy
{

    public abstract String getContentType(MediaLocator medialocator);
}
