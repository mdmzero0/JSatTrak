package javax.media;


// Referenced classes of package javax.media:
//            CachingControl, Time, DownloadProgressListener

public interface ExtendedCachingControl
    extends CachingControl
{

    public abstract void setBufferSize(Time time);

    public abstract Time getBufferSize();

    public abstract void pauseDownload();

    public abstract void resumeDownload();

    public abstract long getStartOffset();

    public abstract long getEndOffset();

    public abstract void addDownloadProgressListener(DownloadProgressListener downloadprogresslistener, int i);

    public abstract void removeDownloadProgressListener(DownloadProgressListener downloadprogresslistener);
}
