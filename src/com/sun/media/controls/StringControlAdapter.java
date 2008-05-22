// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StringControlAdapter.java

package com.sun.media.controls;

import java.awt.Component;
import javax.media.Control;

// Referenced classes of package com.sun.media.controls:
//            AtomicControlAdapter, StringControl

public class StringControlAdapter extends AtomicControlAdapter
    implements StringControl
{

    public StringControlAdapter()
    {
        super(null, true, null);
    }

    public StringControlAdapter(Component c, boolean def, Control parent)
    {
        super(c, def, parent);
    }

    public String setValue(String value)
    {
        this.value = value;
        informListeners();
        return value;
    }

    public String getValue()
    {
        return value;
    }

    public String getTitle()
    {
        return title;
    }

    public String setTitle(String title)
    {
        this.title = title;
        informListeners();
        return title;
    }

    String value;
    String title;
}
