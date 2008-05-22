// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Codec.java

package javax.media;


// Referenced classes of package javax.media:
//            PlugIn, Format, Buffer

public interface Codec
    extends PlugIn
{

    public abstract Format[] getSupportedInputFormats();

    public abstract Format[] getSupportedOutputFormats(Format format);

    public abstract Format setInputFormat(Format format);

    public abstract Format setOutputFormat(Format format);

    public abstract int process(Buffer buffer, Buffer buffer1);
}
