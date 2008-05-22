package javax.media.protocol;


// Referenced classes of package javax.media.protocol:
//            DataSource, PullSourceStream

public abstract class PullDataSource extends DataSource
{

    public PullDataSource()
    {
    }

    public abstract PullSourceStream[] getStreams();
}
