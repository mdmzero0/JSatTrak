// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SendStream.java

package javax.media.rtp;

import java.io.IOException;
import javax.media.rtp.rtcp.SourceDescription;

// Referenced classes of package javax.media.rtp:
//            RTPStream, TransmissionStats

public interface SendStream
    extends RTPStream
{

    public abstract void setSourceDescription(SourceDescription asourcedescription[]);

    public abstract void close();

    public abstract void stop()
        throws IOException;

    public abstract void start()
        throws IOException;

    public abstract int setBitRate(int i);

    public abstract TransmissionStats getSourceTransmissionStats();
}
