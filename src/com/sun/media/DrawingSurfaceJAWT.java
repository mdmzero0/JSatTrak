// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DrawingSurfaceJAWT.java

package com.sun.media;

import java.awt.Component;
import java.awt.Toolkit;

// Referenced classes of package com.sun.media:
//            JMFSecurityManager

public class DrawingSurfaceJAWT
{

    public static native int getWindowHandle(Component component);

    public static native boolean lockAWT(int i);

    public static native void unlockAWT(int i);

    public static native void freeResource(int i, int j);

    public native int getAWT();

    public native int getDrawingSurface(Component component, int i);

    public native int getDrawingSurfaceWinID(int i);

    public native int getDrawingSurfaceDisplay(int i);

    public DrawingSurfaceJAWT()
    {
        winfo = null;
        if(!avail)
            throw new RuntimeException("can't load jmfjawt native module");
        winfo = new int[5];
        for(int i = 0; i < 5; i++)
            winfo[i] = 0;

    }

    public int[] getWindowInfo(Component cc)
    {
        int value = 0;
        value = getAWT();
        if(value == 0)
        {
            winfo[0] = 0;
            return winfo;
        }
        winfo[1] = value;
        value = getDrawingSurface(cc, winfo[1]);
        if(value == 0)
        {
            winfo[0] = 0;
            return winfo;
        }
        winfo[2] = value;
        value = getDrawingSurfaceWinID(winfo[2]);
        if(value == 0)
        {
            winfo[0] = 0;
            return winfo;
        }
        winfo[3] = value;
        value = getDrawingSurfaceDisplay(winfo[2]);
        if(value == 0)
        {
            winfo[0] = 0;
            return winfo;
        } else
        {
            winfo[4] = value;
            winfo[0] = 1;
            return winfo;
        }
    }

    public static final int valid = 0;
    public static final int pawt = 1;
    public static final int pds = 2;
    public static final int pwinid = 3;
    public static final int pdisp = 4;
    private static boolean avail = true;
    int winfo[];

    static 
    {
        try
        {
            Toolkit.getDefaultToolkit();
            JMFSecurityManager.loadLibrary("jmfjawt");
            avail = true;
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            avail = false;
        }
    }
}
