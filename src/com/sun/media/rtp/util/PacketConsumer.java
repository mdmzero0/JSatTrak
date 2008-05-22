// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PacketConsumer.java

package com.sun.media.rtp.util;

import java.io.IOException;

// Referenced classes of package com.sun.media.rtp.util:
//            Packet

public interface PacketConsumer
{

    public abstract void sendTo(Packet packet)
        throws IOException;

    public abstract void closeConsumer();

    public abstract String consumerString();
}
