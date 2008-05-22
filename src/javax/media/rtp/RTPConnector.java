// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPConnector.java

package javax.media.rtp;

import java.io.IOException;
import javax.media.protocol.PushSourceStream;

// Referenced classes of package javax.media.rtp:
//            OutputDataStream

public interface RTPConnector
{

    public abstract PushSourceStream getDataInputStream()
        throws IOException;

    public abstract OutputDataStream getDataOutputStream()
        throws IOException;

    public abstract PushSourceStream getControlInputStream()
        throws IOException;

    public abstract OutputDataStream getControlOutputStream()
        throws IOException;

    public abstract void close();

    public abstract void setReceiveBufferSize(int i)
        throws IOException;

    public abstract int getReceiveBufferSize();

    public abstract void setSendBufferSize(int i)
        throws IOException;

    public abstract int getSendBufferSize();

    public abstract double getRTCPBandwidthFraction();

    public abstract double getRTCPSenderBandwidthFraction();
}
