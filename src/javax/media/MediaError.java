package javax.media;


public class MediaError extends Error
{

    public MediaError()
    {
    }

    public MediaError(String reason)
    {
        super(reason);
    }
}
