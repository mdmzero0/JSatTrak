package javax.media.protocol;


public interface CachedStream
{

    public abstract void setEnabledBuffering(boolean flag);

    public abstract boolean getEnabledBuffering();

    public abstract boolean willReadBytesBlock(long l, int i);

    public abstract boolean willReadBytesBlock(int i);

    public abstract void abortRead();
}
