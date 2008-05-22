package javax.media.protocol;


// Referenced classes of package javax.media.protocol:
//            RateRange, SourceStream

public interface RateConfiguration
{

    public abstract RateRange getRate();

    public abstract SourceStream[] getStreams();
}
