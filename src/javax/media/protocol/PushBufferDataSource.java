// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PushBufferDataSource.java

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
