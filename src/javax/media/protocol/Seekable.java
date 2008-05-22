package javax.media.protocol;


public interface Seekable
{

    public abstract long seek(long l);

    public abstract long tell();

    public abstract boolean isRandomAccess();
}
