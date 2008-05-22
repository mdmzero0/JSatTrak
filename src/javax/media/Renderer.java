// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Renderer.java

package javax.media;


// Referenced classes of package javax.media:
//            PlugIn, Format, Buffer

public interface Renderer
    extends PlugIn
{

    public abstract Format[] getSupportedInputFormats();

    public abstract Format setInputFormat(Format format);

    public abstract void start();

    public abstract void stop();

    public abstract int process(Buffer buffer);
}
