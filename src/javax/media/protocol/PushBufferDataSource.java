package javax.media.protocol;


// Referenced classes of package javax.media.protocol:
//            DataSource, PushBufferStream

public abstract class PushBufferDataSource extends DataSource
{

    public PushBufferDataSource()
    {
    }

    public abstract PushBufferStream[] getStreams();
}
