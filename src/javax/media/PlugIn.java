// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PlugIn.java

package javax.media;


// Referenced classes of package javax.media:
//            Controls, ResourceUnavailableException

public interface PlugIn
    extends Controls
{

    public abstract String getName();

    public abstract void open()
        throws ResourceUnavailableException;

    public abstract void close();

    public abstract void reset();

    public static final int BUFFER_PROCESSED_OK = 0;
    public static final int BUFFER_PROCESSED_FAILED = 1;
    public static final int INPUT_BUFFER_NOT_CONSUMED = 2;
    public static final int OUTPUT_BUFFER_NOT_FILLED = 4;
    public static final int PLUGIN_TERMINATED = 8;
}
