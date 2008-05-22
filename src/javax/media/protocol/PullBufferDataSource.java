package javax.media.protocol;


// Referenced classes of package javax.media.protocol:
//            DataSource, PullBufferStream

public abstract class PullBufferDataSource extends DataSource
{

    public PullBufferDataSource()
    {
    }

    public abstract PullBufferStream[] getStreams();
}
