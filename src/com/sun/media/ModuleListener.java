// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ModuleListener.java

package com.sun.media;

import javax.media.Buffer;
import javax.media.Format;

// Referenced classes of package com.sun.media:
//            Module, InputConnector

public interface ModuleListener
{

    public abstract void bufferPrefetched(Module module);

    public abstract void stopAtTime(Module module);

    public abstract void mediaEnded(Module module);

    public abstract void resetted(Module module);

    public abstract void dataBlocked(Module module, boolean flag);

    public abstract void framesBehind(Module module, float f, InputConnector inputconnector);

    public abstract void markedDataArrived(Module module, Buffer buffer);

    public abstract void formatChanged(Module module, Format format, Format format1);

    public abstract void formatChangedFailure(Module module, Format format, Format format1);

    public abstract void pluginTerminated(Module module);

    public abstract void internalErrorOccurred(Module module);
}
