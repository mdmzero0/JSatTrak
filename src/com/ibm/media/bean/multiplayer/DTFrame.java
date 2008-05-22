// Decompiled by DJ v3.10.10.93 Copyright 2007 Atanas Neshkov  Date: 5/21/2008 10:01:44 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   DTFrame.java

package com.ibm.media.bean.multiplayer;

import java.awt.*;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            DTWinAdapter

public class DTFrame extends Frame
{

    public DTFrame(String str)
    {
        super(str);
        addWindowListener(new DTWinAdapter(false));
        enableEvents(8L);
        setBackground(Color.lightGray);
    }

    public DTFrame(String str, boolean doExit)
    {
        super(str);
        addWindowListener(new DTWinAdapter(doExit));
        enableEvents(8L);
        setBackground(Color.lightGray);
    }

    public Insets getInsets()
    {
        return new Insets(30, 10, 10, 10);
    }
}