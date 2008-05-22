// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BooleanControlAdapter.java

package com.sun.media.controls;

import java.awt.Component;
import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            AtomicControlAdapter, BooleanControl

public class BooleanControlAdapter extends AtomicControlAdapter
    implements BooleanControl
{

    public BooleanControlAdapter()
    {
        super(null, false, null);
    }

    public BooleanControlAdapter(Component c, boolean def, Control parent)
    {
        super(c, def, parent);
    }

    public boolean setValue(boolean val)
    {
        return val;
    }

    public boolean getValue()
    {
        return false;
    }
}
