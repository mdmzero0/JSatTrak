// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RateConfiguration.java

package javax.media.protocol;


// Referenced classes of package javax.media.protocol:
//            RateRange, SourceStream

public interface RateConfiguration
{

    public abstract RateRange getRate();

    public abstract SourceStream[] getStreams();
}
