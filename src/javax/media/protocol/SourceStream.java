// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SourceStream.java

package javax.media.protocol;


// Referenced classes of package javax.media.protocol:
//            Controls, ContentDescriptor

public interface SourceStream
    extends Controls
{

    public abstract ContentDescriptor getContentDescriptor();

    public abstract long getContentLength();

    public abstract boolean endOfStream();

    public static final long LENGTH_UNKNOWN = -1L;
}
