// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ExtendedCachingControl.java

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
