package javax.media.protocol;


// Referenced classes of package javax.media.protocol:
//            DataSource, PushSourceStream

public abstract class PushDataSource extends DataSource
{

    public PushDataSource()
    {
    }

    public abstract PushSourceStream[] getStreams();
}
