// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JMD.java

package com.sun.media;

import javax.media.Buffer;
import javax.media.Control;

// Referenced classes of package com.sun.media:
//            BasicModule

public interface JMD
    extends Control
{

    public abstract void setVisible(boolean flag);

    public abstract void initGraph(BasicModule basicmodule);

    public abstract void moduleIn(BasicModule basicmodule, int i, Buffer buffer, boolean flag);

    public abstract void moduleOut(BasicModule basicmodule, int i, Buffer buffer, boolean flag);
}
